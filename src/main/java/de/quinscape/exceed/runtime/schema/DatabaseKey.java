package de.quinscape.exceed.runtime.schema;

public class DatabaseKey
{
    public final String name, column;
    public final boolean fk;

    public DatabaseKey(String name, String column, boolean fk)
    {
        this.name = name;
        this.column = column;
        this.fk = fk;
    }
}
