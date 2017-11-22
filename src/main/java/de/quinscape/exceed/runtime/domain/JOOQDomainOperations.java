package de.quinscape.exceed.runtime.domain;

import de.quinscape.exceed.model.domain.property.DomainProperty;
import de.quinscape.exceed.model.domain.type.DomainType;
import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.component.DataGraph;
import de.quinscape.exceed.runtime.component.DataGraphQualifier;
import de.quinscape.exceed.runtime.datasrc.ExceedDataSource;
import de.quinscape.exceed.runtime.datasrc.JOOQDataSource;
import de.quinscape.exceed.runtime.domain.property.PropertyConverter;
import de.quinscape.exceed.runtime.expression.query.DataField;
import de.quinscape.exceed.runtime.expression.query.JoinDefinition;
import de.quinscape.exceed.runtime.expression.query.QueryCondition;
import de.quinscape.exceed.runtime.expression.query.QueryDefinition;
import de.quinscape.exceed.runtime.expression.query.QueryDomainType;
import de.quinscape.exceed.runtime.expression.query.QueryMapping;
import de.quinscape.exceed.runtime.util.DBUtil;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.DeleteQuery;
import org.jooq.Field;
import org.jooq.InsertQuery;
import org.jooq.JoinType;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.jooq.SelectQuery;
import org.jooq.SortField;
import org.jooq.Table;
import org.jooq.UpdateQuery;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class JOOQDomainOperations
    implements DomainOperations
{
    private final static Logger log = LoggerFactory.getLogger(JOOQDomainOperations.class);

    private final static DefaultTransactionDefinition INSERT_TRANSACTION_DEF = createDefinition(TransactionDefinition.PROPAGATION_REQUIRED);

    private final static DefaultTransactionDefinition INSERT_OR_UPDATE_TRANSACTION_DEF = createDefinition(TransactionDefinition.PROPAGATION_REQUIRED, TransactionDefinition.ISOLATION_SERIALIZABLE);

    private static final TransactionDefinition UPDATE_TRANSACTION_DEF = createDefinition(TransactionDefinition.PROPAGATION_REQUIRED);

    private static final TransactionDefinition DELETE_TRANSACTION_DEF = createDefinition(TransactionDefinition.PROPAGATION_REQUIRED);

    private final PlatformTransactionManager txManager;

    public JOOQDomainOperations(
        PlatformTransactionManager txManager
    )
    {
        this.txManager = txManager;
    }


    @Override
    public DomainObject create(
        RuntimeContext runtimeContext, DomainService domainService,
        String type,
        String id,
        Class<? extends DomainObject> implClass
    )
    {
        return CommonDomainOperations.create(runtimeContext, domainService, type, id, implClass);
    }

    @Override
    public DomainObject read(RuntimeContext runtimeContext, DomainService domainService, String type, String id)
    {
        DomainType domainType = domainService.getDomainType(type);
        final ExceedDataSource dataSource = domainService.getDataSource(
            domainType.getDataSourceName()
        );
        final NamingStrategy namingStrategy = dataSource.getStorageConfiguration().getNamingStrategy();

        Table<Record> table = DBUtil.jooqTableFor(domainType, null);

        Field<Object> idField = DBUtil.jooqField(domainType, DomainType.ID_PROPERTY);

        return getDSLContext(runtimeContext, type).select()
            .from(table)
            .where(idField.eq(id))
            .fetchOne(new DomainTypeRecordMapper(runtimeContext, domainType, namingStrategy));
    }


    @Override
    public boolean delete(RuntimeContext runtimeContext, DomainService domainService, DomainObject domainObject)
    {
        TransactionStatus status = txManager.getTransaction(DELETE_TRANSACTION_DEF);
        try
        {
            final String type = domainObject.getDomainType();
            DomainType domainType = domainService.getDomainType(type);

            DeleteQuery<Record> query = getDSLContext(runtimeContext, domainType).deleteQuery(DBUtil.jooqTableFor(domainType, null));

            for (String name : domainType.getPkFields())
            {
                Field<Object> pkField = DBUtil.jooqField(domainType, name);
                Object pkValue = domainObject.getProperty(name);
                query.addConditions(pkField.eq(pkValue));
            }

            final boolean wasDeleted = query.execute() == 1;

            txManager.commit(status);
            return wasDeleted;
        }
        catch (Exception ex)
        {
            txManager.rollback(status);
            throw ex;
        }
    }




    @Override
    public void insert(RuntimeContext runtimeContext, DomainService domainService, DomainObject domainObject)
    {
        TransactionStatus status = txManager.getTransaction(INSERT_TRANSACTION_DEF);
        try
        {
            insertInternal(runtimeContext, domainService, domainObject);
        }
        catch (Exception ex)
        {
            txManager.rollback(status);
            throw ex;
        }
        txManager.commit(status);

    }


    private void insertInternal(
        RuntimeContext runtimeContext, DomainService domainService,
        DomainObject domainObject
    )
    {
        final String type = domainObject.getDomainType();
        final DomainType domainType = domainService.getDomainType(type);
        final NamingStrategy namingStrategy = domainService.getDataSource(domainType.getDataSourceName()).getStorageConfiguration().getNamingStrategy();

        InsertQuery<Record> query = getDSLContext(runtimeContext, domainType).insertQuery(DBUtil.jooqTableFor(domainType, null));

        for (DomainProperty property : domainType.getProperties())
        {
            final String name = property.getName();

            final PropertyConverter propertyConverter = property.getPropertyType();
            Field<Object> field = DSL.field(DSL.name(namingStrategy.getFieldName(domainType.getName(), name)), propertyConverter.getJavaType());
            query.addValue(field, domainObject.getProperty(name));
        }

        int count = query.execute();
        if (count != 1)
        {
            log.warn("Insert returned " + count + " results instead of one");
        }
    }

    @Override
    public void insertOrUpdate(
        RuntimeContext runtimeContext,
        DomainService domainService,
        DomainObject genericDomainObject
    )
    {
        TransactionStatus status = txManager.getTransaction(INSERT_OR_UPDATE_TRANSACTION_DEF);
        try
        {
            final DomainObject existing = read(runtimeContext, domainService, genericDomainObject.getDomainType(), genericDomainObject.getId());

            if (existing == null)
            {
                insertInternal(runtimeContext, domainService, genericDomainObject);
            }
            else
            {
                updateInternal(runtimeContext, domainService, genericDomainObject);
            }

        }
        catch (Exception ex)
        {
            txManager.rollback(status);
            throw ex;
        }
        txManager.commit(status);
    }


    @Override
    public boolean update(RuntimeContext runtimeContext, DomainService domainService, DomainObject domainObject)
    {
        TransactionStatus status = txManager.getTransaction(UPDATE_TRANSACTION_DEF);
        try
        {
            final boolean wasUpdated = updateInternal(runtimeContext, domainService, domainObject);

            txManager.commit(status);
            return wasUpdated;
        }
        catch (Exception ex)
        {
            txManager.rollback(status);
            throw ex;
        }
    }

    private class DomainTypeRecordMapper
        implements RecordMapper<Record, DomainObject>
    {
        private final RuntimeContext runtimeContext;

        private final DomainType domainType;

        private final NamingStrategy namingStrategy;


        private DomainTypeRecordMapper(RuntimeContext runtimeContext, DomainType domainType, NamingStrategy namingStrategy)


        {
            this.runtimeContext = runtimeContext;
            this.domainType = domainType;
            this.namingStrategy = namingStrategy;
        }


        @Override
        public DomainObject map(Record record)
        {
            DomainService domainService = domainType.getDomainService();
            DomainObject domainObject = domainService.create(runtimeContext, domainType.getName(), null);
            for (DomainProperty property : domainType.getProperties())
            {
                String name = property.getName();
                Object value = record.getValue(DSL.field(DSL.name(namingStrategy.getFieldName(domainType.getName(),
                    name))));
                domainObject.setProperty(name, value);
            }
            return domainObject;
        }
    }


    private boolean updateInternal(
        RuntimeContext runtimeContext, DomainService domainService,
        DomainObject domainObject
    )
    {
        final String type = domainObject.getDomainType();
        final DomainType domainType =  domainService.getDomainType(type);
        final ExceedDataSource dataSource = domainService.getDataSource(
            domainType.getDataSourceName()
        );
        final NamingStrategy namingStrategy = dataSource.getStorageConfiguration().getNamingStrategy();


        UpdateQuery<Record> query = getDSLContext(runtimeContext, domainType).updateQuery(DBUtil.jooqTableFor(domainType, null));

        final List<String> pkFields = domainType.getPkFields();

        for (DomainProperty property : domainType.getProperties())
        {
            String name = property.getName();
            final PropertyConverter propertyConverter = property.getPropertyType();
            Field<Object> field = DSL.field(DSL.name(namingStrategy.getFieldName(domainType.getName(), name)), propertyConverter.getJavaType());

            if (pkFields.contains(name))
            {
                Object pkValue = domainObject.getProperty(name);
                query.addConditions(field.eq(pkValue));
            }
            else
            {
                query.addValue(field, domainObject.getProperty(name));
            }
        }
        return query.execute() == 1;
    }



    private static DefaultTransactionDefinition createDefinition(int propagationRequired)
    {
        return createDefinition(propagationRequired, TransactionDefinition.ISOLATION_DEFAULT);
    }


    private static DefaultTransactionDefinition createDefinition(int propagationRequired, int isolationLevel)
    {
        final DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setPropagationBehavior(propagationRequired);
        def.setIsolationLevel(isolationLevel);
        return def;
    }

    @Override
    public DataGraph query(RuntimeContext runtimeContext, DomainService domainService, QueryDefinition queryDefinition)
    {
        final QueryDomainType queryDomainType = queryDefinition.getQueryDomainType();

        Table<Record> mainTable = DBUtil.jooqTableFor(queryDomainType.getType(), queryDomainType.getAlias());

        final DSLContext dslContext = getDSLContext(runtimeContext, queryDomainType.getLeftMost().getType());
        SelectQuery<Record> query = dslContext.selectQuery();

        query.addFrom(mainTable);
        query.addSelect(
            queryDomainType.getFieldsInOrder().stream()
                .map(dataField -> jooqField(runtimeContext, dataField))
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

        QueryDomainType current = queryDomainType;
        while ((joinedType = current.getJoinedType()) != null)
        {
            Condition joinCondition = joinedType.getCondition().getCondition();
            Table<Record> joinedTable = DBUtil.jooqTableFor(
                joinedType.getRight().getType(),
                joinedType.getRight().getNameOrAlias()
            );
            final JoinType joinType = getJoinType(joinedType.getJoinType());
            query.addJoin(joinedTable, joinType, joinCondition);

            if (countQuery != null)
            {
                countQuery.addJoin(joinedTable, joinType,   joinCondition);
            }

            current = joinedType.getRight();
        }


        final QueryCondition filter = queryDefinition.getFilter();
        if (filter != null)
        {
            Condition condition = filter.getCondition();
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
                    fields[i] =jooqField(
                            runtimeContext,
                            queryDomainType.resolveField(
                                name.substring(1)
                            )
                        ).desc();
                }
                else
                {
                    fields[i] = jooqField(
                        runtimeContext,
                        queryDomainType.resolveField(name)
                    ).asc();
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


    private DSLContext getDSLContext(RuntimeContext runtimeContext, String type)
    {
        return getDSLContext(runtimeContext, runtimeContext.getDomainService().getDomainType(type));
    }

    private DSLContext getDSLContext(RuntimeContext runtimeContext, DomainType type)
    {
        final ExceedDataSource dataSource = runtimeContext.getDomainService().getDataSource(type.getDataSourceName());
        if (!(dataSource instanceof JOOQDataSource))
        {
            throw new IllegalStateException("Data source is not a JOOQDataSource");
        }

        return ((JOOQDataSource) dataSource).getDslContext();
    }


    private JoinType getJoinType(String joinType)
    {
        return JoinType.valueOf(DefaultNamingStrategy.camelCaseToUnderline(joinType).toUpperCase());
    }


    private DataGraph createDataList(RuntimeContext runtimeContext, QueryDefinition queryDefinition,
                                     List<DomainObject> rows, int rowCount)
    {
        return new DataGraph(
            queryDefinition.createColumnDescriptorMap(),
            rows,
            rowCount,
            DataGraphQualifier.QUERY
        );
    }


    private static Field<Object> jooqField(RuntimeContext runtimeContext, DataField dataField)
    {
        final ExceedDataSource dataSource = runtimeContext.getDomainService().getDataSource(
            dataField.getQueryDomainType().getType().getDataSourceName()
        );
        final NamingStrategy namingStrategy = dataSource.getStorageConfiguration().getNamingStrategy();
        return DSL.field(DSL.name(dataField.getNameFromStrategy(namingStrategy)));
    }


    private class QueryMapper
        implements RecordMapper<Record, DomainObject>
    {
        private final DomainService domainService;

        private final QueryDefinition queryDefinition;

        private final List<DataField> fields;
        private  final Map<String, QueryMapping> queryMappings;

        private final Set<DataField> mappedFields;

        public QueryMapper(DomainService domainService, QueryDefinition queryDefinition)
        {
            this.domainService = domainService;
            this.queryDefinition = queryDefinition;
            this.fields = queryDefinition.getQueryDomainType().getFieldsInOrder();
            queryMappings = queryDefinition.getQueryDomainType().getQueryMappings();

            if (queryMappings != null)
            {
                mappedFields = new HashSet<>();

                final List<DataField> allFields = queryDefinition.getQueryDomainType().getJoinedFields();
                for (QueryMapping queryMapping : queryMappings.values())
                {
                    DomainObject nested = new GenericDomainObject();
                    nested.setProperty(DomainType.TYPE_PROPERTY, queryMapping.getDomainType());

                    for (Integer idx : queryMapping.getIndexes())
                    {
                        final DataField dataField = allFields.get(idx);
                        mappedFields.add(dataField);
                    }
                }
            }
            else
            {
                mappedFields = Collections.emptySet();
            }
        }


        @Override
        public GenericDomainObject map(Record record)
        {
            GenericDomainObject domainObject = new GenericDomainObject();
            domainObject.setDomainService(domainService);
            for (DataField field : fields)
            {
                if (!mappedFields.contains(field))
                {
                    final ExceedDataSource dataSource = domainService.getDataSource(
                        field.getQueryDomainType().getType().getDataSourceName()
                    );
                    final NamingStrategy namingStrategy = dataSource.getStorageConfiguration().getNamingStrategy();
                    Object value = record.getValue(DSL.field(DSL.name(field.getNameFromStrategy(namingStrategy))));
                    domainObject.setProperty(field.getLocalName(), value);
                }
            }

            if (queryMappings != null)
            {
                final List<DataField> allFields = queryDefinition.getQueryDomainType().getJoinedFields();
                for (QueryMapping queryMapping : queryMappings.values())
                {
                    DomainObject nested = new GenericDomainObject();
                    nested.setProperty(DomainType.TYPE_PROPERTY, queryMapping.getDomainType());

                    for (Integer idx : queryMapping.getIndexes())
                    {
                        final DataField dataField = allFields.get(idx);

                        final ExceedDataSource dataSource = domainService.getDataSource(
                            dataField.getQueryDomainType().getType().getDataSourceName()
                        );
                        final NamingStrategy namingStrategy = dataSource.getStorageConfiguration().getNamingStrategy();

                        Object nestedValue = record.getValue(DSL.field(DSL.name(dataField.getNameFromStrategy(namingStrategy))));

                        nested.setProperty(dataField.getDomainProperty().getName(), nestedValue);
                    }

                    domainObject.setProperty(queryMapping.getEmbeddingField(), nested);
                }
            }

            return domainObject;
        }
    }
}
