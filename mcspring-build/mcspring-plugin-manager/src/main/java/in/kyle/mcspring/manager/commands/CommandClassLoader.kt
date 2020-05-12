package `in`.kyle.mcspring.manager.commands

import `in`.kyle.mcspring.command.Command
import `in`.kyle.mcspring.subcommands.plugincommand.api.PluginCommand
import org.springframework.stereotype.Component

@Component
internal class CommandClassLoader {

    @Command(
            value = "classloader",
            description = "Show ClassLoader information for a specific class",
            usage = "/classloader <class>"
    )
    fun classLoader(command: PluginCommand) {
        command.withString()
        command.then(this::executeClassLoader)
        command.otherwise("Usage: /classloader <class>")
    }

    private fun executeClassLoader(clazz: String): String {
        val aClass = Class.forName(clazz)
        val classLoader = aClass.classLoader.toString()
        val protectionDomain = aClass.protectionDomain.codeSource.location.toString()
        return """
            ClassLoader: $classLoader
            Domain: $protectionDomain
        """.trimIndent()
    }
}