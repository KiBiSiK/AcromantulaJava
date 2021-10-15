package net.cydhra.acromantula.java.transfomers.analysis.elimination

import net.cydhra.acromantula.java.util.returnType
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.tree.*
import org.objectweb.asm.tree.analysis.Interpreter

/**
 * Interpreter that operates on the lattice of abstract values denoting if they are const or not.
 */
object DeadVariableInterpreter : Interpreter<VariableLiveValue>(Opcodes.ASM9) {

    override fun newValue(type: Type?): VariableLiveValue? {
        if (type == Type.VOID_TYPE)
            return null

        return VariableLiveValue(type, null, LiveState.UNDEFINED)
    }

    override fun newOperation(insn: AbstractInsnNode): VariableLiveValue? {
        return when (insn.opcode) {
            Opcodes.ACONST_NULL -> VariableLiveValue(null, insn, LiveState.DEFINED)
            Opcodes.ICONST_M1, Opcodes.ICONST_0, Opcodes.ICONST_1, Opcodes.ICONST_2, Opcodes.ICONST_3, Opcodes.ICONST_4,
            Opcodes.ICONST_5, Opcodes.LCONST_0, Opcodes.LCONST_1, Opcodes.FCONST_0, Opcodes.FCONST_1, Opcodes.FCONST_2,
            Opcodes.DCONST_0, Opcodes.DCONST_1, Opcodes.BIPUSH, Opcodes.SIPUSH, Opcodes.LDC ->
                VariableLiveValue(Type.INT_TYPE, insn, LiveState.DEFINED)
            Opcodes.JSR -> null
            Opcodes.GETSTATIC -> {
                VariableLiveValue(Type.getType((insn as FieldInsnNode).desc), insn, LiveState.DEFINED)
            }
            Opcodes.NEW -> {
                VariableLiveValue(Type.getObjectType((insn as TypeInsnNode).desc), insn, LiveState.DEFINED)
            }
            else -> throw AssertionError("method called with undocumented instruction")
        }
    }

    override fun copyOperation(insn: AbstractInsnNode, value: VariableLiveValue): VariableLiveValue {
        return VariableLiveValue(value.type, insn, LiveState.DEFINED)
    }

    override fun unaryOperation(insn: AbstractInsnNode, value: VariableLiveValue): VariableLiveValue? {
        value.state = LiveState.USED

        return when (insn.opcode) {
            Opcodes.INEG, Opcodes.LNEG, Opcodes.FNEG, Opcodes.DNEG, Opcodes.IINC, Opcodes.I2L, Opcodes.I2F, Opcodes.I2D,
            Opcodes.L2I, Opcodes.L2F, Opcodes.L2D, Opcodes.F2I, Opcodes.F2L, Opcodes.F2D, Opcodes.D2I, Opcodes.D2L,
            Opcodes.D2F, Opcodes.I2B, Opcodes.I2C, Opcodes.I2S, Opcodes.GETFIELD, Opcodes.INSTANCEOF,
            Opcodes.NEWARRAY, Opcodes.ANEWARRAY, Opcodes.ARRAYLENGTH ->
                VariableLiveValue(value.type, insn, LiveState.DEFINED)

            Opcodes.IFEQ, Opcodes.IFNE, Opcodes.IFLT, Opcodes.IFGE, Opcodes.IFGT, Opcodes.IFLE, Opcodes.TABLESWITCH,
            Opcodes.LOOKUPSWITCH, Opcodes.IRETURN, Opcodes.LRETURN, Opcodes.FRETURN, Opcodes.DRETURN, Opcodes.ARETURN,
            Opcodes.PUTSTATIC, Opcodes.ATHROW, Opcodes.CHECKCAST, Opcodes.MONITORENTER, Opcodes.MONITOREXIT,
            Opcodes.IFNULL, Opcodes.IFNONNULL ->
                null


            else -> throw AssertionError("method called with undocumented instruction")
        }
    }

    override fun binaryOperation(
        insn: AbstractInsnNode,
        value1: VariableLiveValue,
        value2: VariableLiveValue
    ): VariableLiveValue? {
        value1.state = LiveState.USED
        value2.state = LiveState.USED

        return when (insn.opcode) {
            Opcodes.IALOAD, Opcodes.IADD, Opcodes.ISUB, Opcodes.IMUL, Opcodes.IDIV, Opcodes.IREM, Opcodes.ISHL, Opcodes.ISHR,
            Opcodes.IUSHR, Opcodes.IAND, Opcodes.IOR, Opcodes.IXOR, Opcodes.BALOAD, Opcodes.CALOAD, Opcodes.SALOAD ->
                VariableLiveValue(Type.INT_TYPE, insn, LiveState.DEFINED)

            Opcodes.LALOAD, Opcodes.LADD, Opcodes.LSUB, Opcodes.LMUL, Opcodes.LDIV, Opcodes.LREM, Opcodes.LSHL,
            Opcodes.LSHR, Opcodes.LUSHR, Opcodes.LAND, Opcodes.LOR, Opcodes.LXOR, Opcodes.LCMP ->
                VariableLiveValue(Type.INT_TYPE, insn, LiveState.DEFINED)

            Opcodes.FALOAD, Opcodes.FADD, Opcodes.FSUB, Opcodes.FMUL, Opcodes.FDIV, Opcodes.FREM, Opcodes.FCMPL,
            Opcodes.FCMPG ->
                VariableLiveValue(Type.FLOAT_TYPE, insn, LiveState.DEFINED)

            Opcodes.DALOAD, Opcodes.DADD, Opcodes.DSUB, Opcodes.DMUL, Opcodes.DDIV, Opcodes.DREM, Opcodes.DCMPL,
            Opcodes.DCMPG ->
                VariableLiveValue(Type.DOUBLE_TYPE, insn, LiveState.DEFINED)

            Opcodes.AALOAD -> VariableLiveValue(null, insn, LiveState.DEFINED)

            Opcodes.IF_ICMPEQ, Opcodes.IF_ICMPNE, Opcodes.IF_ICMPLT, Opcodes.IF_ICMPGE, Opcodes.IF_ICMPGT,
            Opcodes.IF_ICMPLE, Opcodes.IF_ACMPEQ, Opcodes.IF_ACMPNE, Opcodes.PUTFIELD ->
                null

            else -> throw AssertionError("method called with undocumented instruction")
        }
    }

    override fun ternaryOperation(
        insn: AbstractInsnNode,
        value1: VariableLiveValue,
        value2: VariableLiveValue,
        value3: VariableLiveValue
    ): VariableLiveValue {
        value1.state = LiveState.USED
        value2.state = LiveState.USED
        value3.state = LiveState.USED
        return VariableLiveValue(null, insn, LiveState.UNDEFINED)
    }

    override fun naryOperation(
        insn: AbstractInsnNode,
        values: MutableList<out VariableLiveValue>
    ): VariableLiveValue? {
        values.forEach { it.state = LiveState.USED }

        return when (insn) {
            is InvokeDynamicInsnNode -> {
                val returnType = returnType(insn.desc)
                if (returnType == "V")
                    null
                else
                    VariableLiveValue(Type.getType(returnType), insn, LiveState.DEFINED)
            }
            is MethodInsnNode -> {
                val returnType = returnType(insn.desc)
                if (returnType == "V")
                    null
                else {
                    VariableLiveValue(Type.getType(returnType), insn, LiveState.DEFINED)
                }
            }
            is MultiANewArrayInsnNode -> {
                VariableLiveValue(Type.getType(insn.desc), insn, LiveState.DEFINED)
            }
            else -> throw AssertionError("method called with undocumented instruction")
        }
    }

    override fun returnOperation(insn: AbstractInsnNode, value: VariableLiveValue, expected: VariableLiveValue) {
        value.state = LiveState.USED
    }

    override fun merge(value1: VariableLiveValue, value2: VariableLiveValue): VariableLiveValue {
        if (value1 == value2)
            return value1

        return arrayOf(value1, value2).maxByOrNull { it.state.ordinal }!!
    }

}