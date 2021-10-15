package net.cydhra.acromantula.java.transfomers.analysis.elimination

import com.strobel.assembler.ir.OpCode
import org.apache.logging.log4j.LogManager
import org.objectweb.asm.tree.FrameNode
import org.objectweb.asm.tree.LabelNode
import org.objectweb.asm.tree.LineNumberNode
import org.objectweb.asm.tree.MethodNode
import org.objectweb.asm.tree.analysis.Analyzer

/**
 * An analysis that specializes in the elimination of dead values/variables (that are unused). It does not eliminate
 * dead control flow.
 */
class DeadVariableEliminationAnalysis {

    fun analyzeMethod(owner: String, methodNode: MethodNode) {
        val analyzer = Analyzer(DeadVariableInterpreter)
        analyzer.analyze(owner, methodNode)
        val frames = analyzer.frames

        val instructionIterator = methodNode.instructions.iterator()
        var frameIndex = 0
        while (instructionIterator.hasNext()) {
            val currentInstruction = instructionIterator.next()
            val currentFrame = frames[frameIndex++] ?: continue

            // replace any instruction that does actual work on constants with some pop() and some constant push
            // operations
            if (currentInstruction is LabelNode || currentInstruction is LineNumberNode || currentInstruction is FrameNode) {
                continue
            }

            LogManager.getLogger().debug("==================================")
            LogManager.getLogger().debug("Instruction Frame of \"${OpCode.get(currentInstruction.opcode).name}\"")
            LogManager.getLogger().debug("Frame Locals:")

            (0 until currentFrame.locals).forEach { i ->
                LogManager.getLogger().debug("[LOCAL] $i: ${currentFrame.getLocal(i)}")
            }

            LogManager.getLogger().debug("Frame Stack:")
            (0 until currentFrame.stackSize).forEach { i ->
                LogManager.getLogger().debug("[STACK] $i: ${currentFrame.getStack(i)}")
            }

            // TODO: remove both defined, but not used values with their load, and their consumption. However: since
            //  the analysis is not backwards, we only see defined-but-not-used values before POP() instructions.
            //  dependent values are still USED. So we need to adjust the analysis framework.
        }
    }
}