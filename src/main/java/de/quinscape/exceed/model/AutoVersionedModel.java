package de.quinscape.exceed.model;

import org.svenson.JSONProperty;

import java.util.UUID;

/**
 * Implemented by classes taking part in the editing versioning system.
 * <p>
 *     Versioning depends on each object having two different unique id values. Humans have a hard time working with
 *     uuids, so the modeling interface works with clear text names. But we need to track different versions and allow
 *     concurrent editing.
 *
 *     The identity guid represents the identity of an object over its entire lifetime, similar to primary keys in a database.
 *     The version changes every time the model changes.
 *
 * </p>
 *
 */
public interface AutoVersionedModel
{
    /**
     * Returns the version guid for this model. We only use this property in-memory and do not
     *
     * @return version guid which changes every time the model is read from disk.
     */
    String getVersionGUID();

    void setVersionGUID(String version);

    /**
     * Sets the identity guid of this model.
     *
     * @return globally unique id expected to never change once set.
     */
    String getIdentityGUID();

    /**
     * Sets the identity guid of this model.
     *
     * @param identity  globally unique id expected to never change once set.
     */
    void setIdentityGUID(String identity);

    default void initializeGUIDs()
    {
        final String identityGUID = this.getIdentityGUID();
        if (identityGUID == null)
        {
            this.setIdentityGUID(UUID.randomUUID().toString());
        }

        final String versionGUID = this.getVersionGUID();
        if (versionGUID == null)
        {
            this.setVersionGUID(UUID.randomUUID().toString());
        }
    }
}
