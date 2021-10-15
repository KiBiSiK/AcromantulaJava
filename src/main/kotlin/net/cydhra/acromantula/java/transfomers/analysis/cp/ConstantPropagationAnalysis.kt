package net.cydhra.acromantula.java.transfomers.analysis.cp

import com.strobel.assembler.ir.OpCode
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.tree.*
import org.objectweb.asm.tree.analysis.Analyzer
import org.objectweb.asm.tree.analysis.Frame
import org.apache.logging.log4j.LogManager.getLogger as logger

class ConstantPropagationAnalysis {

    /**
     * Perform local analysis on a method node
     *
     * @param ownerName Internal name of the class that owns [methodNode]
     * @param methodNode the method to analyze
     */
    fun analyzeMethod(ownerName: String, methodNode: MethodNode) {
        val analyzer = Analyzer(ConstantValueInterpreter)
        analyzer.analyze(ownerName, methodNode)
        val frames = analyzer.frames

        val instructionIterator = methodNode.instructions.iterator()
        var frameIndex = 0
        while (instructionIterator.hasNext()) {
            val currentInstruction = instructionIterator.next()
            val currentFrame = frames[frameIndex++] ?: continue

            // replace any instruction that does actual work on constants with some pop() and some constant push
            // operations
            val applicableValue = getStackLayout(currentInstruction, currentFrame)

            if (currentInstruction is LabelNode || currentInstruction is LineNumberNode || currentInstruction is FrameNode) {
                continue
            }

            logger().debug("==================================")
            logger().debug("Instruction Frame of \"${OpCode.get(currentInstruction.opcode).name}\"")
            logger().debug("Frame Locals:")

            (0 until currentFrame.locals).forEach { i ->
                logger().debug("[LOCAL] $i: ${currentFrame.getLocal(i)}")
            }

            logger().debug("Frame Stack:")
            (0 until currentFrame.stackSize).forEach { i ->
                logger().debug("[STACK] $i: ${currentFrame.getStack(i)}")
            }

            if (applicableValue != null) {
                val (pops, values) = applicableValue

                if (values.all { it is CPConstValue }) {
                    logger().debug("all operands are constant:")
                    values.forEachIndexed { i, value ->
                        logger().debug("$i: $value")
                    }

                    val simulatedFrame = Frame(currentFrame)
                    simulatedFrame.execute(currentInstruction, ConstantValueInterpreter)
                    val resultFrame = simulatedFrame.getStack(simulatedFrame.stackSize - 1)
                    val constantValue = (resultFrame as CPConstValue)

                    logger().debug(
                        "we pop() ${pops.size} times and " +
                                "then replace the instruction with a const push of ${constantValue.value}"
                    )

                    // move cursor one element back, so next() returns the current element
                    instructionIterator.previous()

                    // insert necessary pop() instructions
                    repeat(pops.size) { i ->
                        when (pops[i]) {
                            PopVariant.POP -> instructionIterator.add(InsnNode(Opcodes.POP))
                            PopVariant.POP2 -> instructionIterator.add(InsnNode(Opcodes.POP2))
                        }
                    }

                    // insert constant push operation
                    instructionIterator.add(choosePushOperation(constantValue))

                    // remove the instruction that got replaced
                    instructionIterator.next()
                    instructionIterator.remove()
                } else {
                    logger().debug("not all operands are constant")
                }
            } else {
                logger().debug("non-applicable instruction")
            }
        }
    }

    /**
     * Check if the instruction operates side-effect-free on values and is not itself just a push-operation. If
     * so, return the associated values and the amount of necessary push operations.
     */
    private fun getStackLayout(
        instruction: AbstractInsnNode,
        currentFrameState: Frame<CPLatticeValue>
    ): Pair<List<PopVariant>, List<CPLatticeValue>>? {
        when (instruction.opcode) {
            Opcodes.INEG, Opcodes.LNEG, Opcodes.FNEG, Opcodes.DNEG, Opcodes.IINC -> {
                val stackElement = getStackValue(currentFrameState, 0)
                return Pair(listOf(PopVariant.getBySize(stackElement.size)), listOf(stackElement))
            }
            Opcodes.IADD, Opcodes.LADD, Opcodes.FADD, Opcodes.DADD, Opcodes.ISUB, Opcodes.LSUB, Opcodes.FSUB,
            Opcodes.DSUB, Opcodes.IMUL, Opcodes.LMUL, Opcodes.FMUL, Opcodes.DMUL, Opcodes.IDIV, Opcodes.LDIV,
            Opcodes.FDIV, Opcodes.DDIV, Opcodes.IREM, Opcodes.LREM, Opcodes.FREM, Opcodes.DREM, Opcodes.ISHL,
            Opcodes.LSHL, Opcodes.ISHR, Opcodes.LSHR, Opcodes.IUSHR, Opcodes.LUSHR, Opcodes.IAND, Opcodes.LAND,
            Opcodes.IOR, Opcodes.LOR, Opcodes.IXOR, Opcodes.LXOR, Opcodes.LCMP, Opcodes.DCMPL, Opcodes.FCMPL,
            Opcodes.DCMPG, Opcodes.FCMPG -> {
                val topElement = getStackValue(currentFrameState, 0)
                val secondElement = getStackValue(currentFrameState, 1)

                return Pair(
                    listOf(PopVariant.getBySize(topElement.size), PopVariant.getBySize(secondElement.size)),
                    listOf(topElement, secondElement)
                )
            }
            Opcodes.ILOAD, Opcodes.LLOAD, Opcodes.FLOAD, Opcodes.DLOAD -> {
                return Pair(emptyList(), listOf(currentFrameState.getLocal((instruction as VarInsnNode).`var`)))
            }
            Opcodes.ALOAD -> {
                val value = currentFrameState.getLocal((instruction as VarInsnNode).`var`)

                // we cannot push array types onto stacks. We only need them for array accesses
                return if (value.type!!.isArrayType()) {
                    null
                } else {
                    Pair(emptyList(), listOf(value))
                }
            }
            Opcodes.INVOKESTATIC, Opcodes.INVOKESPECIAL, Opcodes.INVOKEVIRTUAL, Opcodes.INVOKEINTERFACE -> {
                // special handling for string methods
                if ((instruction as MethodInsnNode).owner == STRING_TYPE.internalName) {
                    val parameters = Type.getArgumentTypes(instruction.desc)
                    val returnType = Type.getReturnType(instruction.desc)

                    // if the return value of the function is potentially constant, return a stack layout
                    if (returnType.isArrayType() || returnType == STRING_TYPE
                        || returnType == Type.BOOLEAN_TYPE || returnType == Type.BYTE_TYPE
                        || returnType == Type.SHORT_TYPE || returnType == Type.CHAR_TYPE
                        || returnType == Type.INT_TYPE || returnType == Type.LONG_TYPE
                        || returnType == Type.FLOAT_TYPE || returnType == Type.DOUBLE_TYPE
                    ) {
                        val pops = parameters.map { PopVariant.getBySize(it.size) }.toMutableList()
                        val values = parameters.indices
                            .map { getStackValue(currentFrameState, parameters.size - it - 1) }.toMutableList()
                        if (instruction.opcode != Opcodes.INVOKESTATIC) {
                            // add another pop for the object instance and the value for the object instance
                            pops.add(0, PopVariant.POP)
                            values.add(0, getStackValue(currentFrameState, parameters.size))
                        }

                        return Pair(pops, values)
                    }
                }
            }
        }

        return null
    }

    /**
     * Get a value from the frame's stack layout, counting from the top of the stack
     *
     * @param frame current frame layout
     * @param index stack element index starting from stack top
     *
     * @return [CPLatticeValue] at [index] from stack top
     */
    private fun getStackValue(frame: Frame<CPLatticeValue>, index: Int): CPLatticeValue {
        val indexFromTop = frame.stackSize - 1 - index
        return if (indexFromTop >= 0)
            frame.getStack(indexFromTop)
        else
            throw IllegalStateException("stack invalid for instruction")
    }

    /**
     * Choose an appropriate push operation for the given constant value. Avoids LDC-Operations and uses ICONST_N
     * special instructions
     */
    private fun choosePushOperation(value: CPConstValue): AbstractInsnNode {
        return when (value.type) {
            Type.INT_TYPE -> {
                when (value.value as Int) {
                    -1 -> InsnNode(Opcodes.ICONST_M1)
                    0 -> InsnNode(Opcodes.ICONST_0)
                    1 -> InsnNode(Opcodes.ICONST_1)
                    2 -> InsnNode(Opcodes.ICONST_2)
                    3 -> InsnNode(Opcodes.ICONST_3)
                    4 -> InsnNode(Opcodes.ICONST_4)
                    5 -> InsnNode(Opcodes.ICONST_5)
                    in Byte.MIN_VALUE..Byte.MAX_VALUE -> IntInsnNode(Opcodes.BIPUSH, value.value)
                    in Short.MIN_VALUE..Short.MAX_VALUE -> IntInsnNode(Opcodes.SIPUSH, value.value)
                    else -> LdcInsnNode(value.value)
                }
            }
            Type.LONG_TYPE, Type.DOUBLE_TYPE, Type.FLOAT_TYPE, STRING_TYPE -> LdcInsnNode(value.value)
            else -> throw IllegalStateException("non-primitive constants cannot be pushed")
        }
    }

    /**
     * Indicates whether we have to pop a singular value or a double-sized value
     */
    private enum class PopVariant {
        POP, POP2;

        companion object {
            /**
             * Get the pop variant by the type size
             */
            fun getBySize(size: Int): PopVariant {
                return when (size) {
                    1 -> POP
                    2 -> POP2
                    else -> throw IllegalArgumentException("only size 1 and size 2 are allowed")
                }
            }
        }
    }
}