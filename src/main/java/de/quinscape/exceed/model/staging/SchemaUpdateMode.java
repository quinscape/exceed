package de.quinscape.exceed.model.staging;

public enum SchemaUpdateMode
{
    /**
     * Don't do anything. The database DDL must match the domain model on its own.
     */
    NONE,
    /** Don't synch the schema . The database DDL must match the domain model on its own.
     *
     * This mode just dumps the statements the information schema synch would execute into a temp SQL file.
     */
    DUMP,
    /**
     * Update schema to current domain models.
     */
    UPDATE
}
