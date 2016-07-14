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
    private final static Logger log = LoggerFactory.getLogger(QueryDomainType.class);

    private final DomainType type;

    private String alias;

    private JoinDefinition joinedType;

    private List<DataField> fields;

    private List<DataField> domainTypeFields;

    private QueryDomainType joinedWith;

    private List<DataField> joinedFields;

    private Map<String, DataField> fieldMap;


    public QueryDomainType(DomainType type)
    {
        if (type == null)
        {
            throw new IllegalArgumentException("type can't be null");
        }

        this.type = type;

        joinedFields = new ArrayList<>();

        List<DomainProperty> properties = this.getType().getProperties();
        for (DomainProperty domainProperty : properties)
        {
            DataField dataField = new DataField(this, domainProperty);
            joinedFields.add(dataField);
        }

        domainTypeFields = ImmutableList.copyOf(joinedFields);
    }



    public String getNameOrAlias()
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


    public String getAlias()
    {
        return alias;
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
        List<DataField> dataFields = new ArrayList<>();
        for (int i = 0; i < fields.size(); i++)
        {
            String field = fields.get(i);
            if (field.indexOf('.') < 0)
            {
                DataField dataField = resolveField(field).clone();
                dataField.setLocalName(field);
                dataFields.add(dataField);
            }
            else
            {
                DataField typeForField = findTypeForField(field);
                if (typeForField == null)
                {
                    throw new IllegalArgumentException("Unknown field '" + field + "'");
                }

                dataFields.add(typeForField);
            }
        }
        this.fields = dataFields;
    }


    private DataField findTypeForField(String field)
    {
        int dotPos = field.indexOf('.');
        if (dotPos < 0)
        {
            throw new IllegalArgumentException("Unqualified field: " + field);
        }


        QueryDomainType current = this;

        do
        {
            Map<String, DataField> fields = current.getFields();
            DataField dataField;
            if ((dataField = fields.get(field)) != null)
            {
                return dataField;
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


    public List<DataField> getDomainTypeFields()
    {
        return domainTypeFields;
    }


    public DataField resolveField(final String field)
    {
        QueryDomainType current = this;

        DataField dataField = null;
        do
        {
            List<DataField> domainTypeFields = current.getDomainTypeFields();

            for (DataField f : domainTypeFields)
            {
                if (f.getDomainProperty().getName().equals(field))
                {
                    if (dataField != null)
                    {
                        throw new AmbiguousFieldReferenceException("Ambiguous field reference '" + field + "': exists at least in " +
                            "both\n"
                            + dataField.getQueryDomainType().getType() + "\nand\n" + current.getType());
                    }

                    dataField = f;
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

        if (dataField == null)
        {
            throw new InvalidFieldReferenceException("Invalid field reference '" + field + "' is not in any of the joined domain types");
        }

        return dataField;
    }

    public List<DataField> getFieldsInOrder()
    {
        return this.fields != null ? this.fields : joinedFields;
    }

    @JSONProperty(ignore = true)
    public Map<String, DataField> getFields()
    {
        if (fieldMap == null)
        {
            List<DataField> fields = getFieldsInOrder();
            fieldMap = new HashMap<>();
            for (DataField field : fields)
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
    public List<DataField> getJoinedFields()
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
