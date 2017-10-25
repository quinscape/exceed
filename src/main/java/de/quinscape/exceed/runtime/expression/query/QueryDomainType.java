package de.quinscape.exceed.runtime.expression.query;

import de.quinscape.exceed.model.domain.property.DomainProperty;
import de.quinscape.exceed.model.domain.type.DomainType;
import de.quinscape.exceed.runtime.component.QueryPreparationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.svenson.JSONProperty;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class QueryDomainType
{
    private final static Logger log = LoggerFactory.getLogger(QueryDomainType.class);

    private final DomainType type;

    private String alias;

    private JoinDefinition joinedType;

    private List<String> selectedFieldNames;

    private QueryDomainType joinedWith;

    private List<DataField> allFields;

    private Map<String, DataField> fieldMap;

    private List<DataField> selectedFields;

    private List<String> originalFields;

    private Map<String, QueryMapping> queryMappings;


    public QueryDomainType(DomainType type)
    {
        if (type == null)
        {
            throw new IllegalArgumentException("type can't be null");
        }

        this.type = type;

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


    public void selectedFields(List<String> selectedFields)
    {
        this.getLeftMost().selectedFieldNames = selectedFields;
    }

    public DataField resolveField(final String originalField)
    {
        final DataField field = findField(originalField);

        if (field == null)
        {
            throw new IllegalArgumentException("Unknown field '" + originalField + "'");
        }

        return field;
    }


    public DataField findField(String originalField)
    {
        final int dotPos = originalField.indexOf('.');
        final String prefix;
        final String field;
        if (dotPos >= 0)
        {
            prefix = originalField.substring(0, dotPos);
            field = originalField.substring(dotPos + 1);
        }
        else
        {
            prefix = null;
            field = originalField;
        }

        DataField dataField = null;
        QueryDomainType current = this;
        while (current != null)
        {
            for (DomainProperty domainProperty : current.getType().getProperties())
            {
                if (
                    matches(domainProperty, current.getNameOrAlias(), prefix, field)
                )
                {
                    if (dataField != null)
                    {
                        throw new AmbiguousFieldReferenceException("Ambiguous field reference '" + originalField + "': exists at least in " +
                            "both\n"
                            + dataField.getQueryDomainType().getType() + "\nand\n" + current.getType());
                    }
                    dataField = new DataField(current, domainProperty);
                    dataField.setLocalName(originalField);

                    if (prefix != null)
                    {
                        // no uniqueness check necessary
                        return dataField;
                    }
                }
            }

            current = nextJoinedType(current);
        }
        return dataField;
    }


    public static QueryDomainType nextJoinedType(QueryDomainType current)
    {
        if (current.getJoinedType() != null)
        {
            return current.getJoinedType().getRight();
        }
        else
        {
            return null;
        }
    }


    public static boolean matches(DomainProperty domainProperty, String nameOrAlias, String prefix, String field)
    {
        return domainProperty.getName().equals(field) &&
            (
                prefix == null || prefix.equals(nameOrAlias)
            );
    }


    public List<DataField> getFieldsInOrder()
    {
        return this.selectedFieldNames != null ? this.selectedFields : allFields;
    }


    public List<String> getSelectedFieldNames()
    {
        return selectedFieldNames;
    }


    @JSONProperty(ignore = true)
    public Map<String, DataField> getSelectedFields()
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

        other.seJoinedWith(this);
        joinedType = new JoinDefinition(name, this, other);
        return joinedType;
    }


    @JSONProperty(ignore = true)
    public List<DataField> getJoinedFields()
    {
        return allFields;
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
            + ", selectedFieldNames = " + selectedFieldNames
            + ", joinedWith = " + (joinedWith != null ? joinedWith.getType().getName() : null)
            ;
    }


    public QueryDomainType resolve()
    {

        this.allFields = collectAllJoinedFields();

        if (selectedFieldNames != null)
        {
            final ResolvedSelection resolved = resolveSelection(selectedFieldNames);

            this.selectedFields = resolved.dataFields;
            originalFields = selectedFieldNames;
            selectedFieldNames  = resolved.expandedSelection;

            log.info("originalFields: {}, selectedFieldNames: {}", originalFields, selectedFieldNames);
        }
        else
        {
            this.originalFields = Collections.emptyList();
            this.selectedFields = Collections.emptyList();
        }

        return this;
    }


    private ResolvedSelection resolveSelection(List<String> selectedFieldNames)
    {
        final List<DataField> dataFields = new ArrayList<>();

        final List<String> expandedSelection = new ArrayList<>();

        for (String selectedField : selectedFieldNames)
        {
            final DataField dataField = findField( selectedField);

            if (dataField != null)
            {
                expandedSelection.add(selectedField);
                dataFields.add(dataField);
            }
            else
            {
                QueryDomainType queryDomainType = null;
                List<Integer> columns = new ArrayList<>();
                for (int i = 0; i < allFields.size(); i++)
                {
                    DataField curField = allFields.get(i);
                    if (curField.getQueryDomainType().getNameOrAlias().equals(selectedField))
                    {
                        dataFields.add(curField);
                        columns.add(i);
                        expandedSelection.add(curField.getQualifiedName());
                        queryDomainType = curField.getQueryDomainType();
                    }
                }

                if (queryDomainType == null)
                {
                    throw new QueryPreparationException("Could not find any columns matching '" + selectedField + "'");
                }

                if (queryMappings == null)
                {
                    queryMappings = new HashMap<>();
                }

                queryMappings.put(selectedField,
                    new QueryMapping(
                        queryDomainType.getType().getName(),
                        selectedField,
                        columns,
                        queryDomainType.getNameOrAlias()
                    )
                );

            }
        }

        QueryDomainType current = this;
        while (current != null)
        {
            final JoinDefinition joinDefinition = current.getJoinedType();
            if (joinDefinition != null)
            {
                for (DataField dataField : joinDefinition.getCondition().getFieldReferences())
                {
                    if (!dataField.getDomainProperty().getName().equals(DomainType.ID_PROPERTY))
                    {
                        expandedSelection.add(dataField.getQualifiedName());
                        dataFields.add(dataField);
                    }
                }
            }

            current = nextJoinedType(current);
        }


        return new ResolvedSelection(dataFields, expandedSelection);
    }




    private List<DataField> collectAllJoinedFields()
    {
        final List<DataField> fields = getFieldsFor(this);

        if (joinedType != null)
        {
            QueryDomainType current = joinedType.getRight();
            while (current != null)
            {
                fields.addAll(getFieldsFor(current));
                current = nextJoinedType(current);

            }

        }
        return fields;
    }


    public List<String> getOriginalFields()
    {
        return selectedFieldNames != null ? originalFields : allFields.stream().map( DataField::getQualifiedName ).collect(Collectors.toList());
    }


    private static List<DataField> getFieldsFor(QueryDomainType queryDomainType)
    {
        List<DataField> fields = new ArrayList<>();

        List<DomainProperty> properties = queryDomainType.getType().getProperties();
        for (DomainProperty domainProperty : properties)
        {
            fields.add(
                new DataField(
                    queryDomainType,
                    domainProperty
                )
            );
        }

        return fields;
    }                                                               


    public QueryDomainType getLeftMost()
    {
        QueryDomainType current = this;

        QueryDomainType next;
        while ((next = current.getJoinedWith()) != null)
        {
            current = next;
        }

        return current;
    }


    public Map<String, QueryMapping> getQueryMappings()
    {
        if (queryMappings == null)
        {
            return Collections.emptyMap();
        }

        return queryMappings;
    }


    private static class ResolvedSelection
    {
        public final List<DataField> dataFields;
        public final List<String> expandedSelection;

        public ResolvedSelection(List<DataField> dataFields, List<String> expandedSelection)
        {
            this.dataFields = dataFields;
            this.expandedSelection = expandedSelection;
        }
    }
}
