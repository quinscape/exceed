package de.quinscape.exceed.runtime.domain;

import de.quinscape.exceed.expression.Node;
import de.quinscape.exceed.expression.SimpleNode;
import de.quinscape.exceed.model.domain.DomainType;
import de.quinscape.exceed.runtime.component.QueryExecutor;
import de.quinscape.exceed.runtime.component.DataList;
import de.quinscape.exceed.runtime.expression.query.JoinDefinition;
import de.quinscape.exceed.runtime.expression.query.QueryDefinition;
import de.quinscape.exceed.runtime.expression.query.QueryDomainType;
import de.quinscape.exceed.runtime.expression.query.DataField;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Record1;
import org.jooq.RecordMapper;
import org.jooq.SelectJoinStep;
import org.jooq.SortField;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static org.jooq.impl.DSL.*;

/**
 * JOOQ-based query executor.
 *
 */
@Transactional
public class JOOQQueryExecutor
    implements QueryExecutor
{
    private final DSLContext dslContext;

    private final NamingStrategy namingStrategy;

    public JOOQQueryExecutor(DSLContext dslContext, NamingStrategy namingStrategy)
    {
        this.dslContext = dslContext;
        this.namingStrategy = namingStrategy;
    }


    @Override
    public DataList execute(QueryDefinition queryDefinition)
    {
        QueryDomainType queryDomainType = queryDefinition.getQueryDomainType();

        SelectJoinStep<Record> builder = dslContext.select(
                queryDomainType.getFieldsInOrder().stream()
                    .map(this::jooqField)
                    .collect(Collectors.toList())
            )
            .from(jooqTableFor(queryDomainType));

        SelectJoinStep<Record1<Integer>> countBuilder = null;

        boolean pagedQuery = queryDefinition.getLimit() > 0;
        if (pagedQuery)
        {
            countBuilder = dslContext.selectCount().from(jooqTableFor(queryDomainType));
        }
        JoinDefinition joinedType;
        while ((joinedType = queryDomainType.getJoinedType()) != null)
        {
            Condition joinCondition = createCondition(joinedType.getCondition());
            builder
                .join(jooqTableFor(joinedType.getRight()))
                .on(joinCondition);

            if (countBuilder != null)
            {
                countBuilder.join(jooqTableFor(joinedType.getRight()))
                    .on(joinCondition);
            }
        }


        SimpleNode filter = queryDefinition.getFilter();
        Condition whereCondition = null;
        if (filter != null)
        {
            whereCondition = createCondition(filter);
            builder.where(whereCondition);
            if (countBuilder != null)
            {
                countBuilder.where(whereCondition);
            }
        }

        List<String> orderBy = queryDefinition.getOrderBy();
        if (orderBy != null && orderBy.size() > 0)
        {
            SortField[] fields = new SortField[orderBy.size()];
            for (int i = 0; i < orderBy.size(); i++)
            {
                String name = orderBy.get(i);

                if (name.startsWith("!"))
                {
                    fields[i] = DSL.field(name.substring(1)).desc();
                }
                else
                {
                    fields[i] = DSL.field(name).asc();
                }
            }

            builder.orderBy(fields);
        }

        int limit = queryDefinition.getLimit();
        int offset = queryDefinition.getOffset();
        if (limit > 0)
        {
            if (offset > 0)
            {
                builder.limit(limit).offset(offset);
            }
            else
            {
                builder.limit(limit);
            }
        }

        List<DomainObject> rows = builder.fetch(new QueryMapper(queryDefinition));

        int rowCount;
        if (pagedQuery)
        {
            rowCount = countBuilder.fetchOne(DSL.count());
        }
        else
        {
            rowCount = rows.size();
        }
        return new DataList(queryDefinition, rows, rowCount);
    }


    private Field<Object> jooqField(DataField dataField)
    {
        return field(dataField.getNameFromStrategy(namingStrategy));
    }


    private Condition createCondition(Node expression)
    {

        return null;
    }


    private Table<Record> jooqTableFor(QueryDomainType queryDomainType)
    {

        DomainType type = queryDomainType.getType();
        DomainService domainService = type.getDomainService();
        String schema = domainService.getSchema();
        return DSL.table(namingStrategy.getTableName(schema , type)).as(queryDomainType.getAlias());
    }



    private class QueryMapper
        implements RecordMapper<Record, DomainObject>
    {
        private final QueryDefinition queryDefinition;

        private final List<DataField> fields;


        public QueryMapper(QueryDefinition queryDefinition)
        {
            this.queryDefinition = queryDefinition;
            this.fields = queryDefinition.getQueryDomainType().getFieldsInOrder();
        }

        @Override
        public GenericDomainObject map(Record record)
        {
            GenericDomainObject domainObject = new GenericDomainObject();
            for (DataField field : fields)
            {
                QueryDomainType queryDomainType = field.getQueryDomainType();
                Object value = record.getValue(field.getNameFromStrategy(namingStrategy));

                domainObject.setProperty(field.getLocalName(), value);
            }
            return domainObject;
        }
    }
}
