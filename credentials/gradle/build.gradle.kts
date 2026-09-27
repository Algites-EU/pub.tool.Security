plugins {
    `java-library`
    `maven-publish`
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
    sourceSets {
        val main by getting {
            java.setSrcDirs(listOf("src/product/java"))
            resources.setSrcDirs(listOf("src/product/resources"))
        }
    }
}

val algitesSecurityLibraryVersion = providers.gradleProperty("algites.security.library.version").orElse("1.0-SNAPSHOT")

dependencies {
    implementation("eu.algites.lib.security:pub.lib.Security_credentials.coreimpl:${algitesSecurityLibraryVersion.get()}")
}
