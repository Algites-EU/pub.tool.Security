# Algites credential CLI

Cross-platform command-line interface over the reusable credential implementation in `pub.lib.Security`.

Complete credential documents can be stored with:

```text
algites-credentials document-set [<path|url|->]
```

If the source is omitted or `-`, the document is read from standard input. A filesystem path reads a local file. Any JVM-supported URL can be supplied as a URL source. JSON, YAML, and XML credential documents are detected from their content and normalized by the credential library before persistence.

The remaining profile, named-secret, bootstrap, status, and diagnostics commands retain their existing behavior.
