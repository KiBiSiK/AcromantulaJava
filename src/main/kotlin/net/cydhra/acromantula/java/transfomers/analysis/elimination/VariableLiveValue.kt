package net.cydhra.acromantula.java.transfomers.analysis.elimination

import com.strobel.assembler.ir.OpCode
import org.objectweb.asm.Type
import org.objectweb.asm.tree.AbstractInsnNode
import org.objectweb.asm.tree.analysis.Value

/**
 * A lattice that defines whether a variable is live
 *
 * @param definingInstruction the instruction that defines this value
 * @param state the [LiveState] of this value
 */
class VariableLiveValue(
    var type: Type?,
    var definingInstruction: AbstractInsnNode?,
    var state: LiveState,
) : Value {
    override fun getSize(): Int {
        return if (type == Type.DOUBLE_TYPE || type == Type.LONG_TYPE) 2 else 1
    }

    override fun equals(other: Any?): Boolean {
        if (other !is VariableLiveValue)
            return false

        return this.type == other.type && this.definingInstruction == other.definingInstruction && this.state ==
                other.state
    }

    override fun toString(): String {
        return "LV[$type: $state (by ${definingInstruction?.opcode?.let(OpCode::get)})]"
    }
}

enum class LiveState {
    UNDEFINED, DEFINED, USED
}