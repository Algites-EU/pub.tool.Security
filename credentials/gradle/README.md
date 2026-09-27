# Algites Gradle credential integration

Library-backed Gradle credential resolver packaged as a build-tool artifact.

The integration accepts the complete universal Algites credential document and delegates document parsing, format detection, profile selection, and credential value-source resolution to `pub.lib.Security/credentials/coreimpl`.

Supported complete-document formats are JSON, YAML, and XML. The Gradle adapter does not infer format from a filename; automatic format detection is based on document content. Explicit format-specific parsing remains available through the underlying library API when a caller already knows the format.

The packaged `algites-credential-values.gradle.kts` resource keeps only bootstrap transport concerns:

- use `ALGITES_DEVOPS_BUILD_REPOSITORY_CREDENTIALS` when already materialized by CI;
- outside CI/preflight, optionally obtain the complete stored document from `algites-credentials bootstrap-document`;
- expose `algitesResolveCredentialValue` and `algitesCredentialDocumentAvailable` to Gradle consumers.

Credential-document structure and value-source semantics are not reimplemented in the Gradle script.
