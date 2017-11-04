package de.quinscape.exceed.runtime;

import de.quinscape.exceed.runtime.component.DataGraph;
import de.quinscape.exceed.runtime.domain.CommonDomainOperations;
import de.quinscape.exceed.runtime.domain.DomainObject;
import de.quinscape.exceed.runtime.domain.DomainOperations;
import de.quinscape.exceed.runtime.domain.DomainService;
import de.quinscape.exceed.runtime.domain.NamingStrategy;
import de.quinscape.exceed.runtime.expression.query.QueryDefinition;
import de.quinscape.exceed.runtime.schema.SchemaService;
import de.quinscape.exceed.runtime.schema.StorageConfiguration;
import de.quinscape.exceed.runtime.schema.StorageConfigurationRepository;

import java.util.Collections;
import java.util.Set;

public class TestStorageConfigurationRepository
    implements StorageConfigurationRepository
{
    public final static String TEST_STORAGE = "testStorage";


    @Override
    public Set<String> getConfigurationNames()
    {
        return Collections.singleton(TEST_STORAGE);
    }


    @Override
    public StorageConfiguration getConfiguration(String name)
    {
        return new TestStorageConfiguration();
    }


    private class TestStorageConfiguration
        implements StorageConfiguration
    {
        @Override
        public NamingStrategy getNamingStrategy()
        {
            return new NamingStrategy()
            {
                @Override
                public String getTableName(String typeName)
                {
                    return typeName.toLowerCase();
                }


                @Override
                public String[] getFieldName(String typeName, String propertyName)
                {
                    return new String[]{typeName, propertyName};
                }


                @Override
                public String getForeignKeyName(String typeName, String propertyName, String targetType, String
                    targetProperty)
                {
                    return "fk_" + typeName.toLowerCase() + "_" + propertyName.toLowerCase() + "_" + targetProperty
                        .toLowerCase();
                }


                @Override
                public String getUniqueConstraintName(String typeName, String propertyName)
                {
                    return "uc_" + typeName.toLowerCase() + "_" + propertyName.toLowerCase();
                }


                @Override
                public String getPrimaryKeyName(String typeName)
                {
                    return "pk_" + typeName.toLowerCase();
                }
            };
        }


        @Override
        public DomainOperations getDomainOperations()
        {
            return new DomainOperations()
            {
                @Override
                public DataGraph query(RuntimeContext runtimeContext, DomainService domainService, QueryDefinition queryDefinition)
                {
                    throw new UnsupportedOperationException();
                }


                @Override
                public DomainObject create(RuntimeContext runtimeContext, DomainService domainService, String type, String id, Class<?
                    extends DomainObject> implClass)
                {
//                    final DomainObject genericDomainObject;
//                    try
//                    {
//                        genericDomainObject = implClass.newInstance();
//
//                        if (GenericDomainObject.class.isAssignableFrom(implClass))
//                        {
//                            genericDomainObject.setDomainType(type);
//                        }
//                        genericDomainObject.setDomainService(domainService);
//                        genericDomainObject.setId(id);
//                        return genericDomainObject;
//                    }
//                    catch (InstantiationException | IllegalAccessException e)
//                    {
//                        throw new ExceedRuntimeException(e);
//                    }

                    return CommonDomainOperations.create(runtimeContext, domainService, type, id, implClass);
                }


                @Override
                public DomainObject read(RuntimeContext runtimeContext, DomainService domainService, String type, String id)
                {
                    throw new UnsupportedOperationException();
                }


                @Override
                public boolean delete(RuntimeContext runtimeContext, DomainService domainService, DomainObject genericDomainObject)
                {
                    throw new UnsupportedOperationException();
                }


                @Override
                public void insert(RuntimeContext runtimeContext, DomainService domainService, DomainObject genericDomainObject)
                {
                    throw new UnsupportedOperationException();
                }


                @Override
                public void insertOrUpdate(RuntimeContext runtimeContext, DomainService domainService, DomainObject genericDomainObject)
                {
                    throw new UnsupportedOperationException();
                }


                @Override
                public boolean update(RuntimeContext runtimeContext, DomainService domainService, DomainObject genericDomainObject)
                {
                    throw new UnsupportedOperationException();
                }
            };
        }


        @Override
        public SchemaService getSchemaService()
        {
            return null;
        }
    }
}
