package de.quinscape.exceed.runtime.datalist;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import de.quinscape.exceed.TestDomainServiceBase;
import de.quinscape.exceed.model.domain.DomainProperty;
import de.quinscape.exceed.model.domain.DomainType;
import de.quinscape.exceed.runtime.component.DataGraph;
import de.quinscape.exceed.runtime.domain.DomainObject;
import de.quinscape.exceed.runtime.domain.GenericDomainObject;
import de.quinscape.exceed.runtime.domain.property.DateConverter;
import de.quinscape.exceed.runtime.domain.property.DomainTypeConverter;
import de.quinscape.exceed.runtime.domain.property.ListConverter;
import de.quinscape.exceed.runtime.domain.property.MapConverter;
import de.quinscape.exceed.runtime.domain.property.PlainTextConverter;
import de.quinscape.exceed.runtime.domain.property.PropertyConverter;
import de.quinscape.exceed.runtime.domain.property.TimestampConverter;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

public class DataGraphServiceTest
{
    private final static Logger log = LoggerFactory.getLogger(DataGraphServiceTest.class);

    // we mostly test conversion of different types  in "row" here because "types" and "columns" are just
    // straightforward JSON generation without any transformation or conversion


    private TestDomainService domainService = new TestDomainService();

    private Map<String, PropertyConverter> propertyConverters = new HashMap<>();
    {
        propertyConverters.put("PlainTextConverter", new PlainTextConverter());
        propertyConverters.put("DateConverter", new DateConverter());
        propertyConverters.put("TimestampConverter", new TimestampConverter());
        propertyConverters.put("DomainTypeConverter", new DomainTypeConverter());
        propertyConverters.put("ListConverter", new ListConverter());
        propertyConverters.put("MapConverter", new MapConverter());
    };

    private DataGraphService dataGraphService = new DataGraphService(
        domainService,
        propertyConverters
    );

    @Test
    public void testSimpleToJSON() throws Exception
    {
        DataGraph dataGraph = new DataGraph(
            ImmutableMap.of(
                "name", DomainProperty.builder().withName("name").withType("PlainText")
                    .withDomainType("Foo").build(),
                "created", DomainProperty.builder().withName("created").withType("Date")
                    .withDomainType("Foo").build()
            ),
            ImmutableList.of(
                ImmutableMap.of(
                    "name", "MyFoo", "created", new Date(0)
                )
            ),
            1);


        String json = dataGraphService.toJSON(dataGraph);

        assertThat(json, containsString("\"name\":\"MyFoo\""));
        assertThat(json, containsString("\"created\":\"1970-01-01\""));

        //log.info(JSON.formatJSON(json));
    }

    @Test
    public void testJoinedToJSON() throws Exception
    {
        DataGraph dataGraph = new DataGraph(
            ImmutableMap.of(
                "name", DomainProperty.builder().withName("name").withType("PlainText").withDomainType("Foo").build(),
                "created", DomainProperty.builder().withName("created").withType("Date").withDomainType("Foo").build(),
                "barCreated", DomainProperty.builder().withName("createdWithADifferentName").withType("Timestamp")
                    .withDomainType("Bar").build()
            ),
            ImmutableList.of(
                ImmutableMap.of(
                    "name", "MyFoo",
                    "created", new Date(TimeUnit.DAYS.toMillis(1)),
                    "barCreated", new Timestamp(TimeUnit.DAYS.toMillis(2))
                )
            ), 1);


        String json = dataGraphService.toJSON(dataGraph);

        assertThat(json, containsString("\"name\":\"MyFoo\""));
        assertThat(json, containsString("\"created\":\"1970-01-02\""));
        assertThat(json, containsString("\"barCreated\":\"1970-01-03T00:00:00Z\""));

        //log.info(JSON.formatJSON(json));
    }


    @Test
    public void testComplexListToJSON() throws Exception
    {
        DataGraph dataGraph = new DataGraph(
            ImmutableMap.of(
                "name", DomainProperty.builder().withName("name").withType("PlainText").withDomainType("CContainer").build(),
                "created", DomainProperty.builder().withName("created").withType("Date").withDomainType("CContainer").build(),
                "bazes", DomainProperty.builder().withName("bazes").withType(DomainProperty.LIST_PROPERTY_TYPE)
                    .withTypeParam("Baz").withDomainType("CContainer").build()
            ),
            ImmutableList.of(
                ImmutableMap.of(
                    "name", "MyFoo",
                    "created", new Date(TimeUnit.DAYS.toMillis(1)),
                    "bazes", ImmutableList.of(
                        createDomainObject("Baz", "created", new Timestamp(TimeUnit.DAYS.toMillis(3))),
                        createDomainObject("Baz","created", new Timestamp(TimeUnit.DAYS.toMillis(4)))
                    )
                )
            ), 1);


        String json = dataGraphService.toJSON(dataGraph);

        assertThat(json, containsString("\"name\":\"MyFoo\""));
        assertThat(json, containsString("\"created\":\"1970-01-02\""));
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
        DataGraph dataGraph = new DataGraph(
            ImmutableMap.of(
                "name", DomainProperty.builder().withName("name").withType("PlainText")
                    .withDomainType("PLContainer").build(),
                "created", DomainProperty.builder().withName("created").withType("Date")
                    .withDomainType("PLContainer").build(),
                "dates", DomainProperty.builder().withName("dates").withType(DomainProperty.LIST_PROPERTY_TYPE).withTypeParam("Date").withDomainType("PLContainer").build()
            ),
            ImmutableList.of(
                ImmutableMap.of(
                    "name", "MyFoo",
                    "created", new Date(TimeUnit.DAYS.toMillis(1)),
                    "dates", ImmutableList.of(
                        new Date(TimeUnit.DAYS.toMillis(5)),
                        new Date(TimeUnit.DAYS.toMillis(6))
                    )
                )
            ), 1);


        String json = dataGraphService.toJSON(dataGraph);

        assertThat(json, containsString("\"name\":\"MyFoo\""));
        assertThat(json, containsString("\"created\":\"1970-01-02\""));
        assertThat(json, containsString("[\"1970-01-06\",\"1970-01-07\"]"));


        //log.info(JSON.formatJSON(json));
    }


    @Test
    public void testComplexMapToJSON() throws Exception
    {
        DataGraph dataGraph = new DataGraph(
            ImmutableMap.of(
                "name", DomainProperty.builder().withName("name").withType("PlainText")
                    .withDomainType("MapContainer").build(),
                "created", DomainProperty.builder().withName("created").withType("Date")
                    .withDomainType("MapContainer").build(),
                "bazes", DomainProperty.builder().withName("bazes").withType(DomainProperty.MAP_PROPERTY_TYPE).withTypeParam("Baz").withDomainType("MapContainer").build()
            ),
            ImmutableList.of(
                ImmutableMap.of(
                    "name", "MyFoo",
                    "created", new Date(TimeUnit.DAYS.toMillis(1)),
                    "bazes", ImmutableMap.of(
                        "Bar1", createDomainObject("Baz",
                            "created", new Timestamp(TimeUnit.DAYS.toMillis(7))
                        ),
                        "Bar2", createDomainObject("Baz",
                            "created", new Timestamp(TimeUnit.DAYS.toMillis(8))
                        )
                    )
                )
            ), 1);


        String json = dataGraphService.toJSON(dataGraph);

        assertThat(json, containsString("\"name\":\"MyFoo\""));
        assertThat(json, containsString("\"created\":\"1970-01-02\""));
        assertThat(json, containsString("\"bazes\":{"));
        assertThat(json, containsString("\"Bar1\":{\"_type\":\"Baz\",\"created\":\"1970-01-08T00:00:00Z\""));
        assertThat(json, containsString("\"Bar2\":{\"_type\":\"Baz\",\"created\":\"1970-01-09T00:00:00Z\""));

        //log.info(JSON.formatJSON(json));
    }


    @Test
    public void testPropertyMapToJSON() throws Exception
    {
        DataGraph dataGraph = new DataGraph(
            ImmutableMap.of(
                "name", DomainProperty.builder().withName("name").withType("PlainText")
                    .withDomainType("PropMapContainer").build(),
                "created", DomainProperty.builder().withName("created").withType("Date")
                    .withDomainType("PropMapContainer").build(),
                "dates", DomainProperty.builder().withName("dates").withType(DomainProperty.MAP_PROPERTY_TYPE).withTypeParam("Date").withDomainType("PropMapContainer").build()
            ),
            ImmutableList.of(
                ImmutableMap.of(
                    "name", "MyFoo",
                    "created", new Date(TimeUnit.DAYS.toMillis(1)),
                    "dates", ImmutableMap.of(
                        "Event1", new Date(TimeUnit.DAYS.toMillis(7)),
                        "Event2", new Date(TimeUnit.DAYS.toMillis(8))
                    )
                )
            ), 1);


        String json = dataGraphService.toJSON(dataGraph);

        assertThat(json, containsString("\"name\":\"MyFoo\""));
        assertThat(json, containsString("\"created\":\"1970-01-02\""));
        assertThat(json, containsString("\"Event1\":\"1970-01-08\""));
        assertThat(json, containsString("\"Event2\":\"1970-01-09\""));


        //log.info(JSON.formatJSON(json));
    }


    @Test
    public void testStructuredObjectGraph() throws Exception
    {
        DataGraph dataGraph = new DataGraph(
            ImmutableMap.of(
                "name", DomainProperty.builder().withName("name").withType("PlainText")
                    .withDomainType("PropMapContainer").build(),
                "created", DomainProperty.builder().withName("created").withType("Date").withDomainType("PropMapContainer").build()
            ),
            ImmutableMap.of(
                "name", "MyFoo",
                "created", new Date(TimeUnit.DAYS.toMillis(1))
            ), -1);


        String json = dataGraphService.toJSON(dataGraph);

        assertThat(json, containsString("\"type\":\"OBJECT\""));
        assertThat(json, containsString("\"name\":\"MyFoo\""));
        assertThat(json, containsString("\"created\":\"1970-01-02\""));
        assertThat(json, containsString("\"count\":2"));


    }

    @Test
    public void testMapLikeGraph() throws Exception
    {
        DataGraph dataGraph = new DataGraph(
            ImmutableMap.of(
                "*", DomainProperty.builder().withName("created").withType("Date").build()
            ),
            ImmutableMap.of(
                "A", new Date(TimeUnit.DAYS.toMillis(1)),
                "B", new Date(TimeUnit.DAYS.toMillis(2))
            ), -1);


        String json = dataGraphService.toJSON(dataGraph);

        assertThat(json, containsString("\"type\":\"OBJECT\""));
        assertThat(json, containsString("\"A\":\"1970-01-02\""));
        assertThat(json, containsString("\"B\":\"1970-01-03\""));
        assertThat(json, containsString("\"count\":2"));


    }


    @Test
    public void testRecursiveType() throws Exception
    {
        DomainObject root = createDomainObject("Recursive", "created", new Date(TimeUnit.DAYS.toMillis(3)));
        DomainObject kid = createDomainObject("Recursive", "created", new Date(TimeUnit.DAYS.toMillis(4)));
        root.setProperty("kids", Collections.singletonList(kid));


        DataGraph dataGraph = new DataGraph(
            ImmutableMap.of(
                "root", DomainProperty.builder().withName("root").withType(DomainProperty.DOMAIN_TYPE_PROPERTY_TYPE).withTypeParam("Recursive").build()
            ),
            ImmutableMap.of(
                "root", root
            ), 1);


        String json = dataGraphService.toJSON(dataGraph);

        assertThat(json, containsString("\"_type\":\"Recursive\""));
        assertThat(json, containsString("\"created\":\"1970-01-04\""));
        assertThat(json, containsString("\"created\":\"1970-01-05\""));

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


    private DomainType createDomainType(String name, List<DomainProperty> props)
    {
        DomainType domainType = new DomainType();
        domainType.setName(name);
        domainType.setProperties(props);
        return domainType;
    }

    private class TestDomainService
        extends TestDomainServiceBase
    {

        private Map<String,DomainType> domainTypes =
            domainTypeMap(
            createDomainType("Foo", ImmutableList.of(
                DomainProperty.builder().withName("name").withType("PlainText").build(),
                DomainProperty.builder().withName("created").withType("Date").build()
            )),
            createDomainType("CContainer", ImmutableList.of(
                DomainProperty.builder().withName("name").withType("PlainText").withDomainType
                    ("CContainer").build(),
                DomainProperty.builder().withName("created").withType("Date").withDomainType("CContainer").build(),
                DomainProperty.builder().withName("bazes").withType(DomainProperty.LIST_PROPERTY_TYPE)
                    .withTypeParam("Baz").withDomainType("CContainer").build()
            )),
            createDomainType("PLContainer", ImmutableList.of(
                DomainProperty.builder().withName("name").withType("PlainText").withDomainType
                    ("PLContainer").build(),
                DomainProperty.builder().withName("created").withType("Date").withDomainType("PLContainer").build(),
                DomainProperty.builder().withName("dates").withType(DomainProperty.LIST_PROPERTY_TYPE)
                    .withTypeParam("Date").withDomainType("PLContainer").build()
            )),
            createDomainType("MapContainer", ImmutableList.of(
                DomainProperty.builder().withName("name").withType("PlainText").withDomainType
                    ("MapContainer").build(),
                DomainProperty.builder().withName("created").withType("Date").withDomainType
                    ("MapContainer").build(),
                DomainProperty.builder().withName("bazes").withType(DomainProperty.MAP_PROPERTY_TYPE)
                    .withTypeParam("Baz").withDomainType("MapContainer").build()
            )),
            createDomainType("PropMapContainer", ImmutableList.of(
                DomainProperty.builder().withName("name").withType("PlainText").withDomainType
                    ("PropMapContainer").build(),
                DomainProperty.builder().withName("created").withType("Date").withDomainType
                    ("PropMapContainer").build(),
                DomainProperty.builder().withName("dates").withType(DomainProperty.MAP_PROPERTY_TYPE)
                    .withTypeParam("Date").withDomainType("PropMapContainer").build()
            )),
            createDomainType("Bar", ImmutableList.of(
                DomainProperty.builder().withName("createdWithADifferentName").withType("Timestamp")
                    .build()
            )),
            createDomainType("Baz", ImmutableList.of(
                DomainProperty.builder().withName("created").withType("Timestamp").build()
            )),
            createDomainType("Recursive", ImmutableList.of(
                DomainProperty.builder().withName("created").withType("Date").build(),
                DomainProperty.builder().withName("kids").withType("List").withTypeParam("Recursive").build()
            ))
        );

        @Override
        public Map<String, DomainType> getDomainTypes()
        {
            return domainTypes;
        }


        @Override
        public PropertyConverter getPropertyConverter(String name)
        {
            return propertyConverters.get(name + "Converter");
        }


        @Override
        public DomainObject create(String type, String id)
        {

            final GenericDomainObject genericDomainObject = new GenericDomainObject();
            genericDomainObject.setDomainType(type);
            genericDomainObject.setId(id);
            return genericDomainObject;
        }
    }
}
