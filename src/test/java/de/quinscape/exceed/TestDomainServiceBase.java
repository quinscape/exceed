package de.quinscape.exceed;

import de.quinscape.exceed.expression.ASTExpression;
import de.quinscape.exceed.model.domain.EnumType;
import de.quinscape.exceed.model.domain.StateMachine;
import de.quinscape.exceed.model.domain.property.DomainProperty;
import de.quinscape.exceed.model.domain.property.PropertyTypeModel;
import de.quinscape.exceed.model.domain.type.DomainType;
import de.quinscape.exceed.runtime.ExceedRuntimeException;
import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.application.RuntimeApplication;
import de.quinscape.exceed.runtime.datasrc.ExceedDataSource;
import de.quinscape.exceed.runtime.domain.DomainObject;
import de.quinscape.exceed.runtime.domain.DomainService;
import de.quinscape.exceed.runtime.domain.GenericDomainObject;
import de.quinscape.exceed.runtime.js.JsEnvironment;

import java.util.HashMap;
import java.util.Map;

public class TestDomainServiceBase
    implements DomainService
{
    protected Map<String, DomainType> domainTypes = new HashMap<>();
    protected Map<String, EnumType> enumTypes = new HashMap<>();

    private RuntimeApplication runtimeApplication;


    @Override
    public void init(
        RuntimeApplication runtimeApplication,
        Map<String, ExceedDataSource> dataSources
    )
    {

        this.runtimeApplication = runtimeApplication;
    }


    @Override
    public String toJSON(Object domainObject)
    {
        return null;
    }


    @Override
    public <T> T toDomainObject(Class<T> cls, String json)
    {
        return null;
    }


    @Override
    public DomainType getDomainType(String name)
    {
        return getDomainTypes().get(name);
    }


    @Override
    public String getSchema()
    {
        return "test";
    }


    @Override
    public String getAuthSchema()
    {
        return "test";
    }


    @Override
    public Map<String, DomainType> getDomainTypes()
    {
        return domainTypes;
    }


    @Override
    public Map<String, EnumType> getEnums()
    {
        return enumTypes;
    }


    @Override
    public Map<String, PropertyTypeModel> getPropertyTypes()
    {
        return runtimeApplication.getApplicationModel().getPropertyTypes();
    }


    @Override
    public Map<String, StateMachine> getStateMachines()
    {
        return runtimeApplication.getApplicationModel().getStateMachines();
    }


    @Override
    public DomainObject create(RuntimeContext runtimeContext, String type, String id)
    {
        return create(runtimeContext, type, id, GenericDomainObject.class);
    }


    @Override
    public DomainObject create(RuntimeContext runtimeContext, String type, String id, Class<? extends DomainObject> implClass)
    {

        final DomainObject genericDomainObject;
        try
        {
            genericDomainObject = implClass.newInstance();

            if (GenericDomainObject.class.isAssignableFrom(implClass))
            {
                genericDomainObject.setDomainType(type);
            }
            genericDomainObject.setDomainService(this);
            genericDomainObject.setId(id);

            final JsEnvironment env = runtimeContext.getJsEnvironment();

            final DomainType domainType = runtimeContext.getApplicationModel().getDomainType(type);

            for (DomainProperty property : domainType.getProperties())
            {
                final ASTExpression defaultValueExpression = property.getDefaultValueExpression();
                if (defaultValueExpression != null)
                {
                    final Object defaultValue = env.getValue(runtimeContext, defaultValueExpression);
                    genericDomainObject.setProperty(property.getName(), defaultValue);
                }
            }
            return genericDomainObject;
        }
        catch (InstantiationException | IllegalAccessException e)
        {
            throw new ExceedRuntimeException(e);
        }
    }


    @Override
    public DomainObject read(RuntimeContext runtimeContext, String type, String id)
    {
        return null;
    }


    @Override
    public boolean delete(RuntimeContext runtimeContext, DomainObject genericDomainObject)
    {

        return false;
    }


    @Override
    public void insert(RuntimeContext runtimeContext, DomainObject genericDomainObject)
    {

    }


    @Override
    public void insertOrUpdate(RuntimeContext runtimeContext, DomainObject genericDomainObject)
    {

    }


    @Override
    public boolean update(RuntimeContext runtimeContext, DomainObject genericDomainObject)
    {

        return false;
    }


    @Override
    public JsEnvironment getJsEnvironment()
    {
        return runtimeApplication != null ? runtimeApplication.getApplicationModel().getMetaData().getJsEnvironment() : null;
    }


    @Override
    public ExceedDataSource getDataSource(String dataSourceName)
    {
        return null;
    }


    @Override
    public Map<String, ExceedDataSource> getDataSources()
    {
        return null;
    }


}
