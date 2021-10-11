package net.cydhra.acromantula.java.view.disassembly

abstract class Assembly {

    /**
     * Checks whether the [flag] is set in [access] and if so, executes [code].
     */
    protected inline fun <T> T.checkFlag(access: Int, flag: Int, code: T.() -> Unit) {
        if (access and flag > 0)
            this.apply(code)
    }

    /**
     * Checks whether the [flag] is set in [access] and if so, executes [code], otherwise executes [otherwise].
     */
    protected inline fun <T> T.checkFlagElse(access: Int, flag: Int, code: T.() -> Unit, otherwise: T.() -> Unit) {
        if (access and flag > 0)
            this.apply(code)
        else
            this.apply(otherwise)
    }
}