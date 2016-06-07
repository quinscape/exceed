package de.quinscape.exceed.runtime.domain;

import de.quinscape.exceed.model.domain.DomainType;
import de.quinscape.exceed.model.domain.EnumType;
import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.component.DataList;
import de.quinscape.exceed.runtime.component.QueryExecutor;
import de.quinscape.exceed.runtime.expression.query.DataField;
import de.quinscape.exceed.runtime.expression.query.JoinDefinition;
import de.quinscape.exceed.runtime.expression.query.QueryDefinition;
import de.quinscape.exceed.runtime.expression.query.QueryDomainType;
import de.quinscape.exceed.runtime.util.DBUtil;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.jooq.SelectQuery;
import org.jooq.SortField;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * JOOQ-based query executor.
 */
@Transactional
public class JOOQQueryExecutor
    implements QueryExecutor
{
    private final static Logger log = LoggerFactory.getLogger(JOOQQueryExecutor.class);

    private final DSLContext dslContext;

    private final NamingStrategy namingStrategy;


    public JOOQQueryExecutor(DSLContext dslContext, NamingStrategy namingStrategy)
    {
        if (dslContext == null)
        {
            throw new IllegalArgumentException("dslContext can't be null");
        }

        if (namingStrategy == null)
        {
            throw new IllegalArgumentException("namingStrategy can't be null");
        }

        this.dslContext = dslContext;
        this.namingStrategy = namingStrategy;
    }


    @Override
    public DataList execute(RuntimeContext runtimeContext, QueryDefinition queryDefinition)
    {
        QueryDomainType queryDomainType = queryDefinition.getQueryDomainType();

        Table<Record> mainTable = DBUtil.jooqTableFor(queryDomainType.getType(), queryDomainType.getAlias());

        SelectQuery<Record> query = dslContext.selectQuery();

        query.addFrom(mainTable);
        query.addSelect(
            queryDomainType.getFieldsInOrder().stream()
                .map(this::jooqField)
                .collect(Collectors.toList())
        );

        SelectQuery<Record> countQuery = null;

        boolean pagedQuery = queryDefinition.getLimit() > 0;
        if (pagedQuery)
        {
            countQuery = dslContext.selectQuery();
            countQuery.addFrom(mainTable);
            countQuery.addSelect(DSL.count());
        }
        JoinDefinition joinedType;
        while ((joinedType = queryDomainType.getJoinedType()) != null)
        {
            Condition joinCondition = joinedType.getCondition();
            Table<Record> joinedTable = DBUtil.jooqTableFor(
                joinedType.getRight().getType(),
                joinedType.getRight().getAlias()
            );
            query.addJoin(joinedTable, joinCondition);

            if (countQuery != null)
            {
                countQuery.addJoin(joinedTable, joinCondition);
            }
        }


        Condition condition = queryDefinition.getFilter();
        Condition whereCondition = null;
        if (condition != null)
        {
            whereCondition = condition;
            query.addConditions(whereCondition);
            if (countQuery != null)
            {
                countQuery.addConditions(whereCondition);
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
                    fields[i] = DSL.field(DSL.name(name.substring(1))).desc();
                }
                else
                {
                    fields[i] = DSL.field(DSL.name(name)).asc();
                }
            }

            query.addOrderBy(fields);
        }

        int limit = queryDefinition.getLimit();
        int offset = queryDefinition.getOffset();
        if (limit > 0)
        {
            if (offset > 0)
            {
                query.addLimit(offset, limit);
            }
            else
            {
                query.addLimit(limit);
            }
        }

        List<DomainObject> rows = query.fetch(new QueryMapper(runtimeContext.getDomainService(), queryDefinition));

        int rowCount;
        if (pagedQuery)
        {
            rowCount = countQuery.fetchOne(DSL.count());
        }
        else
        {
            rowCount = rows.size();
        }
        return createDataList(runtimeContext, queryDefinition, rows, rowCount);
    }


    private DataList createDataList(RuntimeContext runtimeContext, QueryDefinition queryDefinition, List<DomainObject> rows, int rowCount)
    {
        return new DataList(
            queryDefinition.createDomainTypeMap(),
            queryDefinition.createColumnDescriptorMap(),
            rows,
            rowCount
        );
    }



    private Field<Object> jooqField(DataField dataField)
    {
        return DSL.field(DSL.name(dataField.getNameFromStrategy(namingStrategy)));
    }

    private class QueryMapper
        implements RecordMapper<Record, DomainObject>
    {
        private final DomainService domainService;

        private final QueryDefinition queryDefinition;

        private final List<DataField> fields;


        public QueryMapper(DomainService domainService, QueryDefinition queryDefinition)
        {
            this.domainService = domainService;
            this.queryDefinition = queryDefinition;
            this.fields = queryDefinition.getQueryDomainType().getFieldsInOrder();
        }


        @Override
        public GenericDomainObject map(Record record)
        {
            GenericDomainObject domainObject = new GenericDomainObject();
            for (DataField field : fields)
            {
                Object value = record.getValue(DSL.field(DSL.name(field.getNameFromStrategy(namingStrategy))));

                domainObject.setProperty(field.getLocalName(), value);
            }
            return domainObject;
        }
    }
}
