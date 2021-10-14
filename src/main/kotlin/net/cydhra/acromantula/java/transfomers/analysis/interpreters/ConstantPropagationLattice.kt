package net.cydhra.acromantula.java.transfomers.analysis.interpreters

import org.objectweb.asm.Type
import org.objectweb.asm.tree.analysis.Value

/**
 * Constant Propagation lattice value. The lattice has three different types of values: UNDEFINED (lattice bottom),
 * CONST_VAL (with any kind of constant value from the target type space), and NO_CONST. All of them are expressed
 * through subtypes of this abstract type.
 */
abstract class CPLatticeValue(val type: Type?) : Value {
    // TODO: eval function, etc

    override fun getSize(): Int {
        return this.type?.size ?: 0
    }
}

/**
 * Bottom-type of the lattice that expresses that not enough is known about a value
 */
class CPUndefined(type: Type?) : CPLatticeValue(type) {
    override fun equals(other: Any?): Boolean {
        return other != null && other is CPUndefined
    }
}

/**
 * Top-type of the lattice that expresses that a value is not constant
 */
class CPNoConst : CPLatticeValue(null) {
    override fun equals(other: Any?): Boolean {
        return other != null && other is CPNoConst
    }
}

/**
 * Any value of the [type]-defined value space. An instance of this class indicates that the associated variable is
 * of constant value.
 */
class CPConstValue<T>(type: Type?, val value: T?) : CPLatticeValue(type) {

    override fun equals(other: Any?): Boolean {
        return other != null && other is CPConstValue<*> && other.type == this.type && other.value == this.value
    }

    fun add(other: CPConstValue<*>): CPConstValue<*> {
        check(this.value is Number && other.value is Number) { "tried to eval values that are not numeric" }
        check(this.type == other.type) { "tried to add values with different types" }

        return when (this.type) {
            Type.INT_TYPE -> CPConstValue(this.type, this.value as Int + other.value as Int)
            Type.LONG_TYPE -> CPConstValue(this.type, this.value as Long + other.value as Long)
            Type.FLOAT_TYPE -> CPConstValue(this.type, this.value as Float + other.value as Float)
            Type.DOUBLE_TYPE -> CPConstValue(this.type, this.value as Double + other.value as Double)
            else -> throw AssertionError("unexpected type")
        }
    }

    fun sub(other: CPConstValue<*>): CPConstValue<*> {
        check(this.value is Number && other.value is Number) { "tried to eval values that are not numeric" }
        check(this.type == other.type) { "tried to add values with different types" }

        return when (this.type) {
            Type.INT_TYPE -> CPConstValue(this.type, this.value as Int - other.value as Int)
            Type.LONG_TYPE -> CPConstValue(this.type, this.value as Long - other.value as Long)
            Type.FLOAT_TYPE -> CPConstValue(this.type, this.value as Float - other.value as Float)
            Type.DOUBLE_TYPE -> CPConstValue(this.type, this.value as Double - other.value as Double)
            else -> throw AssertionError("unexpected type")
        }
    }

    fun mul(other: CPConstValue<*>): CPConstValue<*> {
        check(this.value is Number && other.value is Number) { "tried to eval values that are not numeric" }
        check(this.type == other.type) { "tried to add values with different types" }

        return when (this.type) {
            Type.INT_TYPE -> CPConstValue(this.type, this.value as Int * other.value as Int)
            Type.LONG_TYPE -> CPConstValue(this.type, this.value as Long * other.value as Long)
            Type.FLOAT_TYPE -> CPConstValue(this.type, this.value as Float * other.value as Float)
            Type.DOUBLE_TYPE -> CPConstValue(this.type, this.value as Double * other.value as Double)
            else -> throw AssertionError("unexpected type")
        }
    }

    fun div(other: CPConstValue<*>): CPConstValue<*> {
        check(this.value is Number && other.value is Number) { "tried to eval values that are not numeric" }
        check(this.type == other.type) { "tried to add values with different types" }

        return when (this.type) {
            Type.INT_TYPE -> CPConstValue(this.type, this.value as Int / other.value as Int)
            Type.LONG_TYPE -> CPConstValue(this.type, this.value as Long / other.value as Long)
            Type.FLOAT_TYPE -> CPConstValue(this.type, this.value as Float / other.value as Float)
            Type.DOUBLE_TYPE -> CPConstValue(this.type, this.value as Double / other.value as Double)
            else -> throw AssertionError("unexpected type")
        }
    }

    fun remainder(other: CPConstValue<*>): CPConstValue<*> {
        check(this.value is Number && other.value is Number) { "tried to eval values that are not numeric" }
        check(this.type == other.type) { "tried to add values with different types" }

        return when (this.type) {
            Type.INT_TYPE -> CPConstValue(Type.INT_TYPE, (this.value as Int).rem(other.value as Int))
            Type.LONG_TYPE -> CPConstValue(Type.LONG_TYPE, (this.value as Long).rem(other.value as Long))
            Type.FLOAT_TYPE -> CPConstValue(Type.FLOAT_TYPE, (this.value as Float).rem(other.value as Float))
            Type.DOUBLE_TYPE -> CPConstValue(Type.DOUBLE_TYPE, (this.value as Double).rem(other.value as Double))
            else -> throw AssertionError("unexpected type")
        }
    }

    fun negate(): CPConstValue<*> {
        check(this.value is Number) { "tried to eval value that is not numeric" }

        return when (this.type) {
            Type.INT_TYPE -> CPConstValue(this.type, -(this.value as Int))
            Type.LONG_TYPE -> CPConstValue(this.type, -(this.value as Long))
            Type.FLOAT_TYPE -> CPConstValue(this.type, -(this.value as Float))
            Type.DOUBLE_TYPE -> CPConstValue(this.type, -(this.value as Double))
            else -> throw AssertionError("unexpected type")
        }
    }

    fun shiftRight(other: CPConstValue<*>): CPConstValue<*> {
        check(this.value is Number && other.value is Number) { "tried to eval values that are not integers" }
        check(this.type == other.type) { "tried to add values with different types" }

        return when (this.type) {
            Type.INT_TYPE -> CPConstValue(this.type, (this.value as Int).shr(other.value as Int))
            Type.LONG_TYPE -> CPConstValue(this.type, (this.value as Long).shr(other.value as Int))
            else -> throw AssertionError("unexpected type")
        }
    }

    fun unsignedShiftRight(other: CPConstValue<*>): CPConstValue<*> {
        check(this.value is Number && other.value is Number) { "tried to eval values that are not integers" }
        check(this.type == other.type) { "tried to eval values with different types" }

        return when (this.type) {
            Type.INT_TYPE -> CPConstValue(this.type, (this.value as Int).ushr(other.value as Int))
            Type.LONG_TYPE -> CPConstValue(this.type, (this.value as Long).ushr(other.value as Int))
            else -> throw AssertionError("unexpected type")
        }
    }

    fun shiftLeft(other: CPConstValue<*>): CPConstValue<*> {
        check(this.value is Number && other.value is Number) { "tried to eval values that are not integers" }
        check(this.type == other.type) { "tried to eval values with different types" }

        return when (this.type) {
            Type.INT_TYPE -> CPConstValue(this.type, (this.value as Int).shl(other.value as Int))
            Type.LONG_TYPE -> CPConstValue(this.type, (this.value as Long).shl(other.value as Int))
            else -> throw AssertionError("unexpected type")
        }
    }

    fun bitwiseAnd(other: CPConstValue<*>): CPConstValue<*> {
        check(this.value is Number && other.value is Number) { "tried to eval values that are not integers" }
        check(this.type == other.type) { "tried to eval values with different types" }

        return when (this.type) {
            Type.INT_TYPE -> CPConstValue(this.type, (this.value as Int).and(other.value as Int))
            Type.LONG_TYPE -> CPConstValue(this.type, (this.value as Long).and(other.value as Long))
            else -> throw AssertionError("unexpected type")
        }
    }

    fun bitwiseOr(other: CPConstValue<*>): CPConstValue<*> {
        check(this.value is Number && other.value is Number) { "tried to eval values that are not integers" }
        check(this.type == other.type) { "tried to eval values with different types" }

        return when (this.type) {
            Type.INT_TYPE -> CPConstValue(this.type, (this.value as Int).or(other.value as Int))
            Type.LONG_TYPE -> CPConstValue(this.type, (this.value as Long).or(other.value as Long))
            else -> throw AssertionError("unexpected type")
        }
    }

    fun bitwiseXor(other: CPConstValue<*>): CPConstValue<*> {
        check(this.value is Number && other.value is Number) { "tried to eval values that are not integers" }
        check(this.type == other.type) { "tried to eval values with different types" }

        return when (this.type) {
            Type.INT_TYPE -> CPConstValue(this.type, (this.value as Int).xor(other.value as Int))
            Type.LONG_TYPE -> CPConstValue(this.type, (this.value as Long).xor(other.value as Long))
            else -> throw AssertionError("unexpected type")
        }
    }

    fun longCompare(other: CPConstValue<*>): CPConstValue<*> {
        check(this.value is Long && other.value is Long) { "tried to eval values that are not longs" }
        check(this.type == other.type) { "tried to eval values with different types" }

        return when (this.type) {
            Type.LONG_TYPE -> CPConstValue(Type.INT_TYPE, (this.value as Long).compareTo(other.value).coerceIn(-1, 1))
            else -> throw AssertionError("unexpected type")
        }
    }

    fun floatingCompareGreater(other: CPConstValue<*>): CPConstValue<*> {
        check(
            (this.value is Float && other.value is Float) ||
                    this.value is Double && other.value is Double
        ) { "tried to eval values that are not floating point numbers" }
        check(this.type == other.type) { "tried to eval values with different types" }

        return when (this.type) {
            Type.FLOAT_TYPE -> {
                if ((this.value as Float).isNaN() || (other.value as Float).isNaN()) {
                    CPConstValue(
                        Type.INT_TYPE,
                        1
                    )
                } else {
                    CPConstValue(
                        Type.INT_TYPE,
                        (this.value as Float).compareTo(other.value).coerceIn(-1, 1)
                    )
                }
            }
            Type.DOUBLE_TYPE -> {
                if ((this.value as Double).isNaN() || (other.value as Double).isNaN()) {
                    CPConstValue(
                        Type.INT_TYPE,
                        1
                    )
                } else {
                    CPConstValue(
                        Type.INT_TYPE,
                        (this.value as Double).compareTo(other.value).coerceIn(-1, 1)
                    )
                }
            }
            else -> throw AssertionError("unexpected type")
        }
    }

    fun floatingCompareLesser(other: CPConstValue<*>): CPConstValue<*> {
        check(
            (this.value is Float && other.value is Float) ||
                    this.value is Double && other.value is Double
        ) { "tried to eval values that are not floating point numbers" }
        check(this.type == other.type) { "tried to eval values with different types" }

        return when (this.type) {
            Type.FLOAT_TYPE -> {
                if ((this.value as Float).isNaN() || (other.value as Float).isNaN()) {
                    CPConstValue(
                        Type.INT_TYPE,
                        -1
                    )
                } else {
                    CPConstValue(
                        Type.INT_TYPE,
                        (this.value as Float).compareTo(other.value).coerceIn(-1, 1)
                    )
                }
            }
            Type.DOUBLE_TYPE -> {
                if ((this.value as Double).isNaN() || (other.value as Double).isNaN()) {
                    CPConstValue(
                        Type.INT_TYPE,
                        -1
                    )
                } else {
                    CPConstValue(
                        Type.INT_TYPE,
                        (this.value as Double).compareTo(other.value).coerceIn(-1, 1)
                    )
                }
            }
            else -> throw AssertionError("unexpected type")
        }
    }
}

fun mergeTypes(type1: Type?, type2: Type?): Type? {
    // TODO better merge, not just null avoidance

    if (type1 == null)
        return type2

    if (type2 == null)
        return type1

    if (type1 == Type.VOID_TYPE && type2 != Type.VOID_TYPE)
        return type2

    return type1
}

fun <T> coerceType(value: CPLatticeValue, type: Type, converter: (Any?) -> T): CPLatticeValue {
    return if (value is CPNoConst) {
        value
    } else {
        if (value is CPConstValue<*>) {
            CPConstValue(type, converter(value.value))
        } else {
            CPUndefined(type)
        }
    }
}