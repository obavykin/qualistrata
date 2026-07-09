package com.bavykin.qualistrata.core.config.merge;

import com.bavykin.qualistrata.core.config.secrets.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Merges layered secrets configuration objects into one effective secrets model.
 *
 * <p>Unlike the old target overlay model, this class merges only secret access
 * profiles. It never merges secrets into {@code TargetConfig}. Later layers win
 * by resource alias and access alias.</p>
 */
public class SecretsConfigMerger {

    /**
     * Creates a secrets config merger.
     */
    public SecretsConfigMerger() {
    }

    /**
     * Merges two secrets configuration layers.
     *
     * @param base base secrets configuration.
     * @param override overriding secrets configuration.
     * @return merged secrets configuration.
     */
    public SecretsConfig merge(SecretsConfig base, SecretsConfig override) {
        if (base == null) {
            return override;
        }
        if (override == null) {
            return base;
        }

        AccessSecretsConfig baseAccess = base.access();
        AccessSecretsConfig overrideAccess = override.access();

        if (baseAccess == null) {
            return override;
        }
        if (overrideAccess == null) {
            return base;
        }

        return new SecretsConfig(new AccessSecretsConfig(
                mergeTyped(baseAccess.ui(), overrideAccess.ui()),
                mergeTyped(baseAccess.unix(), overrideAccess.unix()),
                mergeTyped(baseAccess.databases(), overrideAccess.databases()),
                mergeTyped(baseAccess.api(), overrideAccess.api()),
                mergeTyped(baseAccess.metrics(), overrideAccess.metrics())
        ));
    }

    /**
     * Merges resource/access maps for one resource category.
     *
     * <p>The method treats each access profile as an atomic secret object.
     * Repeating the same resource alias and access alias in an override layer
     * replaces that access profile.</p>
     */
    private <T> Map<String, Map<String, T>> mergeTyped(
            Map<String, Map<String, T>> base,
            Map<String, Map<String, T>> override
    ) {
        if (base == null) {
            return override;
        }
        if (override == null) {
            return base;
        }

        Map<String, Map<String, T>> result = new HashMap<>(base);

        for (Map.Entry<String, Map<String, T>> resourceEntry : override.entrySet()) {
            Map<String, T> baseProfiles = result.get(resourceEntry.getKey());
            Map<String, T> overrideProfiles = resourceEntry.getValue();

            if (baseProfiles == null) {
                result.put(resourceEntry.getKey(), overrideProfiles);
            } else if (overrideProfiles != null) {
                Map<String, T> mergedProfiles = new HashMap<>(baseProfiles);
                mergedProfiles.putAll(overrideProfiles);
                result.put(resourceEntry.getKey(), mergedProfiles);
            }
        }

        return result;
    }
}
