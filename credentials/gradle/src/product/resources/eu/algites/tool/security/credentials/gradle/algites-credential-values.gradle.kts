/*
 * Algites universal credential value resolver.
 *
 * Credential document parsing and value-source semantics are implemented by pub.lib.Security.
 * This script only obtains the complete document and exposes a Gradle-friendly resolver function.
 */

import eu.algites.tool.security.credentials.gradle.AIcGradleCredentialDocumentResolver
import java.io.File

val locAlgitesCredentialPreflight = System.getenv("_TMP_ALGITES_CREDENTIAL_PREFLIGHT")
    ?.equals("true", ignoreCase = true) == true
val locAlgitesCredentialCi = System.getenv("CI")
    ?.equals("true", ignoreCase = true) == true

fun AIcAlgitesFindExecutable(aExecutable: String): String? {
    val locExecutable = aExecutable.trim()
    if (locExecutable.isBlank()) return null

    val locDirectFile = File(locExecutable)
    if (locDirectFile.isAbsolute || locExecutable.contains('/') || locExecutable.contains('\\')) {
        return locDirectFile.takeIf { it.isFile }?.absolutePath
    }

    val locPath = System.getenv("PATH")?.takeIf { it.isNotBlank() } ?: return null
    val locIsWindows = System.getProperty("os.name").lowercase().contains("win")
    val locExtensions = if (locIsWindows) {
        val locPathExt = System.getenv("PATHEXT")
            ?.split(';')
            ?.map { it.trim() }
            ?.filter { it.isNotBlank() }
            .orEmpty()
        listOf("") + locPathExt
    } else {
        listOf("")
    }

    locPath.split(File.pathSeparatorChar).forEach { locDirectory ->
        if (locDirectory.isBlank()) return@forEach
        locExtensions.forEach { locExtension ->
            val locCandidate = File(locDirectory, locExecutable + locExtension)
            if (locCandidate.isFile) return locCandidate.absolutePath
        }
    }
    return null
}

val locAlgitesReadCredentialCliOutput = fun(aArguments: List<String>): String? {
    val locConfiguredExecutable = System.getenv("ALGITES_CREDENTIAL_CLI")
        ?.trim()
        ?.takeIf { it.isNotBlank() }
        ?: "algites-credentials"
    val locExecutable = AIcAlgitesFindExecutable(locConfiguredExecutable) ?: return null
    val locCommand = mutableListOf(locExecutable)
    locCommand.addAll(aArguments)
    val locOutput = try {
        providers.exec {
            commandLine(locCommand)
            isIgnoreExitValue = true
        }
    } catch (_: Exception) {
        return null
    }
    val locResult = try {
        locOutput.result.get()
    } catch (_: Exception) {
        return null
    }
    if (locResult.exitValue != 0) return null
    return try {
        locOutput.standardOutput.asText.get()
    } catch (_: Exception) {
        null
    }
}

val locAlgitesCredentialDocumentRaw = System.getenv("ALGITES_DEVOPS_BUILD_REPOSITORY_CREDENTIALS")
    ?.takeIf { it.isNotBlank() }
    ?: if (locAlgitesCredentialPreflight || locAlgitesCredentialCi) {
        null
    } else {
        locAlgitesReadCredentialCliOutput(listOf("bootstrap-document"))
    }

val locAlgitesCredentialDocumentResolver = locAlgitesCredentialDocumentRaw
    ?.takeIf { it.isNotBlank() }
    ?.let { locDocument ->
        AIcGradleCredentialDocumentResolver(
            locDocument,
            System.getenv(),
            rootProject.projectDir.toPath(),
            locAlgitesCredentialPreflight
        )
    }

val locAlgitesResolveCredentialValue = fun(
    aProfileId: String,
    aCredentialType: String,
    aField: String,
    aBaseDirectory: File
): String? {
    val locDocument = locAlgitesCredentialDocumentRaw ?: return null
    val locResolver = if (aBaseDirectory.toPath().toAbsolutePath().normalize() == rootProject.projectDir.toPath().toAbsolutePath().normalize()) {
        locAlgitesCredentialDocumentResolver
    } else {
        AIcGradleCredentialDocumentResolver(
            locDocument,
            System.getenv(),
            aBaseDirectory.toPath(),
            locAlgitesCredentialPreflight
        )
    }
    return locResolver?.resolveValue(aProfileId, aCredentialType, aField)
}

extra["algitesResolveCredentialValue"] = locAlgitesResolveCredentialValue
extra["algitesCredentialDocumentAvailable"] = locAlgitesCredentialDocumentResolver?.isAvailable() == true
