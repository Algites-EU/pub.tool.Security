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
            java.setSrcDirs(emptyList<String>())
            resources.setSrcDirs(listOf("src/product/resources"))
        }
    }
}
