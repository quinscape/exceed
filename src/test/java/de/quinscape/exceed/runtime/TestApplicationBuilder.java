package de.quinscape.exceed.runtime;

import de.quinscape.exceed.model.ApplicationModel;
import de.quinscape.exceed.model.domain.DomainType;
import de.quinscape.exceed.model.domain.EnumType;
import de.quinscape.exceed.model.domain.PropertyType;
import de.quinscape.exceed.model.process.Process;
import de.quinscape.exceed.runtime.config.DefaultPropertyConverters;
import de.quinscape.exceed.runtime.domain.DomainService;
import de.quinscape.exceed.runtime.domain.DomainServiceImpl;
import de.quinscape.exceed.runtime.service.model.ModelSchemaService;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FalseFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.svenson.JSONParser;
import org.svenson.tokenize.InputStreamSource;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

public class TestApplicationBuilder
{

    private Map<String, DomainType> domainTypes = new HashMap<>();

    private String name = "TestApp";

    private Map<String, EnumType> enums = new HashMap<>();

    private boolean registerBaseProperties = true;

    private Map<String, PropertyType> propertyTypes = new HashMap<>();

    private Map<String, Process> processes = new HashMap<>();

    private DomainService domainService;

    private static ModelSchemaService modelSchemaService = create();


    private static ModelSchemaService create()
    {
        try
        {
            modelSchemaService = new ModelSchemaService();
            modelSchemaService.init();
            return modelSchemaService;
        }
        catch (ClassNotFoundException e)
        {
            throw new ExceedRuntimeException(e);
        }
    }

    public TestApplicationBuilder withDomainType(String name, DomainType domainType)
    {
        domainTypes.put(name, domainType);
        return this;
    }


    public TestApplicationBuilder withEnum(String name, EnumType enumType)
    {
        enums.put(name, enumType);
        return this;
    }


    public TestApplicationBuilder withName(String name)
    {
        this.name = name;
        return this;
    }


    public TestApplicationBuilder withPropertyType(PropertyType propertyType)
    {
        propertyTypes.put(propertyType.getName(), propertyType);
        return this;
    }


    public TestApplicationBuilder withBaseProperties(boolean registerBaseProperties)
    {
        this.registerBaseProperties = registerBaseProperties;

        return this;
    }

    public TestApplicationBuilder withDomainService(DomainService domainService)
    {
        this.domainService = domainService;
        return this;
    }


    public TestApplication build()
    {
        ApplicationModel applicationModel = new ApplicationModel();
        applicationModel.setName(name);

        for (DomainType domainType : domainTypes.values())
        {
            applicationModel.addDomainType(domainType);
        }

        for (EnumType enumType : enums.values())
        {
            applicationModel.addEnum(enumType);
        }

        for (PropertyType propertyType : propertyTypes.values())
        {
            applicationModel.addPropertyType(propertyType);

        }

        if (registerBaseProperties)
        {
            propertyTypes.putAll(readBaseProperties());
        }

        if (domainService == null)
        {
            domainService = new DomainServiceImpl(new DefaultPropertyConverters().getConverters(), null,
                modelSchemaService.getModelDomainTypes());
        }

        return new TestApplication(applicationModel, domainService);
    }


    private Map<String, PropertyType> readBaseProperties()
    {
        try
        {
            Map<String, PropertyType> map = new HashMap<>();
            JSONParser parser = JSONParser.defaultJSONParser();

            for (File file : FileUtils.listFiles(new File("./src/main/base/models/domain/property/"), new
                SuffixFileFilter(".json"), FalseFileFilter.INSTANCE))
            {
                PropertyType propertyType = parser.parse(PropertyType.class, new InputStreamSource(new
                    FileInputStream(file)
                    , true));


                map.put(propertyType.getName(), propertyType);

            }

            return map;
        }
        catch (FileNotFoundException e)
        {
            throw new ExceedRuntimeException(e);
        }
    }

}
