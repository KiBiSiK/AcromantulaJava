package net.cydhra.acromantula.java.transfomers.analysis.cp

/**
 * Evaluate an expression given in form of a lambda function that operates on [lattice values][CPLatticeValue]. The
 * eval function does nothing but return the [CPLatticeValue] that the [expr] lambda produces. The expression is
 * solvable, if it is well-defined in the expression DSL implemented in [ConstantLatticeAlgebra]
 */
fun eval(expr: ConstantLatticeAlgebra.() -> CPLatticeValue) = ConstantLatticeAlgebra.let(expr)

object ConstantLatticeAlgebra {

    private fun checkBottom(vararg values: CPLatticeValue): Boolean {
        return values.any { it is CPUndefined }
    }

    private fun checkTop(vararg values: CPLatticeValue): Boolean {
        return values.any { it is CPNoConst }
    }

    /**
     * Add two values together
     */
    operator fun CPLatticeValue.plus(other: CPLatticeValue): CPLatticeValue {
        if (checkBottom(this, other))
            return CPUndefined(mergeTypes(this.type, other.type))

        if (checkTop(this, other))
            return CPNoConst()

        return (this as CPConstValue<*>).add(other as CPConstValue<*>)
    }

    /**
     * Subtract two values
     */
    operator fun CPLatticeValue.minus(other: CPLatticeValue): CPLatticeValue {
        if (checkBottom(this, other))
            return CPUndefined(mergeTypes(this.type, other.type))

        if (checkTop(this, other))
            return CPNoConst()

        return (this as CPConstValue<*>).sub(other as CPConstValue<*>)
    }

    /**
     * Multiply two values
     */
    operator fun CPLatticeValue.times(other: CPLatticeValue): CPLatticeValue {
        if (checkBottom(this, other))
            return CPUndefined(mergeTypes(this.type, other.type))

        if (checkTop(this, other))
            return CPNoConst()

        return (this as CPConstValue<*>).mul(other as CPConstValue<*>)
    }

    /**
     * Divide this value by the given divisor
     */
    operator fun CPLatticeValue.div(other: CPLatticeValue): CPLatticeValue {
        if (checkBottom(this, other))
            return CPUndefined(mergeTypes(this.type, other.type))

        if (checkTop(this, other))
            return CPNoConst()

        return (this as CPConstValue<*>).div(other as CPConstValue<*>)
    }

    /**
     * Calculate the remainder when dividing this value by [other]
     */
    infix fun CPLatticeValue.rem(other: CPLatticeValue): CPLatticeValue {
        if (checkBottom(this, other))
            return CPUndefined(mergeTypes(this.type, other.type))

        if (checkTop(this, other))
            return CPNoConst()

        return (this as CPConstValue<*>).remainder(other as CPConstValue<*>)
    }

    /**
     * Shift this value right by [other]
     */
    infix fun CPLatticeValue.shr(other: CPLatticeValue): CPLatticeValue {
        if (checkBottom(this, other))
            return CPUndefined(mergeTypes(this.type, other.type))

        if (checkTop(this, other))
            return CPNoConst()

        return (this as CPConstValue<*>).shiftRight(other as CPConstValue<*>)
    }

    /**
     * Shift this value right by [other]
     */
    infix fun CPLatticeValue.ushr(other: CPLatticeValue): CPLatticeValue {
        if (checkBottom(this, other))
            return CPUndefined(mergeTypes(this.type, other.type))

        if (checkTop(this, other))
            return CPNoConst()

        return (this as CPConstValue<*>).unsignedShiftRight(other as CPConstValue<*>)
    }

    /**
     * Shift this value left by [other]
     */
    infix fun CPLatticeValue.shl(other: CPLatticeValue): CPLatticeValue {
        if (checkBottom(this, other))
            return CPUndefined(mergeTypes(this.type, other.type))

        if (checkTop(this, other))
            return CPNoConst()

        return (this as CPConstValue<*>).shiftLeft(other as CPConstValue<*>)
    }

    /**
     * Bitwise and of two values
     */
    infix fun CPLatticeValue.and(other: CPLatticeValue): CPLatticeValue {
        if (checkBottom(this, other))
            return CPUndefined(mergeTypes(this.type, other.type))

        if (checkTop(this, other))
            return CPNoConst()

        return (this as CPConstValue<*>).bitwiseAnd(other as CPConstValue<*>)
    }

    /**
     * Bitwise or of two values
     */
    infix fun CPLatticeValue.or(other: CPLatticeValue): CPLatticeValue {
        if (checkBottom(this, other))
            return CPUndefined(mergeTypes(this.type, other.type))

        if (checkTop(this, other))
            return CPNoConst()

        return (this as CPConstValue<*>).bitwiseOr(other as CPConstValue<*>)
    }

    /**
     * Bitwise xor of two values
     */
    infix fun CPLatticeValue.xor(other: CPLatticeValue): CPLatticeValue {
        if (checkBottom(this, other))
            return CPUndefined(mergeTypes(this.type, other.type))

        if (checkTop(this, other))
            return CPNoConst()

        return (this as CPConstValue<*>).bitwiseXor(other as CPConstValue<*>)
    }

    /**
     * Long compare of two long-values
     */
    infix fun CPLatticeValue.lcmp(other: CPLatticeValue): CPLatticeValue {
        if (checkBottom(this, other))
            return CPUndefined(mergeTypes(this.type, other.type))

        if (checkTop(this, other))
            return CPNoConst()

        return (this as CPConstValue<*>).longCompare(other as CPConstValue<*>)
    }

    /**
     * Float compare of two values where NaN produces 1
     */
    infix fun CPLatticeValue.cmpg(other: CPLatticeValue): CPLatticeValue {
        if (checkBottom(this, other))
            return CPUndefined(mergeTypes(this.type, other.type))

        if (checkTop(this, other))
            return CPNoConst()

        return (this as CPConstValue<*>).floatingCompareGreater(other as CPConstValue<*>)
    }

    /**
     * Float compare of two values where NaN produces 1
     */
    infix fun CPLatticeValue.cmpl(other: CPLatticeValue): CPLatticeValue {
        if (checkBottom(this, other))
            return CPUndefined(mergeTypes(this.type, other.type))

        if (checkTop(this, other))
            return CPNoConst()

        return (this as CPConstValue<*>).floatingCompareLesser(other as CPConstValue<*>)
    }

    fun neg(value: CPLatticeValue): CPLatticeValue {
        if (checkBottom(value))
            return CPUndefined(value.type)

        if (checkTop(value))
            return CPNoConst()

        return (value as CPConstValue<*>).negate()
    }
}