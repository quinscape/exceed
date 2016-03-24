package de.quinscape.exceed.runtime.domain;

import de.quinscape.exceed.model.domain.DomainProperty;
import de.quinscape.exceed.model.domain.DomainType;
import de.quinscape.exceed.runtime.expression.query.QueryDomainType;

public class DefaultNamingStrategy
    implements NamingStrategy
{
    @Override
    public String getTableName(DomainType type)
    {

        return camelCaseToUnderline(type.getName());
    }


    static String camelCaseToUnderline(String name)
    {
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < name.length(); i++)
        {
            char c = name.charAt(i);
            if (Character.isUpperCase(c))
            {
                if (i > 0 && !Character.isUpperCase(name.charAt(i-1)))
                {
                    buf.append("_");
                }
                buf.append(Character.toLowerCase(c));
            }
            else
            {
                buf.append(c);
            }
        }
        return buf.toString();
    }


    @Override
    public String[] getFieldName(String tableName, String propertyName)
    {
        return new String[] { tableName, camelCaseToUnderline(propertyName) };
    }
}
