package de.quinscape.exceed.runtime.datalist;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import de.quinscape.exceed.model.domain.DomainProperty;
import de.quinscape.exceed.model.domain.DomainType;
import de.quinscape.exceed.runtime.component.ColumnDescriptor;
import de.quinscape.exceed.runtime.component.DataList;
import de.quinscape.exceed.runtime.domain.property.DateConverter;
import de.quinscape.exceed.runtime.domain.property.PlainTextConverter;
import de.quinscape.exceed.runtime.domain.property.TimestampConverter;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.svenson.JSON;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.*;

public class DataListServiceTest
{
    private static Logger log = LoggerFactory.getLogger(DataListServiceTest.class);

    // we mostly test conversion of different types  in "row" here because "types" and "columns" are just
    // straightforward JSON generation without any transformation or conversion

    private DataListService dataListService = new DataListService(ImmutableMap.of(
        "PlainTextConverter", new PlainTextConverter(),
        "DateConverter", new DateConverter(),
        "TimestampConverter", new TimestampConverter()
    ));

    @Test
    public void testSimpleToJSON() throws Exception
    {
        DataList dataList = new DataList(
            domainTypeMap(
                createDomainType("Foo", ImmutableList.of(
                    new DomainProperty("name", "PlainText", null, false),
                    new DomainProperty("created", "Date", null, false)
                ))
            ),
            ImmutableMap.of(
                "name", new ColumnDescriptor("Foo", "name"),
                "created", new ColumnDescriptor("Foo", "created")
            ),
            ImmutableList.of(
                ImmutableMap.of(
                    "name", "MyFoo", "created", new Date(0)
                )
            ),
            1);


        String json = dataListService.toJSON(dataList);

        assertThat(json, containsString("\"name\":\"MyFoo\""));
        assertThat(json, containsString("\"created\":\"1970-01-01T00:00Z\""));

        //log.info(JSON.formatJSON(json));
    }

    @Test
    public void testJoinedToJSON() throws Exception
    {
        DataList dataList = new DataList(
            domainTypeMap(
                createDomainType("Foo", ImmutableList.of(
                    new DomainProperty("name", "PlainText", null, false),
                    new DomainProperty("created", "Date", null, false)
                )),
                createDomainType("Bar", ImmutableList.of(
                    new DomainProperty("createdWithADifferentName", "Timestamp", null, false)
                ))
            ),
            ImmutableMap.of(
                "name", new ColumnDescriptor("Foo", "name"),
                "created", new ColumnDescriptor("Foo", "created"),
                "barCreated", new ColumnDescriptor("Bar", "createdWithADifferentName")
            ),
            ImmutableList.of(
                ImmutableMap.of(
                    "name", "MyFoo",
                    "created", new Date(TimeUnit.DAYS.toMillis(1)),
                    "barCreated", new Timestamp(TimeUnit.DAYS.toMillis(2))
                )
            ), 1);


        String json = dataListService.toJSON(dataList);

        assertThat(json, containsString("\"name\":\"MyFoo\""));
        assertThat(json, containsString("\"created\":\"1970-01-02T00:00Z\""));
        assertThat(json, containsString("\"barCreated\":\"1970-01-03T00:00:00Z\""));

        //log.info(JSON.formatJSON(json));
    }


    @Test
    public void testComplexListToJSON() throws Exception
    {
        DataList dataList = new DataList(
            domainTypeMap(
                createDomainType("Foo", ImmutableList.of(
                    new DomainProperty("name", "PlainText", null, false),
                    new DomainProperty("created", "Date", null, false),
                    new DomainProperty("bars", "List", null, false, "Bar", 0)
                )),
                createDomainType("Bar", ImmutableList.of(
                    new DomainProperty("created", "Date", null, false)
                ))
            ),
            ImmutableMap.of(
                "name", new ColumnDescriptor("Foo", "name"),
                "created", new ColumnDescriptor("Foo", "created"),
                "bars", new ColumnDescriptor("Foo", "bars")
            ),
            ImmutableList.of(
                ImmutableMap.of(
                    "name", "MyFoo",
                    "created", new Date(TimeUnit.DAYS.toMillis(1)),
                    "bars", ImmutableList.of(
                        ImmutableMap.of(
                            "created", new Date(TimeUnit.DAYS.toMillis(3))
                        ),
                        ImmutableMap.of(
                            "created", new Date(TimeUnit.DAYS.toMillis(4))
                        )
                    )
                )
            ), 1);


        String json = dataListService.toJSON(dataList);

        assertThat(json, containsString("\"name\":\"MyFoo\""));
        assertThat(json, containsString("\"created\":\"1970-01-02T00:00Z\""));
        assertThat(json, containsString("\"bars\":["));
        assertThat(json, containsString("\"created\":\"1970-01-04T00:00Z\""));
        assertThat(json, containsString("\"created\":\"1970-01-05T00:00Z\""));

        //log.info(JSON.formatJSON(json));
    }


    @Test
    public void testPropertyListToJSON() throws Exception
    {
        DataList dataList = new DataList(
            domainTypeMap(
                createDomainType("Foo", ImmutableList.of(
                    new DomainProperty("name", "PlainText", null, false),
                    new DomainProperty("created", "Date", null, false),
                    new DomainProperty("dates", "List", null, false, "Date", 0)
                ))
            ),
            ImmutableMap.of(
                "name", new ColumnDescriptor("Foo", "name"),
                "created", new ColumnDescriptor("Foo", "created"),
                "dates", new ColumnDescriptor("Foo", "dates")
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


        String json = dataListService.toJSON(dataList);

        assertThat(json, containsString("\"name\":\"MyFoo\""));
        assertThat(json, containsString("\"created\":\"1970-01-02T00:00Z\""));
        assertThat(json, containsString("[\"1970-01-06T00:00Z\",\"1970-01-07T00:00Z\"]"));


        //log.info(JSON.formatJSON(json));
    }


    @Test
    public void testComplexMapToJSON() throws Exception
    {
        DataList dataList = new DataList(
            domainTypeMap(
                createDomainType("Foo", ImmutableList.of(
                    new DomainProperty("name", "PlainText", null, false),
                    new DomainProperty("created", "Date", null, false),
                    new DomainProperty("bars", "Map", null, false, "Bar", 0)
                )),
                createDomainType("Bar", ImmutableList.of(
                    new DomainProperty("created", "Date", null, false)
                ))
            ),
            ImmutableMap.of(
                "name", new ColumnDescriptor("Foo", "name"),
                "created", new ColumnDescriptor("Foo", "created"),
                "bars", new ColumnDescriptor("Foo", "bars")
            ),
            ImmutableList.of(
                ImmutableMap.of(
                    "name", "MyFoo",
                    "created", new Date(TimeUnit.DAYS.toMillis(1)),
                    "bars", ImmutableMap.of(
                        "Bar1", ImmutableMap.of(
                            "created", new Date(TimeUnit.DAYS.toMillis(7))
                        ),
                        "Bar2", ImmutableMap.of(
                            "created", new Date(TimeUnit.DAYS.toMillis(8))
                        )
                    )
                )
            ), 1);


        String json = dataListService.toJSON(dataList);

        assertThat(json, containsString("\"name\":\"MyFoo\""));
        assertThat(json, containsString("\"created\":\"1970-01-02T00:00Z\""));
        assertThat(json, containsString("\"bars\":{"));
        assertThat(json, containsString("\"Bar1\":{\"created\":\"1970-01-08T00:00Z\""));
        assertThat(json, containsString("\"Bar2\":{\"created\":\"1970-01-09T00:00Z\""));

        //log.info(JSON.formatJSON(json));
    }


    @Test
    public void testPropertyMapToJSON() throws Exception
    {
        DataList dataList = new DataList(
            domainTypeMap(
                createDomainType("Foo", ImmutableList.of(
                    new DomainProperty("name", "PlainText", null, false),
                    new DomainProperty("created", "Date", null, false),
                    new DomainProperty("dates", "Map", null, false, "Date", 0)
                ))
            ),
            ImmutableMap.of(
                "name", new ColumnDescriptor("Foo", "name"),
                "created", new ColumnDescriptor("Foo", "created"),
                "dates", new ColumnDescriptor("Foo", "dates")
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


        String json = dataListService.toJSON(dataList);

        assertThat(json, containsString("\"name\":\"MyFoo\""));
        assertThat(json, containsString("\"created\":\"1970-01-02T00:00Z\""));
        assertThat(json, containsString("\"Event1\":\"1970-01-08T00:00Z\""));
        assertThat(json, containsString("\"Event2\":\"1970-01-09T00:00Z\""));


        //log.info(JSON.formatJSON(json));
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
}
