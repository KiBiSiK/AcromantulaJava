package net.cydhra.acromantula.java.transfomers.analysis.cp

import net.cydhra.acromantula.java.util.returnType
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.tree.*
import org.objectweb.asm.tree.analysis.Interpreter

/**
 * Interpreter that operates on the lattice of abstract values denoting if they are const or not.
 */
object ConstantValueInterpreter : Interpreter<CPLatticeValue>(Opcodes.ASM9) {

    override fun newValue(type: Type?): CPLatticeValue? {
        return if (type == null || type == Type.VOID_TYPE) null else CPUndefined(type)
    }

    override fun newOperation(insn: AbstractInsnNode): CPLatticeValue {
        return when (insn.opcode) {
            Opcodes.ACONST_NULL -> CPNoConst() // TODO this should not be no_const
            Opcodes.ICONST_M1 -> CPConstValue(Type.INT_TYPE, -1)
            Opcodes.ICONST_0 -> CPConstValue(Type.INT_TYPE, 0)
            Opcodes.ICONST_1 -> CPConstValue(Type.INT_TYPE, 1)
            Opcodes.ICONST_2 -> CPConstValue(Type.INT_TYPE, 2)
            Opcodes.ICONST_3 -> CPConstValue(Type.INT_TYPE, 3)
            Opcodes.ICONST_4 -> CPConstValue(Type.INT_TYPE, 4)
            Opcodes.ICONST_5 -> CPConstValue(Type.INT_TYPE, 5)
            Opcodes.LCONST_0 -> CPConstValue(Type.LONG_TYPE, 0L)
            Opcodes.LCONST_1 -> CPConstValue(Type.LONG_TYPE, 1L)
            Opcodes.FCONST_0 -> CPConstValue(Type.FLOAT_TYPE, 0f)
            Opcodes.FCONST_1 -> CPConstValue(Type.FLOAT_TYPE, 1f)
            Opcodes.FCONST_2 -> CPConstValue(Type.FLOAT_TYPE, 2f)
            Opcodes.DCONST_0 -> CPConstValue(Type.DOUBLE_TYPE, 0.0)
            Opcodes.DCONST_1 -> CPConstValue(Type.DOUBLE_TYPE, 1.0)
            Opcodes.BIPUSH -> CPConstValue(Type.INT_TYPE, (insn as IntInsnNode).operand)
            Opcodes.SIPUSH -> CPConstValue(Type.INT_TYPE, (insn as IntInsnNode).operand)
            Opcodes.LDC -> {
                when ((insn as LdcInsnNode).cst) {
                    is Int -> CPConstValue(Type.INT_TYPE, insn.cst)
                    is Float -> CPConstValue(Type.FLOAT_TYPE, insn.cst)
                    is Long -> CPConstValue(Type.LONG_TYPE, insn.cst)
                    is Double -> CPConstValue(Type.DOUBLE_TYPE, insn.cst)
                    is String -> CPConstValue(STRING_TYPE, insn.cst)
                    is Type -> CPNoConst()
                    else -> throw AssertionError("constant with undefined type")
                }

            }
            Opcodes.JSR -> CPNoConst()
            Opcodes.GETSTATIC -> CPNoConst() // TODO field analysis
            Opcodes.NEW -> CPNoConst() // TODO const object analysis?
            else -> throw AssertionError("method called with undocumented instruction")
        }
    }

    override fun copyOperation(insn: AbstractInsnNode, value: CPLatticeValue): CPLatticeValue {
        return value
    }

    override fun unaryOperation(insn: AbstractInsnNode, value: CPLatticeValue): CPLatticeValue {
        return when (insn.opcode) {
            Opcodes.INEG, Opcodes.LNEG, Opcodes.FNEG, Opcodes.DNEG -> eval { neg(value) }
            Opcodes.IINC -> eval { value + CPConstValue(Type.INT_TYPE, (insn as IincInsnNode).incr) }
            Opcodes.I2L -> coerceType(value, Type.LONG_TYPE) { (it as Int).toLong() }
            Opcodes.I2F -> coerceType(value, Type.FLOAT_TYPE) { (it as Int).toFloat() }
            Opcodes.I2D -> coerceType(value, Type.DOUBLE_TYPE) { (it as Int).toDouble() }
            Opcodes.L2I -> coerceType(value, Type.INT_TYPE) { (it as Long).toInt() }
            Opcodes.L2F -> coerceType(value, Type.FLOAT_TYPE) { (it as Long).toFloat() }
            Opcodes.L2D -> coerceType(value, Type.DOUBLE_TYPE) { (it as Long).toDouble() }
            Opcodes.F2I -> coerceType(value, Type.INT_TYPE) { (it as Float).toInt() }
            Opcodes.F2L -> coerceType(value, Type.LONG_TYPE) { (it as Float).toLong() }
            Opcodes.F2D -> coerceType(value, Type.DOUBLE_TYPE) { (it as Float).toDouble() }
            Opcodes.D2I -> coerceType(value, Type.INT_TYPE) { (it as Double).toInt() }
            Opcodes.D2L -> coerceType(value, Type.LONG_TYPE) { (it as Double).toLong() }
            Opcodes.D2F -> coerceType(value, Type.FLOAT_TYPE) { (it as Double).toFloat() }
            Opcodes.I2B -> coerceType(value, Type.INT_TYPE) { (it as Int).toByte().toInt() }
            Opcodes.I2C -> coerceType(value, Type.INT_TYPE) { (it as Int).toChar().code }
            Opcodes.I2S -> coerceType(value, Type.INT_TYPE) { (it as Int).toShort().toInt() }
            Opcodes.IFEQ, Opcodes.IFNE, Opcodes.IFLT, Opcodes.IFGE, Opcodes.IFGT, Opcodes.IFLE, Opcodes.TABLESWITCH,
            Opcodes.LOOKUPSWITCH, Opcodes.IRETURN, Opcodes.LRETURN, Opcodes.FRETURN, Opcodes.DRETURN, Opcodes.ARETURN,
            Opcodes.PUTSTATIC, Opcodes.GETFIELD, Opcodes.ATHROW, Opcodes.CHECKCAST, Opcodes.INSTANCEOF,
            Opcodes.MONITORENTER, Opcodes.MONITOREXIT, Opcodes.IFNULL, Opcodes.IFNONNULL -> CPNoConst();
            // TODO more array handling, object handling, null handling

            Opcodes.NEWARRAY -> CPNoConst()
            Opcodes.ANEWARRAY -> CPNoConst()
            Opcodes.ARRAYLENGTH -> eval { value.length() }

            else -> throw AssertionError("method called with undocumented instruction")
        }
    }

    override fun binaryOperation(
        insn: AbstractInsnNode,
        value1: CPLatticeValue,
        value2: CPLatticeValue
    ): CPLatticeValue {
        return when (insn.opcode) {
            Opcodes.IALOAD, Opcodes.LALOAD, Opcodes.FALOAD, Opcodes.DALOAD,
            Opcodes.AALOAD, Opcodes.BALOAD, Opcodes.CALOAD, Opcodes.SALOAD -> CPNoConst() // todo array handling
            Opcodes.IADD, Opcodes.LADD, Opcodes.FADD, Opcodes.DADD -> eval { value1 + value2 }
            Opcodes.ISUB, Opcodes.LSUB, Opcodes.FSUB, Opcodes.DSUB -> eval { value1 - value2 }
            Opcodes.IMUL, Opcodes.LMUL, Opcodes.FMUL, Opcodes.DMUL -> eval { value1 * value2 }
            Opcodes.IDIV, Opcodes.LDIV, Opcodes.FDIV, Opcodes.DDIV -> eval { value1 / value2 }
            Opcodes.IREM, Opcodes.LREM, Opcodes.FREM, Opcodes.DREM -> eval { value1 rem value2 }
            Opcodes.ISHL, Opcodes.LSHL -> eval { value1 shl value2 }
            Opcodes.ISHR, Opcodes.LSHR -> eval { value1 shr value2 }
            Opcodes.IUSHR, Opcodes.LUSHR -> eval { value1 ushr value2 }
            Opcodes.IAND, Opcodes.LAND -> eval { value1 and value2 }
            Opcodes.IOR, Opcodes.LOR -> eval { value1 or value2 }
            Opcodes.IXOR, Opcodes.LXOR -> eval { value1 xor value2 }
            Opcodes.LCMP -> eval { value1 lcmp value2 }
            Opcodes.DCMPL, Opcodes.FCMPL -> eval { value1 cmpl value2 }
            Opcodes.DCMPG, Opcodes.FCMPG -> eval { value1 cmpg value2 }
            Opcodes.IF_ICMPEQ, Opcodes.IF_ICMPNE, Opcodes.IF_ICMPLT, Opcodes.IF_ICMPGE, Opcodes.IF_ICMPGT,
            Opcodes.IF_ICMPLE, Opcodes.IF_ACMPEQ, Opcodes.IF_ACMPNE, Opcodes.PUTFIELD -> CPNoConst()
            else -> throw AssertionError("method called with undocumented instruction")
        }
    }

    override fun ternaryOperation(
        insn: AbstractInsnNode?,
        value1: CPLatticeValue?,
        value2: CPLatticeValue?,
        value3: CPLatticeValue?
    ): CPLatticeValue {
        return CPNoConst()
    }

    override fun naryOperation(insn: AbstractInsnNode, values: MutableList<out CPLatticeValue>?): CPLatticeValue? {
        // TODO: interprocedural propagation
        return when (insn) {
            is InvokeDynamicInsnNode -> {
                if (returnType(insn.desc) == "V")
                    null
                else
                    CPNoConst()
            }
            is MethodInsnNode -> {
                if (returnType(insn.desc) == "V")
                    null
                else {
                    // special handling for string methods
                    if (insn.owner == STRING_TYPE.internalName) {
                        val parameters = Type.getArgumentTypes(insn.desc)
                        val returnType = Type.getReturnType(insn.desc)

                        if (returnType == Type.VOID_TYPE) {
                            null
                        } else if (returnType.isArrayType() || returnType == STRING_TYPE
                            || returnType == Type.BOOLEAN_TYPE || returnType == Type.BYTE_TYPE
                            || returnType == Type.SHORT_TYPE || returnType == Type.CHAR_TYPE
                            || returnType == Type.INT_TYPE || returnType == Type.LONG_TYPE
                            || returnType == Type.FLOAT_TYPE || returnType == Type.DOUBLE_TYPE
                        ) {
                            evalString(
                                insn.desc,
                                insn.name,
                                parameters.map { it.className }.toTypedArray(),
                                insn.opcode == Opcodes.INVOKESTATIC,
                                *values?.toTypedArray() ?: emptyArray()
                            )
                        } else {
                            CPNoConst()
                        }
                    } else {
                        CPNoConst()
                    }
                }
            }
            is MultiANewArrayInsnNode -> {
                // TODO multi dimensional array handling
                CPNoConst()
            }
            else -> throw AssertionError("method called with undocumented instruction")
        }
    }

    override fun returnOperation(insn: AbstractInsnNode, value: CPLatticeValue, expected: CPLatticeValue?) {
        // return operations don't produce a value
    }

    override fun merge(value1: CPLatticeValue, value2: CPLatticeValue): CPLatticeValue {
        if (value1 == value2)
            return value1

        if (value1 is CPUndefined || value2 is CPUndefined) {
            return CPUndefined(mergeTypes(value1.type, value2.type))
        }

        return CPNoConst()
    }
}