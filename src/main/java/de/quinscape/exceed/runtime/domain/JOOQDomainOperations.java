package de.quinscape.exceed.runtime.domain;

import de.quinscape.exceed.expression.ASTExpression;
import de.quinscape.exceed.model.domain.DomainProperty;
import de.quinscape.exceed.model.domain.DomainType;
import de.quinscape.exceed.runtime.domain.property.PropertyConverter;
import de.quinscape.exceed.runtime.expression.ExpressionService;
import de.quinscape.exceed.runtime.util.DBUtil;
import org.jooq.DSLContext;
import org.jooq.DeleteQuery;
import org.jooq.Field;
import org.jooq.InsertQuery;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.jooq.Table;
import org.jooq.UpdateQuery;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.util.List;

public class JOOQDomainOperations
    implements DomainOperations
{
    private final static Logger log = LoggerFactory.getLogger(JOOQDomainOperations.class);

    private final static DefaultTransactionDefinition INSERT_TRANSACTION_DEF = createDefinition(TransactionDefinition.PROPAGATION_REQUIRED);

    private final static DefaultTransactionDefinition INSERT_OR_UPDATE_TRANSACTION_DEF = createDefinition(TransactionDefinition.PROPAGATION_REQUIRED, TransactionDefinition.ISOLATION_SERIALIZABLE);

    private static final TransactionDefinition UPDATE_TRANSACTION_DEF = createDefinition(TransactionDefinition.PROPAGATION_REQUIRED);

    private static final TransactionDefinition DELETE_TRANSACTION_DEF = createDefinition(TransactionDefinition.PROPAGATION_REQUIRED);

    private final DSLContext dslContext;

    private final ExpressionService expressionService;

    private final PlatformTransactionManager txManager;

    public JOOQDomainOperations(DSLContext dslContext,
                                ExpressionService expressionService,
                                PlatformTransactionManager txManager)
    {
        this.dslContext = dslContext;
        this.expressionService = expressionService;
        this.txManager = txManager;
    }


    @Override
    public DomainObject create(DomainService domainService, String type, String id)
    {
        final DomainType domainType = domainService.getDomainType(type);

        if (domainType == null)
        {
            throw new IllegalArgumentException("Unknown domain type '" + type + "'");
        }

        GenericDomainObject genericDomainObject = new GenericDomainObject();
        genericDomainObject.setDomainType(type);
        genericDomainObject.setId(id);
        genericDomainObject.setDomainService(domainService);

        final PropertyDefaultEnvironment env = new PropertyDefaultEnvironment(domainService);
        for (DomainProperty property : domainType.getProperties())
        {
            final ASTExpression defaultValueExpression = property.getDefaultValueExpression();
            if (defaultValueExpression != null)
            {
                final Object value = expressionService.evaluate(
                    defaultValueExpression,
                    env
                );

                genericDomainObject.setProperty(property.getName(), value);
            }
        }


        return genericDomainObject;
    }

    @Override
    public DomainObject read(DomainService domainService, String type, String id)
    {
        DomainType domainType = domainService.getDomainType(type);
        final NamingStrategy namingStrategy = domainService.getStorageConfiguration(type).getNamingStrategy();

        Table<Record> table = DBUtil.jooqTableFor(domainType, null);

        Field<Object> idField = DBUtil.jooqField(domainType, DomainType.ID_PROPERTY);

        return dslContext.select()
            .from(table)
            .where(idField.eq(id))
            .fetchOne(new DomainTypeRecordMapper(domainType, namingStrategy));
    }


    @Override
    public void delete(DomainService domainService, DomainObject domainObject)
    {
        TransactionStatus status = txManager.getTransaction(DELETE_TRANSACTION_DEF);
        try
        {
            final String type = domainObject.getDomainType();
            DomainType domainType = domainService.getDomainType(type);

            DeleteQuery<Record> query = dslContext.deleteQuery(DBUtil.jooqTableFor(domainType, null));

            for (String name : domainType.getPkFields())
            {
                Field<Object> pkField = DBUtil.jooqField(domainType, name);
                Object pkValue = domainObject.getProperty(name);
                query.addConditions(pkField.eq(pkValue));
            }

            int count = query.execute();
            if (count != 1)
            {
                log.warn("Update returned " + count + " results instead of one");
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
    public void insert(DomainService domainService, DomainObject domainObject)
    {
        TransactionStatus status = txManager.getTransaction(INSERT_TRANSACTION_DEF);
        try
        {
            insertInternal(domainService, domainObject);
        }
        catch (Exception ex)
        {
            txManager.rollback(status);
            throw ex;
        }
        txManager.commit(status);

    }


    private void insertInternal(DomainService domainService, DomainObject domainObject)
    {
        final String type = domainObject.getDomainType();
        final DomainType domainType = domainService.getDomainType(type);
        final NamingStrategy namingStrategy = domainService.getStorageConfiguration(type).getNamingStrategy();

        InsertQuery<Record> query = dslContext.insertQuery(DBUtil.jooqTableFor(domainType, null));

        for (DomainProperty property : domainType.getProperties())
        {
            final String name = property.getName();

            final PropertyConverter propertyConverter = domainService.getPropertyConverter(property.getType());
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
    public void insertOrUpdate(DomainService domainService, DomainObject genericDomainObject)
    {
        TransactionStatus status = txManager.getTransaction(INSERT_OR_UPDATE_TRANSACTION_DEF);
        try
        {
            final DomainObject existing = read(domainService, genericDomainObject.getDomainType(), genericDomainObject.getId());

            if (existing == null)
            {
                insertInternal(domainService, genericDomainObject);
            }
            else
            {
                updateInternal(domainService, genericDomainObject);
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
    public void update(DomainService domainService, DomainObject domainObject)
    {
        TransactionStatus status = txManager.getTransaction(UPDATE_TRANSACTION_DEF);
        try
        {
            updateInternal(domainService, domainObject);
        }
        catch (Exception ex)
        {
            txManager.rollback(status);
            throw ex;
        }
        txManager.commit(status);
    }

    private class DomainTypeRecordMapper
        implements RecordMapper<Record, DomainObject>
    {
        private final DomainType domainType;

        private final NamingStrategy namingStrategy;


        private DomainTypeRecordMapper(DomainType domainType, NamingStrategy namingStrategy)
        {
            this.domainType = domainType;
            this.namingStrategy = namingStrategy;
        }


        @Override
        public DomainObject map(Record record)
        {
            DomainService domainService = domainType.getDomainService();
            DomainObject domainObject = domainService.create(domainType.getName(), null);
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


    private void updateInternal(DomainService domainService, DomainObject domainObject)
    {
        final String type = domainObject.getDomainType();
        final DomainType domainType =  domainService.getDomainType(type);
        final NamingStrategy namingStrategy = domainService.getStorageConfiguration(type).getNamingStrategy();


        UpdateQuery<Record> query = dslContext.updateQuery(DBUtil.jooqTableFor(domainType, null));

        final List<String> pkFields = domainType.getPkFields();

        for (DomainProperty property : domainType.getProperties())
        {
            String name = property.getName();
            final PropertyConverter propertyConverter = domainService.getPropertyConverter(property.getType());
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
        int count = query.execute();
        if (count != 1)
        {
            log.warn("Update returned " + count + " results instead of one");
        }
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
}
