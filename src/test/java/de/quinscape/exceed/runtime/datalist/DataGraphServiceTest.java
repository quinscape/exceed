package de.quinscape.exceed.runtime.datalist;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import de.quinscape.exceed.TestDomainServiceBase;
import de.quinscape.exceed.model.Model;
import de.quinscape.exceed.model.domain.property.DomainProperty;
import de.quinscape.exceed.model.domain.type.DomainType;
import de.quinscape.exceed.model.domain.type.DomainTypeModel;
import de.quinscape.exceed.model.meta.PropertyType;
import de.quinscape.exceed.model.translation.TranslationEntry;
import de.quinscape.exceed.runtime.ExceedRuntimeException;
import de.quinscape.exceed.runtime.RuntimeContextHolder;
import de.quinscape.exceed.runtime.TestApplication;
import de.quinscape.exceed.runtime.TestApplicationBuilder;
import de.quinscape.exceed.runtime.component.DataGraph;
import de.quinscape.exceed.runtime.domain.DomainObject;
import de.quinscape.exceed.runtime.domain.GenericDomainObject;
import de.quinscape.exceed.runtime.service.model.ModelSchemaService;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

public class DataGraphServiceTest
{
    private final static Logger log = LoggerFactory.getLogger(DataGraphServiceTest.class);

    // we mostly test conversion of different types  in "row" here because "types" and "columns" are just
    // straightforward JSON generation without any transformation or conversion


    private ModelSchemaService modelSchemaService = createSchemaService();


    private ModelSchemaService createSchemaService()
    {
        try
        {
            final ModelSchemaService svc = new ModelSchemaService();
            svc.init();
            return svc;
        }
        catch (ClassNotFoundException e)
        {
            throw new ExceedRuntimeException(e);
        }
    }


//    private Map<String, PropertyConverter> propertyConverters = new HashMap<>();
//    {
//        propertyConverters.put("UUIDConverter", new UUIDConverter());
//        propertyConverters.put("PlainTextConverter", new PlainTextConverter());
//        propertyConverters.put("DateConverter", new DateConverter());
//        propertyConverters.put("TimestampConverter", new TimestampConverter());
//        propertyConverters.put("DomainTypeConverter", new DomainTypeConverter());
//        propertyConverters.put("ListConverter", new ListConverter());
//        propertyConverters.put("MapConverter", new MapConverter());
//    };

    private TestDomainService domainService = new TestDomainService(modelSchemaService);

    private DataGraphService dataGraphService = new DataGraphService(
        domainService
    );

    private TestApplication testApplication = new TestApplicationBuilder()
        .withBaseProperties(true)
        .withDomainService(domainService)
        .build();

    @Before
    public void registerRuntimeContext()
    {
        RuntimeContextHolder.register(testApplication.createRuntimeContext(), null);
    }

    @Test
    public void testSimpleToJSON() throws Exception
    {

        DataGraph dataGraph = new DataGraph(ImmutableMap.of(
                        "name", DomainProperty.builder().withName("name").withType(PropertyType.PLAIN_TEXT)
                            .withDomainType("Foo").build(),
                        "created", DomainProperty.builder().withName("created").withType(PropertyType.TIMESTAMP)
                            .withDomainType("Foo").build()
                    ), ImmutableList.of(
                        ImmutableMap.of(
                            "name", "MyFoo", "created", new Timestamp(0)
                        )
                    ), 1, null);


        String json = dataGraphService.toJSON(dataGraph);

        assertThat(json, containsString("\"name\":\"MyFoo\""));
        assertThat(json, containsString("\"created\":\"1970-01-01"));

        //log.info(JSON.formatJSON(json));
    }

    @Test
    public void testJoinedToJSON() throws Exception
    {
        DataGraph dataGraph = new DataGraph(ImmutableMap.of(
                        "name", DomainProperty.builder().withName("name").withType(PropertyType.PLAIN_TEXT).withDomainType("Foo").build(),
                        "created", DomainProperty.builder().withName("created").withType(PropertyType.TIMESTAMP).withDomainType("Foo").build(),
                        "barCreated", DomainProperty.builder().withName("createdWithADifferentName").withType(PropertyType.TIMESTAMP)
                            .withDomainType("Bar").build()
                    ), ImmutableList.of(
                        ImmutableMap.of(
                            "name", "MyFoo",
                            "created", new Timestamp(TimeUnit.DAYS.toMillis(1)),
                            "barCreated", new Timestamp(TimeUnit.DAYS.toMillis(2))
                        )
                    ), 1, null);


        String json = dataGraphService.toJSON(dataGraph);

        assertThat(json, containsString("\"name\":\"MyFoo\""));
        assertThat(json, containsString("\"created\":\"1970-01-02"));
        assertThat(json, containsString("\"barCreated\":\"1970-01-03T00:00:00Z\""));

        //log.info(JSON.formatJSON(json));
    }


    @Test
    public void testComplexListToJSON() throws Exception
    {
        DataGraph dataGraph = new DataGraph(ImmutableMap.of(
                        "name", DomainProperty.builder()
                                .withName("name")
                                .withType(PropertyType.PLAIN_TEXT)
                                .withDomainType("CContainer")
                                .build(),
                        "created", DomainProperty.builder()
                            .withName("created")
                            .withType(PropertyType.TIMESTAMP)
                            .withDomainType("CContainer")
                            .build(),
                        "bazes", DomainProperty.builder()
                            .withName("bazes")
                            .withType(PropertyType.LIST, "Baz")
                            .withDomainType("CContainer")
                            .build()
                    ), ImmutableList.of(
                        ImmutableMap.of(
                            "name", "MyFoo",
                            "created", new Timestamp(TimeUnit.DAYS.toMillis(1)),
                            "bazes", ImmutableList.of(
                                createDomainObject("Baz", "created", new Timestamp(TimeUnit.DAYS.toMillis(3))),
                                createDomainObject("Baz","created", new Timestamp(TimeUnit.DAYS.toMillis(4)))
                            )
                        )
                    ), 1, null);


        String json = dataGraphService.toJSON(dataGraph);

        assertThat(json, containsString("\"name\":\"MyFoo\""));
        assertThat(json, containsString("\"created\":\"1970-01-02"));
        assertThat(json, containsString("\"bazes\":["));
        assertThat(json, containsString("\"_type\":\"Baz\""));
        assertThat(json, containsString("\"_type\":\"Baz\",\"created\":\"1970-01-04T00:00:00Z\""));
        assertThat(json, containsString("\"_type\":\"Baz\",\"created\":\"1970-01-05T00:00:00Z\""));

        //log.info(JSON.formatJSON(json));
    }


    private DomainObject createDomainObject(String type, String prop, Object value)
    {

        final GenericDomainObject domainObject = new GenericDomainObject();

        domainObject.setDomainType(type);
        domainObject.setDomainService(domainService);
        domainObject.setProperty(prop, value);

        return domainObject;
    }


    @Test
    public void testPropertyListToJSON() throws Exception
    {
        DataGraph dataGraph = new DataGraph(ImmutableMap.of(
                        "name", DomainProperty.builder().withName("name").withType(PropertyType.PLAIN_TEXT)
                            .withDomainType("PLContainer").build(),
                        "created", DomainProperty.builder().withName("created").withType(PropertyType.TIMESTAMP)
                            .withDomainType("PLContainer").build(),
                        "dates", DomainProperty.builder().withName("dates").withType(PropertyType.LIST, PropertyType.TIMESTAMP).withDomainType("PLContainer").build()
                    ), ImmutableList.of(
                        ImmutableMap.of(
                            "name", "MyFoo",
                            "created", new Timestamp(TimeUnit.DAYS.toMillis(1)),
                            "dates", ImmutableList.of(
                                new Timestamp(TimeUnit.DAYS.toMillis(5)),
                                new Timestamp(TimeUnit.DAYS.toMillis(6))
                            )
                        )
                    ), 1, null);


        String json = dataGraphService.toJSON(dataGraph);

        assertThat(json, containsString("\"name\":\"MyFoo\""));
        assertThat(json, containsString("\"created\":\"1970-01-02"));
        assertThat(json, containsString("[\"1970-01-06T00:00:00Z\",\"1970-01-07T00:00:00Z\"]"));

        //log.info(JSON.formatJSON(json));
    }


    @Test
    public void testComplexMapToJSON() throws Exception
    {
        DataGraph dataGraph = new DataGraph(ImmutableMap.of(
                        "name", DomainProperty.builder().withName("name").withType(PropertyType.PLAIN_TEXT)
                            .withDomainType("MapContainer").build(),
                        "created", DomainProperty.builder().withName("created").withType(PropertyType.TIMESTAMP)
                            .withDomainType("MapContainer").build(),
                        "bazes", DomainProperty.builder().withName("bazes").withType(PropertyType.MAP, "Baz").withDomainType("MapContainer").build()
                    ), ImmutableList.of(
                        ImmutableMap.of(
                            "name", "MyFoo",
                            "created", new Timestamp(TimeUnit.DAYS.toMillis(1)),
                            "bazes", ImmutableMap.of(
                                "Bar1", createDomainObject("Baz",
                                    "created", new Timestamp(TimeUnit.DAYS.toMillis(7))
                                ),
                                "Bar2", createDomainObject("Baz",
                                    "created", new Timestamp(TimeUnit.DAYS.toMillis(8))
                                )
                            )
                        )
                    ), 1, null);


        String json = dataGraphService.toJSON(dataGraph);

        assertThat(json, containsString("\"name\":\"MyFoo\""));
        assertThat(json, containsString("\"created\":\"1970-01-02"));
        assertThat(json, containsString("\"bazes\":{"));
        assertThat(json, containsString("\"Bar1\":{\"_type\":\"Baz\",\"created\":\"1970-01-08T00:00:00Z\""));
        assertThat(json, containsString("\"Bar2\":{\"_type\":\"Baz\",\"created\":\"1970-01-09T00:00:00Z\""));

        //log.info(JSON.formatJSON(json));
    }


    @Test
    public void testPropertyMapToJSON() throws Exception
    {
        DataGraph dataGraph = new DataGraph(ImmutableMap.of(
                        "name", DomainProperty.builder().withName("name").withType(PropertyType.PLAIN_TEXT)
                            .withDomainType("PropMapContainer").build(),
                        "created", DomainProperty.builder().withName("created").withType(PropertyType.TIMESTAMP)
                            .withDomainType("PropMapContainer").build(),
                        "dates", DomainProperty.builder().withName("dates").withType(PropertyType.MAP, PropertyType.TIMESTAMP).withDomainType("PropMapContainer").build()
                    ), ImmutableList.of(
                        ImmutableMap.of(
                            "name", "MyFoo",
                            "created", new Timestamp(TimeUnit.DAYS.toMillis(1)),
                            "dates", ImmutableMap.of(
                                "Event1", new Timestamp(TimeUnit.DAYS.toMillis(7)),
                                "Event2", new Timestamp(TimeUnit.DAYS.toMillis(8))
                            )
                        )
                    ), 1, null);


        String json = dataGraphService.toJSON(dataGraph);

        assertThat(json, containsString("\"name\":\"MyFoo\""));
        assertThat(json, containsString("\"created\":\"1970-01-02"));
        assertThat(json, containsString("\"Event1\":\"1970-01-08"));
        assertThat(json, containsString("\"Event2\":\"1970-01-09"));


        //log.info(JSON.formatJSON(json));
    }


    @Test
    public void testStructuredObjectGraph() throws Exception
    {
        DataGraph dataGraph = new DataGraph(ImmutableMap.of(
                        "name", DomainProperty.builder().withName("name").withType(PropertyType.PLAIN_TEXT)
                            .withDomainType("PropMapContainer").build(),
                        "created", DomainProperty.builder().withName("created").withType(PropertyType.TIMESTAMP).withDomainType("PropMapContainer").build()
                    ), ImmutableMap.of(
                        "name", "MyFoo",
                        "created", new Timestamp(TimeUnit.DAYS.toMillis(1))
                    ), -1, null);


        String json = dataGraphService.toJSON(dataGraph);

        assertThat(json, containsString("\"type\":\"OBJECT\""));
        assertThat(json, containsString("\"name\":\"MyFoo\""));
        assertThat(json, containsString("\"created\":\"1970-01-02"));
        assertThat(json, containsString("\"count\":1"));


    }

    @Test
    public void testMapLikeGraph() throws Exception
    {
        DataGraph dataGraph = new DataGraph(ImmutableMap.of(
                        "*", DomainProperty.builder().withName("created").withType(PropertyType.TIMESTAMP).build()
                    ), ImmutableMap.of(
                        "A", new Timestamp(TimeUnit.DAYS.toMillis(1)),
                        "B", new Timestamp(TimeUnit.DAYS.toMillis(2))
                    ), -1, null);


        String json = dataGraphService.toJSON(dataGraph);

        assertThat(json, containsString("\"type\":\"OBJECT\""));
        assertThat(json, containsString("\"A\":\"1970-01-02"));
        assertThat(json, containsString("\"B\":\"1970-01-03"));
        assertThat(json, containsString("\"count\":1"));


    }


    @Test
    public void testRecursiveType() throws Exception
    {
        DomainObject root = createDomainObject("Recursive", "created", new Timestamp(TimeUnit.DAYS.toMillis(3)));
        DomainObject kid = createDomainObject("Recursive", "created", new Timestamp(TimeUnit.DAYS.toMillis(4)));
        root.setProperty("kids", Collections.singletonList(kid));


        DataGraph dataGraph = new DataGraph(ImmutableMap.of(
                        "root", DomainProperty.builder().withName("root").withType(PropertyType.DOMAIN_TYPE, "Recursive").build()
                    ), ImmutableMap.of(
                        "root", root
                    ), 1, null);


        String json = dataGraphService.toJSON(dataGraph);

        assertThat(json, containsString("\"_type\":\"Recursive\""));
        assertThat(json, containsString("\"created\":\"1970-01-04"));
        assertThat(json, containsString("\"created\":\"1970-01-05"));

    }


    /** We check that an annotation like @see TranslationEntry#setTranslations(Map) is converted correctly  */
    @Test
    public void thatTypeAnnotationsWork() throws Exception
    {

        final String testTagName = "TestTag";
        final TranslationEntry entry = new TranslationEntry(testTagName);

        DomainObject translation = translation("de-DE", "Test-Tag");

        final String id1 = translation.getId();

        entry.setTranslations(
            ImmutableMap.of((String)translation.getProperty("tag"), translation)
        );
        DomainObject local = translation("de-DE", "Local-Tag");
        local.setProperty("processName", "TestProcess");


        final String id2 = local.getId();

        entry.setLocalTranslations(
            ImmutableList.of(local)
        );

        DataGraph dataGraph = new DataGraph(ImmutableMap.of(
                        DataGraph.WILDCARD_SYMBOL,
                        DomainProperty.builder()
                            .withName("entry")
                            .withType(PropertyType.DOMAIN_TYPE, "xcd.translation.TranslationEntry")
                            .build()
                    ), ImmutableMap.of(
                        testTagName, entry
                    ), 1, null);

        String json = dataGraphService.toJSON(dataGraph);

        //{"type":"OBJECT","columns":{"*":{"name":"entry","type":"DomainType","typeParam":"xcd.translation.TranslationEntry","required":false,"maxLength":0}},"rootObject":{"TestTag":{"_":null,"type":"xcd.translation.TranslationEntry","references":[],"translations":{"de-DE":{"_type":"AppTranslation","id":"b846f5ee-80db-4d20-bf2f-4cde3f0489ac","locale":"de-DE","tag":"de-DE","translation":"Test-Tag","created":"2017-05-17T13:31:13Z","processName":null,"viewName":null}},"name":"TestTag","localTranslations":[{"_type":"AppTranslation","id":"a221a75d-7683-4788-aa59-fe995c7dc075","locale":"de-DE","tag":"de-DE","translation":"Local-Tag","created":"2017-05-17T13:31:13Z","processName":null,"viewName":null}]}},"count":1}

        assertThat(json,containsString(Model.getType(TranslationEntry.class)));
        assertThat(json,containsString("\"id\":\"" + id1 + "\""));
        assertThat(json,containsString("\"id\":\"" + id2 + "\""));
        assertThat(json,containsString("TestProcess"));
    }


    private DomainObject translation(String tag, String translation)
    {
        final String id = UUID.randomUUID().toString();
        final DomainObject domainObject = domainService.create(testApplication.createRuntimeContext(), "AppTranslation", id);

        domainObject.setProperty("tag", tag);

        domainObject.setProperty("created", new Timestamp(System.currentTimeMillis()));
        domainObject.setProperty("locale", "de-DE");
        domainObject.setProperty("translation", translation);
        domainObject.setProperty("processName", null);
        domainObject.setProperty("viewName", null);

        return domainObject;
    }


    private Map<String, DomainType> domainTypeMap(DomainType... types)
    {
        HashMap<String, DomainType> map = new HashMap<>();

        for (DomainType type : types)
        {
            map.put(type.getName(), type);
        }
        return map;
    }



    private class TestDomainService
        extends TestDomainServiceBase
    {

        private final ModelSchemaService modelSchemaService;

        private Map<String,DomainType> domainTypes;

        private DomainType createDomainType(String name, List<DomainProperty> props)
        {
            DomainTypeModel domainType = new DomainTypeModel();
            domainType.setDomainService(this);
            domainType.setName(name);
            domainType.setProperties(props);
            return domainType;
        }

        public TestDomainService(ModelSchemaService modelSchemaService)
        {

            this.modelSchemaService = modelSchemaService;
            domainTypes = domainTypeMap(
                createDomainType("Foo", ImmutableList.of(
                    DomainProperty.builder().withName("name").withType(PropertyType.PLAIN_TEXT).build(),
                    DomainProperty.builder().withName("created").withType(PropertyType.TIMESTAMP).build()
                )),
                createDomainType("CContainer", ImmutableList.of(
                    DomainProperty.builder().withName("name").withType(PropertyType.PLAIN_TEXT).withDomainType
                        ("CContainer").build(),
                    DomainProperty.builder().withName("created").withType(PropertyType.TIMESTAMP).withDomainType("CContainer").build(),
                    DomainProperty.builder().withName("bazes").withType(PropertyType.LIST, "Baz").withDomainType("CContainer").build()
                )),
                createDomainType("PLContainer", ImmutableList.of(
                    DomainProperty.builder().withName("name").withType(PropertyType.PLAIN_TEXT).withDomainType
                        ("PLContainer").build(),
                    DomainProperty.builder().withName("created").withType(PropertyType.TIMESTAMP).withDomainType("PLContainer").build(),
                    DomainProperty.builder().withName("dates").withType(PropertyType.LIST,PropertyType.TIMESTAMP).withDomainType("PLContainer").build()
                )),
                createDomainType("MapContainer", ImmutableList.of(
                    DomainProperty.builder().withName("name").withType(PropertyType.PLAIN_TEXT).withDomainType
                        ("MapContainer").build(),
                    DomainProperty.builder().withName("created").withType(PropertyType.TIMESTAMP).withDomainType
                        ("MapContainer").build(),
                    DomainProperty.builder().withName("bazes").withType(PropertyType.MAP,"Baz").withDomainType("MapContainer").build()
                )),
                createDomainType("PropMapContainer", ImmutableList.of(
                    DomainProperty.builder().withName("name").withType(PropertyType.PLAIN_TEXT).withDomainType
                        ("PropMapContainer").build(),
                    DomainProperty.builder().withName("created").withType(PropertyType.TIMESTAMP).withDomainType
                        ("PropMapContainer").build(),
                    DomainProperty.builder().withName("dates").withType(PropertyType.MAP, PropertyType.TIMESTAMP).withDomainType("PropMapContainer").build()
                )),
                createDomainType("Bar", ImmutableList.of(
                    DomainProperty.builder().withName("createdWithADifferentName").withType(PropertyType.TIMESTAMP)
                        .build()
                )),
                createDomainType("Baz", ImmutableList.of(
                    DomainProperty.builder().withName("created").withType(PropertyType.TIMESTAMP).build()
                )),
                createDomainType("Recursive", ImmutableList.of(
                    DomainProperty.builder().withName("created").withType(PropertyType.TIMESTAMP).build(),
                    DomainProperty.builder().withName("kids").withType("List", "Recursive").build()
                )),
                createDomainType("AppTranslation", ImmutableList.of(
                    DomainProperty.builder().withName("id").withType("UUID").setRequired(true).withMaxLength(36).build(),
                    DomainProperty.builder().withName("locale").withType(PropertyType.PLAIN_TEXT).setRequired(true).withMaxLength(64).build(),
                    DomainProperty.builder().withName("tag").withType(PropertyType.PLAIN_TEXT).setRequired(true).withMaxLength(255).build(),
                    DomainProperty.builder().withName("translation").withType(PropertyType.PLAIN_TEXT).setRequired(true).build(),
                    DomainProperty.builder().withName("created").withType(PropertyType.TIMESTAMP).setRequired(true).build(),
                    DomainProperty.builder().withName("processName").withType(PropertyType.PLAIN_TEXT).withMaxLength(64).build(),
                    DomainProperty.builder().withName("viewName").withType(PropertyType.PLAIN_TEXT).withMaxLength(64).build()
                ))
            );

            domainTypes.putAll(
                modelSchemaService.getModelDomainTypes().entrySet().stream()
                    .collect(Collectors.toMap(
                        e -> e.getValue().getName(),
                        Map.Entry::getValue)
                    )
            );
        }


        @Override
        public Map<String, DomainType> getDomainTypes()
        {
            return domainTypes;
        }


        @Override
        public DomainType getDomainType(String name)
        {
            return domainTypes.get(name);
        }
    }
}
