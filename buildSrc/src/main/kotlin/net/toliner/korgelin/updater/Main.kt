package net.toliner.korgelin.updater

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.runBlocking
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import kotlin.system.exitProcess

@KtorExperimentalAPI
val client = HttpClient(CIO)

val urlList = listOf(
        "http://central.maven.org/maven2/org/jetbrains/kotlin/kotlin-stdlib/maven-metadata.xml",
        "http://central.maven.org/maven2/org/jetbrains/annotations/maven-metadata.xml",
        "http://central.maven.org/maven2/org/jetbrains/kotlinx/kotlinx-coroutines-core/maven-metadata.xml",
        "https://bintray.com/kotlin/kotlinx/download_file?file_path=org%2Fjetbrains%2Fkotlinx%2Fkotlinx-serialization-runtime%2Fmaven-metadata.xml"
)

val urlToName = mapOf(
        urlList[0] to "kotlinVersion",
        urlList[1] to "annotationsVersion",
        urlList[2] to "coroutineVersion",
        urlList[3] to "serializationVersion"
)

val regex = "[0-9.]+".toRegex()

lateinit var configFile: File

@KtorExperimentalAPI
fun main() {
    val currentConfigMap = analyzeConfigScript()
    val newConfigMap = currentConfigMap.toMutableMap()
    val currentVersionMap = getCurrentVersion(currentConfigMap)

    println("Current: ${currentConfigMap.toList().joinToString()}")

    /*
    configFile.delete()
    configFile.createNewFile()
     */
    urlList.map {
        (urlToName[it] ?: error("urlToName map is illegal.")) to runBlocking { client.get<String>(it) }
    }.forEach { pair ->
        val propName = pair.first
        val xmlString = pair.second
        val doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(xmlString.byteInputStream())
        val latestVersionString = doc.getElementsByTagName("release").item(0).textContent!!
        val latestVersion = if (!latestVersionString.matches(regex)) {
            doc.getElementsByTagName("version").asList()
                    .asSequence()
                    .filter { it.textContent.matches(regex) }
                    .map { Version(it.textContent) }
                    .sorted()
                    .last()
        } else Version(latestVersionString)
        val currentVersion = currentVersionMap[propName] ?: error("propName is illegal.")
        if (currentVersion < latestVersion) {
            newConfigMap[propName] = latestVersion.toString()
        }
    }

    val file = File(".kotlinUpdate")
    file.createNewFile()
    if (Version(currentConfigMap["kotlinVersion"] ?: error("Hello, World!")) < Version(newConfigMap["kotlinVersion"]
                    ?: error("WTF !?!?"))) {
        file.writeText("true")
        val script = buildString {
            newConfigMap.forEach { (k, v) ->
                append("extra[\"")
                append(k)
                append("\"] = \"")
                append(v)
                appendln('"')
            }
        }.removeSuffix("\n")

        configFile.writeText(script)
        println("New: ${newConfigMap.toList().joinToString()}")
    } else {
        file.writeText("false")
        println("No kotlin update.")
    }
    exitProcess(0)
}

fun NodeList.asList(): List<Node> {
    val list = mutableListOf<Node>()
    for (i in 1..this.length) {
        list.add(item(i - 1))
    }
    return list
}

fun analyzeConfigScript(): Map<String, String> {
    return File(".").walkTopDown()
            .filter { it.name == "config.gradle.kts" }
            .single()
            .apply {
                configFile = this
            }
            .readText()
            .lines()
            .filterNot { it.isEmpty() || it.isBlank() }
            .map { it.split('=') }
            .map {
                it[0].split('"')[1] to it[1].split('"')[1]
            }.toMap()
}

fun getCurrentVersion(configMap: Map<String, String>): Map<String, Version> {
    return configMap
            .filterValues {
                it.matches(regex)
            }
            .mapValues {
                Version(it.value)
            }
}