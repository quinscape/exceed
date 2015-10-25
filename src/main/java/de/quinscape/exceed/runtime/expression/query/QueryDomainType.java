package de.quinscape.exceed.runtime.expression.query;

import com.google.common.collect.ImmutableList;
import de.quinscape.exceed.model.domain.DomainProperty;
import de.quinscape.exceed.model.domain.DomainType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.svenson.JSONProperty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QueryDomainType
{
    private static Logger log = LoggerFactory.getLogger(QueryDomainType.class);

    private final DomainType type;

    private String alias;

    private JoinDefinition joinedType;

    private List<QueryField> fields;

    private List<QueryField> domainTypeFields;

    private QueryDomainType joinedWith;

    private List<QueryField> joinedFields;

    private Map<String, QueryField> fieldMap;


    public QueryDomainType(DomainType type)
    {
        if (type == null)
        {
            throw new IllegalArgumentException("type can't be null");
        }

        this.type = type;

        joinedFields = new ArrayList<>();

        Map<String, DomainProperty> properties = this.getType().getProperties();
        for (DomainProperty domainProperty : properties.values())
        {
            QueryField queryField = new QueryField(this, domainProperty);
            joinedFields.add(queryField);
        }

        domainTypeFields = ImmutableList.copyOf(joinedFields);
    }

    public String getAlias()
    {
        if (alias == null)
        {
            return type.getName();
        }

        return alias;
    }


    public void setAlias(String alias)
    {
        this.alias = alias;
    }


    public DomainType getType()
    {
        return type;
    }


    public JoinDefinition getJoinedType()
    {
        return joinedType;
    }


    public void setJoinedType(JoinDefinition joinedType)
    {
        this.joinedType = joinedType;
    }


    public void setFields(List<String> fields)
    {
        List<QueryField> queryFields = new ArrayList<>();
        for (int i = 0; i < fields.size(); i++)
        {
            String field = fields.get(i);
            if (field.indexOf('.') < 0)
            {
                QueryField queryField = resolveField(field).clone();
                queryField.setLocalName(field);
                queryFields.add(queryField);
            }
            else
            {
                QueryField typeForField = findTypeForField(field);
                if (typeForField == null)
                {
                    throw new IllegalArgumentException("Unknown field '" + field + "'");
                }

                queryFields.add(typeForField);
            }
        }
        this.fields = queryFields;
    }


    private QueryField findTypeForField(String field)
    {
        int dotPos = field.indexOf('.');
        if (dotPos < 0)
        {
            throw new IllegalArgumentException("Unqualified field: " + field);
        }


        QueryDomainType current = this;

        do
        {
            Map<String, QueryField> fields = current.getFields();
            QueryField queryField;
            if ((queryField = fields.get(field)) != null)
            {
                return queryField;
            }

            if (current.getJoinedType() != null)
            {
                current = current.getJoinedType().getRight();
            }
            else
            {
                current = null;
            }

        } while (current != null);

        return null;
    }


    public List<QueryField> getDomainTypeFields()
    {
        return domainTypeFields;
    }


    private QueryField resolveField(final String field)
    {
        QueryDomainType current = this;

        QueryField queryField = null;
        do
        {
            List<QueryField> domainTypeFields = current.getDomainTypeFields();

            for (QueryField f : domainTypeFields)
            {
                if (f.getDomainProperty().getName().equals(field))
                {
                    if (queryField != null)
                    {
                        throw new AmbiguousFieldReferenceException("Ambiguous field reference '" + field + "': exists at least in " +
                            "both "
                            + queryField.getQueryDomainType().getType() + " and " + current.getType());
                    }

                    queryField = f;
                }
            }

            if (current.getJoinedType() != null)
            {
                current = current.getJoinedType().getRight();
            }
            else
            {
                current = null;
            }

        } while (current != null);

        if (queryField == null)
        {
            throw new InvalidFieldReferenceException("Invalid field reference '" + field + "' is not any of the joined domain types");
        }

        return queryField;
    }

    public List<QueryField> getFieldsInOrder()
    {
        return this.fields != null ? this.fields : joinedFields;
    }

    @JSONProperty(ignore = true)
    public Map<String, QueryField> getFields()
    {
        if (fieldMap == null)
        {
            List<QueryField> fields = getFieldsInOrder();
            fieldMap = new HashMap<>();
            for (QueryField field : fields)
            {
                fieldMap.put(field.getLocalName(), field);
            }
        }
        return fieldMap;
    }


    public JoinDefinition join(String name, QueryDomainType other)
    {
        if (joinedType != null)
        {
            throw new IllegalStateException("Already joined with " + joinedType);
        }

        int pos = joinedFields.size();
        QueryDomainType current = other;
        do
        {
            joinedFields.addAll(current.getDomainTypeFields());

            if (current.getJoinedType() != null)
            {
                current = current.getJoinedType().getRight();
            }
            else
            {
                current = null;
            }

        } while (current != null);

        other.seJoinedWith(this);
        joinedType = new JoinDefinition(name, this, other);
        return joinedType;
    }


    @JSONProperty(ignore = true)
    public List<QueryField> getJoinedFields()
    {
        return joinedFields;
    }


    private void seJoinedWith(QueryDomainType queryDomainType)
    {
        this.joinedWith = queryDomainType;
    }


    public QueryDomainType getJoinedWith()
    {
        return joinedWith;
    }


    @Override
    public String toString()
    {
        return super.toString() + ": "
            + "type = " + type.getName()
            + ", alias = '" + alias + '\''
            + ", joinedType = " + joinedType
            + ", fields = " + fields
            + ", domainTypeFields = " + domainTypeFields
            + ", joinedWith = " + (joinedWith != null ? joinedWith.getType().getName() : null)
            ;
    }
}
