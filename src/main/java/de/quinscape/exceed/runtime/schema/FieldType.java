package de.quinscape.exceed.runtime.schema;

public enum FieldType
{
    TEXT,
    CHARACTER_VARYING,
    INTEGER,
    BOOLEAN,
    BIGINT,
    REAL,
    DOUBLE_PRECISION,
    TIMESTAMP_WITHOUT_TIME_ZONE,
    DATE;

    private final String sqlName;


    private FieldType()
    {
        this.sqlName = this.name().replace('_', ' ').toLowerCase();
    }


    public String getSqlName()
    {
        return sqlName;
    }

    public String getSqlWithMax(int maxLength)
    {
        if (this != CHARACTER_VARYING || maxLength <= 0)
        {
            return getSqlName();
        }

        return sqlName + "(" + maxLength + ")";
    }
}
