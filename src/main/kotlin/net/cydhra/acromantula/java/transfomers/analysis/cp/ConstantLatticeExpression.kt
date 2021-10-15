package net.cydhra.acromantula.java.transfomers.analysis.cp

import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import java.lang.reflect.Method

/**
 * Cache for string method reflections
 */
private val reflectionCache = mutableMapOf<String, Method>()

/**
 * Evaluate an expression given in form of a lambda function that operates on [lattice values][CPLatticeValue]. The
 * eval function does nothing but return the [CPLatticeValue] that the [expr] lambda produces. The expression is
 * solvable, if it is well-defined in the expression DSL implemented in [ConstantLatticeAlgebra]
 */
fun eval(expr: ConstantLatticeAlgebra.() -> CPLatticeValue) = ConstantLatticeAlgebra.let(expr)

/**
 * Check whether any of the given values is [CPUndefined]
 */
private fun checkBottom(vararg values: CPLatticeValue): Boolean {
    return values.any { it is CPUndefined }
}

/**
 * Check whether any of the given values is [CPNoConst]
 */
private fun checkTop(vararg values: CPLatticeValue): Boolean {
    return values.any { it is CPNoConst }
}

fun evalString(
    methodDesc: String,
    method: String,
    parameterTypes: Array<String>,
    isStatic: Boolean,
    vararg arguments: CPLatticeValue
): CPLatticeValue {
    // check if inputs are const
    if (checkBottom(*arguments))
        return CPUndefined(STRING_TYPE)

    if (checkTop(*arguments))
        return CPNoConst()

    val parameterClasses = parameterTypes
        .map {
            when (it) {
                "byte" -> Byte::class.javaPrimitiveType
                "char" -> Char::class.javaPrimitiveType
                "short" -> Short::class.javaPrimitiveType
                "int" -> Int::class.javaPrimitiveType
                "long" -> Long::class.javaPrimitiveType
                "float" -> Float::class.javaPrimitiveType
                "double" -> Double::class.javaPrimitiveType
                else -> Class.forName(it)
            }
        }
        .toTypedArray()

    // find method reflection
    val reflection = reflectionCache.getOrPut(methodDesc) {
        String::class.java.getMethod(method, *parameterClasses)
    }

    // extract actual values
    val argumentValues = arguments
        .map { (it as CPConstValue).value }
        .toTypedArray()

    // cast primitive value integers to corresponding types
    parameterClasses
        .withIndex()
        .forEach { (i, cls) ->
            when (cls) {
                Boolean::class.javaPrimitiveType -> {
                    argumentValues[i + 1] = argumentValues[i + 1] != 0
                }
                Byte::class.javaPrimitiveType -> {
                    argumentValues[i + 1] = (argumentValues[i + 1]!! as Int).toByte()
                }
                Char::class.javaPrimitiveType -> {
                    argumentValues[i + 1] = Char(argumentValues[i + 1]!! as Int)
                }
                Short::class.javaPrimitiveType -> {
                    argumentValues[i + 1] = (argumentValues[i + 1]!! as Int).toShort()
                }
            }
        }

    // calculate string operation
    val newConst = if (isStatic)
        reflection.invoke(null, argumentValues)
    else
        reflection.invoke(
            argumentValues.first(),
            *argumentValues
                .slice(1 until argumentValues.size)
                .toTypedArray()
        )

    // return result as const value
    return CPConstValue(Type.getType(newConst.javaClass), newConst)
}

object ConstantLatticeAlgebra {

    /**
     * Add two values together
     */
    operator fun CPLatticeValue.plus(other: CPLatticeValue): CPLatticeValue {
        if (checkBottom(this, other))
            return CPUndefined(mergeTypes(this.type, other.type))

        if (checkTop(this, other))
            return CPNoConst()

        return (this as CPConstValue).add(other as CPConstValue)
    }

    /**
     * Subtract two values
     */
    operator fun CPLatticeValue.minus(other: CPLatticeValue): CPLatticeValue {
        if (checkBottom(this, other))
            return CPUndefined(mergeTypes(this.type, other.type))

        if (checkTop(this, other))
            return CPNoConst()

        return (this as CPConstValue).sub(other as CPConstValue)
    }

    /**
     * Multiply two values
     */
    operator fun CPLatticeValue.times(other: CPLatticeValue): CPLatticeValue {
        if (checkBottom(this, other))
            return CPUndefined(mergeTypes(this.type, other.type))

        if (checkTop(this, other))
            return CPNoConst()

        return (this as CPConstValue).mul(other as CPConstValue)
    }

    /**
     * Divide this value by the given divisor
     */
    operator fun CPLatticeValue.div(other: CPLatticeValue): CPLatticeValue {
        if (checkBottom(this, other))
            return CPUndefined(mergeTypes(this.type, other.type))

        if (checkTop(this, other))
            return CPNoConst()

        return (this as CPConstValue).div(other as CPConstValue)
    }

    /**
     * Calculate the remainder when dividing this value by [other]
     */
    infix fun CPLatticeValue.rem(other: CPLatticeValue): CPLatticeValue {
        if (checkBottom(this, other))
            return CPUndefined(mergeTypes(this.type, other.type))

        if (checkTop(this, other))
            return CPNoConst()

        return (this as CPConstValue).remainder(other as CPConstValue)
    }

    /**
     * Shift this value right by [other]
     */
    infix fun CPLatticeValue.shr(other: CPLatticeValue): CPLatticeValue {
        if (checkBottom(this, other))
            return CPUndefined(mergeTypes(this.type, other.type))

        if (checkTop(this, other))
            return CPNoConst()

        return (this as CPConstValue).shiftRight(other as CPConstValue)
    }

    /**
     * Shift this value right by [other]
     */
    infix fun CPLatticeValue.ushr(other: CPLatticeValue): CPLatticeValue {
        if (checkBottom(this, other))
            return CPUndefined(mergeTypes(this.type, other.type))

        if (checkTop(this, other))
            return CPNoConst()

        return (this as CPConstValue).unsignedShiftRight(other as CPConstValue)
    }

    /**
     * Shift this value left by [other]
     */
    infix fun CPLatticeValue.shl(other: CPLatticeValue): CPLatticeValue {
        if (checkBottom(this, other))
            return CPUndefined(mergeTypes(this.type, other.type))

        if (checkTop(this, other))
            return CPNoConst()

        return (this as CPConstValue).shiftLeft(other as CPConstValue)
    }

    /**
     * Bitwise and of two values
     */
    infix fun CPLatticeValue.and(other: CPLatticeValue): CPLatticeValue {
        if (checkBottom(this, other))
            return CPUndefined(mergeTypes(this.type, other.type))

        if (checkTop(this, other))
            return CPNoConst()

        return (this as CPConstValue).bitwiseAnd(other as CPConstValue)
    }

    /**
     * Bitwise or of two values
     */
    infix fun CPLatticeValue.or(other: CPLatticeValue): CPLatticeValue {
        if (checkBottom(this, other))
            return CPUndefined(mergeTypes(this.type, other.type))

        if (checkTop(this, other))
            return CPNoConst()

        return (this as CPConstValue).bitwiseOr(other as CPConstValue)
    }

    /**
     * Bitwise xor of two values
     */
    infix fun CPLatticeValue.xor(other: CPLatticeValue): CPLatticeValue {
        if (checkBottom(this, other))
            return CPUndefined(mergeTypes(this.type, other.type))

        if (checkTop(this, other))
            return CPNoConst()

        return (this as CPConstValue).bitwiseXor(other as CPConstValue)
    }

    /**
     * Long compare of two long-values
     */
    infix fun CPLatticeValue.lcmp(other: CPLatticeValue): CPLatticeValue {
        if (checkBottom(this, other))
            return CPUndefined(mergeTypes(this.type, other.type))

        if (checkTop(this, other))
            return CPNoConst()

        return (this as CPConstValue).longCompare(other as CPConstValue)
    }

    /**
     * Float compare of two values where NaN produces 1
     */
    infix fun CPLatticeValue.cmpg(other: CPLatticeValue): CPLatticeValue {
        if (checkBottom(this, other))
            return CPUndefined(mergeTypes(this.type, other.type))

        if (checkTop(this, other))
            return CPNoConst()

        return (this as CPConstValue).floatingCompareGreater(other as CPConstValue)
    }

    /**
     * Float compare of two values where NaN produces 1
     */
    infix fun CPLatticeValue.cmpl(other: CPLatticeValue): CPLatticeValue {
        if (checkBottom(this, other))
            return CPUndefined(mergeTypes(this.type, other.type))

        if (checkTop(this, other))
            return CPNoConst()

        return (this as CPConstValue).floatingCompareLesser(other as CPConstValue)
    }

    /**
     * Negate the given value
     */
    fun neg(value: CPLatticeValue): CPLatticeValue {
        if (checkBottom(value))
            return CPUndefined(value.type)

        if (checkTop(value))
            return CPNoConst()

        return (value as CPConstValue).negate()
    }

    /**
     * Get the array length of the value
     */
    fun CPLatticeValue.length(): CPLatticeValue {
        if (checkBottom(this))
            return CPUndefined(this.type)

        if (checkTop(this))
            return CPNoConst()

        return (this as CPConstValue).arrayLength()
    }

    /**
     * Create a new array-type value
     */
    fun newArray(kind: Int, size: CPLatticeValue): CPLatticeValue {
        val type = when (kind) {
            Opcodes.T_BOOLEAN -> Type.getType(Array<Boolean>::class.java)
            Opcodes.T_CHAR -> Type.getType(Array<Char>::class.java)
            Opcodes.T_FLOAT -> Type.getType(Array<Float>::class.java)
            Opcodes.T_DOUBLE -> Type.getType(Array<Double>::class.java)
            Opcodes.T_BYTE -> Type.getType(Array<Byte>::class.java)
            Opcodes.T_SHORT -> Type.getType(Array<Short>::class.java)
            Opcodes.T_INT -> Type.getType(Array<Int>::class.java)
            Opcodes.T_LONG -> Type.getType(Array<Long>::class.java)
            else -> throw AssertionError("illegal kind of array")
        }

        if (checkBottom(size))
            return CPUndefined(type)

        if (checkTop(size))
            return CPNoConst()

        val arraySize = (size as CPConstValue).value as Int

        return CPConstValue(type, when (kind) {
            Opcodes.T_BOOLEAN -> Array(arraySize) { false }
            Opcodes.T_CHAR -> Array(arraySize) { Char(0) }
            Opcodes.T_FLOAT -> Array(arraySize) { 0f }
            Opcodes.T_DOUBLE -> Array(arraySize) { 0.0 }
            Opcodes.T_BYTE -> Array(arraySize) { 0.toByte() }
            Opcodes.T_SHORT -> Array(arraySize) { 0.toShort() }
            Opcodes.T_INT -> Array(arraySize) { 0 }
            Opcodes.T_LONG -> Array(arraySize) { 0L }
            else -> throw AssertionError()
        })
    }
}