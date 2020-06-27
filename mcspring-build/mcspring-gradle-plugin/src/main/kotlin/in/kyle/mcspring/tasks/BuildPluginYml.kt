package `in`.kyle.mcspring.tasks

import `in`.kyle.mcspring.McSpringExtension
import `in`.kyle.mcspring.annotation.PluginDepend
import `in`.kyle.mcspring.annotation.SoftPluginDepend
import `in`.kyle.mcspring.commands.dsl.mcspring.Command
import `in`.kyle.mcspring.div
import `in`.kyle.mcspring.getMainSourceSet
import `in`.kyle.mcspring.mcspring
import io.github.classgraph.AnnotationParameterValueList
import io.github.classgraph.ClassGraph
import org.apache.log4j.Logger
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.yaml.snakeyaml.Yaml
import java.net.URLClassLoader
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.reflect.KClass

open class BuildPluginYml : DefaultTask() {

    private val logger = Logger.getLogger(BuildPluginYml::class.java)
    private val attributes = mutableMapOf<String, Any>()

    @TaskAction
    fun buildYml() {
        val props = project.extensions.mcspring
        logInfo(props)

        props.apply {
            writeMainAttributes()

            fun writeNonNull(key: String, value: Any?) = value?.apply { attributes[key] = this }

            writeNonNull("description", pluginDescription)
            writeNonNull("load", pluginLoad?.toString()?.toLowerCase())
            writeNonNull("author", pluginAuthor)
            writeNonNull("authors", pluginAuthors)
            writeNonNull("website", pluginWebsite)
            writeNonNull("database", pluginDatabase)
            writeNonNull("prefix", pluginPrefix)
            writeNonNull("loadbefore", pluginLoadBefore)
        }

        project.getMainSourceSet().runtimeClasspath.files
                .apply {
                    val classLoader = URLClassLoader(this.map { it.toURI().toURL() }.toTypedArray())
                    addDependencies(classLoader)
                    addCommands(classLoader)
                    addSpringBootMain(classLoader)
                    classLoader.close()
                }

        writeYmlFile()
    }

    private fun McSpringExtension.writeMainAttributes() {
        val name = pluginName ?: project.name
        val version = pluginVersion ?: project.version
        if (pluginMainPackage == null) {
            pluginMainPackage = "${project.group}.${project.name}"
            logger.info("No main package specified, using $pluginMainPackage")
        }
        val main = "$pluginMainPackage.SpringJavaPlugin"

        if (!name.matches("[a-zA-Z0-9_-]+".toRegex())) {
            throw GradleException("""
                Invalid plugin name: $name
                The plugin name must consist of all alphanumeric characters and underscores (a-z,A-Z,0-9,_)
                
                Update the project name
                OR
                Update the generated name by setting the `pluginName` in the mcspring extension block.
            """.trimIndent())
        }

        val pattern = "(\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*\\.)*\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*"
        if (!main.matches(pattern.toRegex())) {
            throw GradleException("""
                Invalid main class location: $main
                Refer to the Java specification for valid package/class names.
                The main class location is set using: {project group}.{project location}.SpringJavaPlugin
                
                Update the project group and project name
                OR
                Update the generated main package by setting the `pluginMainPackage` in the mcspring extension block.
            """.trimIndent())
        }

        attributes["name"] = name
        attributes["version"] = version
        attributes["main"] = main
    }

    private fun writeYmlFile() {
        val outputFile = project.buildDir / "resources" / "main"
        outputFile.mkdirs()
        val pluginYml = outputFile / "plugin.yml"
        logger.info("Building to $pluginYml")

        val formatter = DateTimeFormatter.ofPattern("YYYY-MM-dd HH:mm:SS")
        val nowFormatted = LocalDateTime.now().format(formatter)
        if (pluginYml.exists()) pluginYml.delete()
        pluginYml.createNewFile()
        pluginYml.appendText("""
                # File auto generated by mcspring on $nowFormatted
                # https://github.com/kylepls/mcspring
                
            """.trimIndent())
        pluginYml.appendText(Yaml().dumpAsMap(attributes))
    }

    private fun addDependencies(classLoader: ClassLoader) {
        val scanResult = ClassGraph()
                .overrideClassLoaders(classLoader)
                .enableAnnotationInfo()
                .scan()
        scanResult.use {
            fun getPluginDependencies(annotation: String) =
                    scanResult.allClasses.filter { it.isStandardClass && it.hasAnnotation(annotation) }
                            .map { it.getAnnotationInfo(annotation).parameterValues }
                            .flatMap { (it["plugins"].value as Array<String>).toList() }

            fun addAnnotationAttributeList(string: String, clazz: KClass<*>) =
                    getPluginDependencies(clazz.qualifiedName!!)
                            .takeIf { it.isNotEmpty() }
                            ?.apply { attributes[string] = this }

            addAnnotationAttributeList("softdepend", SoftPluginDepend::class)
            addAnnotationAttributeList("depend", PluginDepend::class)
        }
    }

    private fun addCommands(classLoader: ClassLoader) {
        val scanResult = ClassGraph()
                .overrideClassLoaders(classLoader)
                .enableAnnotationInfo()
                .enableMethodInfo()
                .enableClassInfo()
                .scan()

        val annotations = scanResult.use {
            fun getAnnotations(annotation: KClass<*>): List<AnnotationParameterValueList> {
                val methods = scanResult.getClassesWithMethodAnnotation(annotation.qualifiedName!!)
                        .flatMap { it.methodInfo.filter { it.hasAnnotation(annotation.qualifiedName!!) } }
                val annotations =
                        methods.map { it.getAnnotationInfo(annotation.qualifiedName!!) }
                                .map { it.parameterValues }
                return annotations
            }

            getAnnotations(Command::class)
        }

        val commands = annotations.map {
            val meta = mutableMapOf<String, Any>()

            meta["description"] = it.getValue("description")
            meta["aliases"] = it.getValue("aliases")
            meta["permission"] = it.getValue("permission")
            meta["permission-message"] = it.getValue("permissionMessage")
            meta["usage"] = it.getValue("usage")

            val name = it["value"].value
            val filteredMeta = meta.filterValues { value ->
                when (value) {
                    is Array<*> -> value.isNotEmpty()
                    is String -> value.isNotEmpty()
                    else -> true
                }
            }
            Pair(name, filteredMeta)
        }.associate { it }

        if (commands.isNotEmpty()) {
            attributes["commands"] = commands
        }
    }

    private fun addSpringBootMain(classLoader: ClassLoader) {
        val scanResult = ClassGraph()
                .overrideClassLoaders(classLoader)
                .enableAnnotationInfo()
                .scan(4)
        val mains = scanResult.use {
            scanResult
                    .allStandardClasses
                    .filter { it.hasAnnotation(SpringBootApplication::class.qualifiedName) }
                    .map { it.name }
        }
        require(mains.size == 1) {
            """
                There should be 1 main class on the classpath: $mains
                Make sure to annotate a class with @SpringPlugin
                This serves as an entry point for mcspring.
            """.trimIndent()
        }
        attributes["spring-boot-main"] = mains.first()
    }

    private fun logInfo(extension: McSpringExtension) {
        extension.apply {
            logger.info("Spigot Version: $spigotVersion")
            logger.info("Spigot Download URL: $spigotDownloadUrl")
            logger.info("Spigot Directory: $spigotDirectory")
            logger.info("Plugin Main Package: $pluginMainPackage")
            logger.info("Project Name: ${project.name}")
            logger.info("Project Version: ${project.version}")
            logger.info("Project Group: ${project.group}")
            logger.info("Plugin Name: $pluginName")
            logger.info("Plugin Version: $pluginVersion")
            logger.info("Plugin Description: $pluginDescription")
            logger.info("Plugin Load: $pluginLoad")
            logger.info("Plugin Author: $pluginAuthor")
            logger.info("Plugin Authors: $pluginAuthors")
            logger.info("Plugin Website: $pluginWebsite")
            logger.info("Plugin Database: $pluginDatabase")
            logger.info("Plugin Prefix: $pluginPrefix")
            logger.info("Plugin Load Before: $pluginLoadBefore")
        }
    }
}
