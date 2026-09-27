package eu.algites.tool.security.credentials.gradle;

import eu.algites.lib.security.credentials.core.AIcCredentialDocument;
import eu.algites.lib.security.credentials.core.AIcCredentialDocumentProvider;
import eu.algites.lib.security.credentials.core.AIcCredentialDocumentReader;
import eu.algites.lib.security.credentials.core.AIcCredentialProfile;
import eu.algites.lib.security.credentials.core.AIcCredentialValueReference;
import eu.algites.lib.security.credentials.core.AInCredentialField;
import eu.algites.lib.security.credentials.core.AInCredentialType;
import eu.algites.lib.security.credentials.core.AInCredentialValueSource;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Library-backed adapter for resolving one complete Algites credential document from Gradle.
 */
public final class AIcGradleCredentialDocumentResolver {
    private final AIcCredentialDocument document;
    private final AIcCredentialDocumentProvider provider;
    private final boolean preflight;

    public AIcGradleCredentialDocumentResolver(
        String aDocument,
        Map<String, String> aEnvironment,
        Path aBaseDirectory,
        boolean aPreflight
    ) {
        Objects.requireNonNull(aDocument, "Credential document must not be null.");
        Objects.requireNonNull(aEnvironment, "Environment must not be null.");
        Objects.requireNonNull(aBaseDirectory, "Base directory must not be null.");
        document = new AIcCredentialDocumentReader().read(aDocument);
        provider = new AIcCredentialDocumentProvider(aEnvironment, aBaseDirectory);
        preflight = aPreflight;
    }

    public boolean isAvailable() {
        return !document.getProfileIds().isEmpty();
    }

    public boolean containsProfile(String aProfileId) {
        return document.containsProfile(aProfileId);
    }

    public String resolveValue(String aProfileId, String aCredentialType, String aField) {
        AInCredentialType locType = AInCredentialType.fromId(aCredentialType);
        AInCredentialField locField = AInCredentialField.fromId(aField);
        Optional<Map<AInCredentialField, AIcCredentialValueReference>> locReferences =
            document.getCredentialValues(aProfileId, locType);
        if (locReferences.isEmpty()) {
            return null;
        }
        AIcCredentialValueReference locReference = locReferences.get().get(locField);
        if (locReference == null) {
            return null;
        }
        if (preflight && locReference.getSource() == AInCredentialValueSource.SECRET_CONTENT) {
            return null;
        }

        AIcCredentialProfile locProfile = new AIcCredentialProfile(aProfileId, locType);
        Optional<char[]> locChars = provider.resolveField(document, locProfile, locField);
        if (locChars.isEmpty()) {
            return null;
        }
        char[] locCopy = locChars.get();
        try {
            return new String(locCopy);
        } finally {
            Arrays.fill(locCopy, '\0');
        }
    }

    public Map<String, String> resolveProfile(String aProfileId, String aCredentialType) {
        AInCredentialType locType = AInCredentialType.fromId(aCredentialType);
        LinkedHashMap<String, String> locValues = new LinkedHashMap<>();
        for (AInCredentialField locField : locType.getSupportedFields()) {
            String locValue = resolveValue(aProfileId, locType.getId(), locField.getId());
            if (locValue != null) {
                locValues.put(locField.getId(), locValue);
            }
        }
        return Map.copyOf(locValues);
    }
}
