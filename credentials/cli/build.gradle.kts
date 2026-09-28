plugins {
    application
    `maven-publish`
}

application {
    mainClass.set("eu.algites.tool.security.credentials.cli.AIcCredentialCli")
}
