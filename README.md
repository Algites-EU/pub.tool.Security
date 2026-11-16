# Algites Security Tools

Security-oriented command-line tools and build-tool integrations for the Algites ecosystem.

> Public Algites project.

---

## 📦 Overview

This repository contains **tooling and integration artifacts** built on top of the reusable security libraries provided by `pub.lib.Security`.

The repository does not own the underlying security business model or reusable resolver implementations. Those belong in `pub.lib.Security`; this repository adapts them to command-line, Gradle, and other tool-specific environments.

---

## 🧱 Modules & Structure

The repository is organized into security tool domains. Each domain may expose several independently distributed tool artifacts.

```text
.
├── README.md
└── credentials/
    ├── README.md
    ├── modustro-artifact-set.yml
    ├── cli/
    │   └── src/
    │       └── product/
    │           └── java/
    └── gradle/
        └── src/
            └── product/
                └── resources/
```

Tool artifacts depend on reusable security functionality instead of duplicating it. The current credential tools use the common credential contracts and implementations from `pub.lib.Security`.

---

## 🚀 Build

The supported repository build entry point is the **Algites Gradle lifecycle**:

```bash
./gradlew algitesBuild
```

Gradle coordinates repository discovery, validation, dependencies, testing, packaging, and publication. Direct technology-specific repository builds are not a supported build mode.

`credentials/cli` depends on the corresponding `pub.lib.Security` credential artifacts. During bootstrap development, the matching library snapshot must therefore already be available through the configured Algites repositories or the Modustro local Maven repository (`~/.modustro/artifact-repos/java/maven`).

---

### Standalone Security bootstrap (1.1)

Security products built with the current generators use the **1.1** release line
(`1.1-SNAPSHOT` for Java and unique `1.1.dev…` snapshot versions for Python).
The previously published **1.0** line is intentionally retained for consumers
built with the old generators; it is not replaced by this bootstrap.

A standalone, descriptor-driven bootstrap is available without loading the
unfinished Modustro Builder or remote Gradle settings plugin:

```bash
MODUSTRO_BOOTSTRAP_DEPENDENCY_MODE=local bash devops/bootstrap/bootstrap.sh --plan-only
MODUSTRO_BOOTSTRAP_DEPENDENCY_MODE=local bash devops/bootstrap/bootstrap.sh --prepare-only
# Only when ready to publish the verified artifacts to Cloudsmith:
MODUSTRO_BOOTSTRAP_DEPENDENCY_MODE=remote bash devops/bootstrap/bootstrap.sh --upload
```

`--prepare-only` runs tests, builds and verifies the Maven publications, promotes
them to `~/.modustro/artifact-repos/java/maven`, builds and checks declared
Python wheels/sdists, and publishes those distributions to the local Python
Simple index under `~/.modustro/artifact-repos/python`. It does not upload to
Cloudsmith. The result manifests are stored under `build/run/bootstrap/<run-id>/`; the legacy `devops/bootstrap.sh` remains an alias.
The explicit modes `remote`, `local` and `source` control dependency sources,
not the publication destination. Both `MODUSTRO_HOME` and
`MODUSTRO_LOCAL_MAVEN_REPOSITORY` can change the local repository location.

`--plan-only` and both publication manifests derive their counts directly from
`modustro-artifact.yml` declarations. Unspecified Java/Python products are **not**
implicitly enabled. The locally staged JARs, POMs, Gradle metadata and source
JARs are verified before promotion. Full integration tests require Gradle 9.2.1
and the required upstream dependency artifacts.

This repository currently declares **two Java products and no Python
products**. In local mode, prepare `pub.lib.Security` 1.1 first; source mode also discovers and prepares the sibling checkout automatically;
`credentials/cli` and `credentials/gradle` require those published APIs.

## 🔄 Continuous Integration (Algites CI)

This repository uses the **Algites unified GitHub Actions CI pipeline**; build, test, and publish rules are centralized.

For exact usage and naming of branches, see:
https://github.com/Algites-EU/pub.gov.Algites.specs/blob/main/ci/Algites-Github-CI-Policy.md

---

## 📥 Usage

Use the tool artifact that corresponds to the integration surface required by the caller. Reusable security APIs and implementations should be consumed from `pub.lib.Security` instead of depending on tool artifacts.

### Credentials

The `credentials` module set exposes the common credential subsystem to command-line and build environments.

#### CLI

`credentials/cli` provides the `algites-credentials` command-line interface backed by the reusable credential libraries from `pub.lib.Security`.

It is intended for explicit credential operations and for bootstrap scenarios in which another tool can invoke an already installed credential resolver.

#### Gradle

`credentials/gradle` contains the bootstrap-safe Gradle credential resolution integration.

The bootstrap resolver remains able to obtain the universal credential document before authenticated repositories are available. During the current migration, `pub.gov.Algites` retains its bootstrap copy until governance builds are switched to consuming the published or explicitly local `pub.tool.Security` artifact.

The Gradle integration is an adapter over the common credential model; reusable credential semantics must remain in `pub.lib.Security` rather than being reimplemented here.

---

## 🛠 Development

Typical workflow:

```bash
git clone https://github.com/Algites-EU/pub.tool.Security.git
cd pub.tool.Security
./gradlew algitesBuild
```

When developing tool artifacts together with unpublished Security library changes, build or publish the corresponding `pub.lib.Security` snapshot first, or make it available through the configured local Algites repository mechanism.

Follow the Algites artifact, source-layout, technology, and naming conventions defined by `pub.gov.Algites`.

---

## 🤝 Contributing

Contributions are welcome.

Please:
- open an issue to discuss changes,
- follow the Algites coding, artifact, source-layout, and naming standards,
- keep tool adapters thin and reusable security logic in `pub.lib.Security`,
- ensure CI passes before submitting a PR.

---

## 📜 License

This project is licensed under the terms of the license specified in the `LICENSE` file. Individual governed content may additionally reference applicable licenses through the repository licensing metadata.

---

## 🌍 About Algites

Algites develops platforms, tools, frameworks, libraries, and applications based on strong governance, modeling, and automation principles.

See:
- https://github.com/Algites-EU/pub.gov.Algites.specs

---

**© Algites**

### Generated source policy during standalone bootstrap

The transitional bootstrap applies the same technology-generated source
policy as `pub.lib.Security`, using the identical
`devops/bootstrap/generated_source_policy.py` helper. This repository has
no configured Defs/SDO generators and currently owns no `.gen` roots;
bootstrap therefore fails if any `src/<branch>/java.gen` or
`src/<branch>/python.gen` directory appears without registering a generator.
Handwritten sources and `config.gen` are unaffected.
