package `in`.kyle.mcspring.subcommands.plugincommand.impl

import org.bukkit.ChatColor
import org.bukkit.command.CommandSender

interface PluginCommandBase {

    val injections: MutableList<Any>
    val parts: MutableList<String>
    val sender: CommandSender
    val completions: MutableList<CompletionStage>
    val runExecutors: Boolean

    var child: PluginCommandBase?
    var state: State

    fun nextPart(): String?

    fun dirtiesState(requiredStates: Array<State> = arrayOf(State.CLEAN),
                     resultingState: State = State.COMPLETED,
                     action: () -> Unit)

    fun sendMessage(message: String) {
        if (message.isNotBlank()) {
            val colored = ChatColor.translateAlternateColorCodes('&', message)
            colored.lines().forEach { sender.sendMessage(it) }
        }
    }

    fun addCompletion(completion: String, type: String) {
        var stage = if (completions.size != 0) completions.last() else addCompletionStage(type, type)
        if (stage.type != type) {
            stage = addCompletionStage(type, type)
        }
        stage.completions.add(completion)
    }

    fun addCompletionStage(name: String, type: String): CompletionStage {
        val stage = CompletionStage(name, type)
        completions.add(stage)
        return stage
    }

    fun execute(e: () -> Unit) {
        if (runExecutors) {
            e()
        }
    }

    data class CompletionStage(val name: String, val type: String) {
        val completions: MutableList<String> = mutableListOf()
    }

    enum class State {
        CLEAN,
        MISSING_ARG,
        COMPLETED
    }
}