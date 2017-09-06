package de.quinscape.exceed.runtime.domain;

import de.quinscape.exceed.model.domain.property.DomainProperty;
import de.quinscape.exceed.model.domain.type.QueryTypeModel;
import de.quinscape.exceed.model.meta.PropertyType;
import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.domain.property.DecimalConverter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.object.MappingSqlQueryWithParameters;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import java.util.Map;

public class DefaultQueryTypeQuery
    extends MappingSqlQueryWithParameters<DomainObject>
{
    private final RuntimeContext runtimeContext;

    private final QueryTypeModel queryTypeModel;

    public DefaultQueryTypeQuery(
        RuntimeContext runtimeContext,
        DataSource ds,
        QueryTypeModel queryTypeModel
    )
    {
        this.runtimeContext = runtimeContext;
        this.queryTypeModel = queryTypeModel;

        setDataSource(ds);
        setSql(queryTypeModel.getQuery());
        final SqlParameter[] sqlParameters = getSqlParameters(queryTypeModel);
        if (sqlParameters != null)
        {
            setParameters(
                sqlParameters
            );
        }
    }


    private SqlParameter[] getSqlParameters(QueryTypeModel queryTypeModel)
    {
        final List<DomainProperty> parameterTypes = queryTypeModel.getParameterTypes();

        if (parameterTypes == null)
        {
            return null;
        }

        final SqlParameter[] params = new SqlParameter[parameterTypes.size()];


        for (int i = 0; i < parameterTypes.size(); i++)
        {
            DomainProperty parameterType = parameterTypes.get(i);

            if (PropertyType.DECIMAL.equals(parameterType.getType()))
            {
                params[i] = new SqlParameter(getSqlType(parameterType), DecimalConverter.getPrecision(3, parameterType.getConfig()));
            }
            else
            {
                params[i] = new SqlParameter(getSqlType(parameterType));
            }
        }
        return params;
    }


    private int getSqlType(DomainProperty parameterType)
    {
        final String type = parameterType.getType();
        switch(type)
        {
            case PropertyType.BOOLEAN:
                return Types.BOOLEAN;
            case PropertyType.INTEGER:
                return Types.INTEGER;
            case PropertyType.DECIMAL:
                return Types.DECIMAL;
            case PropertyType.PLAIN_TEXT:
                return Types.VARCHAR;
            case PropertyType.RICH_TEXT:
                return Types.VARCHAR;
            case PropertyType.CURRENCY:
            case PropertyType.LONG:
                return Types.BIGINT;
            case PropertyType.TIMESTAMP:
                return Types.TIMESTAMP;
            case PropertyType.DATE:
                return Types.DATE;
            case PropertyType.UUID:
                return Types.VARCHAR;
            case PropertyType.ENUM:
                return Types.INTEGER;
            case PropertyType.OBJECT:
            case PropertyType.MAP:
            case PropertyType.LIST:
            case PropertyType.DOMAIN_TYPE:
            default:
                throw new IllegalStateException("Unhandled type: " + type);
        }
    }


    @Override
    protected DomainObject mapRow(ResultSet rs, int rowNum, Object[] parameters, Map<?, ?> context) throws SQLException
    {
        final GenericDomainObject domainObject = new GenericDomainObject();

        List<DomainProperty> columnTypes = queryTypeModel.getColumnTypes();
        for (int i = 1; i <= columnTypes.size(); i++)
        {
            DomainProperty domainProperty = columnTypes.get(i - 1);
            final String name = domainProperty.getName();
            final String type = domainProperty.getType();

            Object value;
            switch(type)
            {
                case PropertyType.PLAIN_TEXT:
                case PropertyType.RICH_TEXT:
                case PropertyType.STATE:
                case PropertyType.UUID:
                    value = rs.getString(i);
                    break;
                case PropertyType.BOOLEAN:
                    value = rs.getBoolean(i);
                    break;
                case PropertyType.ENUM:
                case PropertyType.INTEGER:
                    value = rs.getInt(i);
                    break;
                case PropertyType.DECIMAL:
                    value = rs.getBigDecimal(i);
                    break;
                case PropertyType.CURRENCY:
                case PropertyType.LONG:
                    value = rs.getLong(i);
                    break;
                case PropertyType.TIMESTAMP:
                    value = rs.getTimestamp(i);
                    break;
                case PropertyType.DATE:
                    value = rs.getDate(i);
                    break;
                case PropertyType.OBJECT:
                    value = rs.getObject(i);
                    break;

                case PropertyType.MAP:
                case PropertyType.LIST:
                case PropertyType.DOMAIN_TYPE:
                default:
                    throw new IllegalStateException("Unhandled property type " + type);

            }

            domainObject.setProperty(name, value);

        }
        return domainObject;
    }
}
