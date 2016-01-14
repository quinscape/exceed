package de.quinscape.exceed.runtime.config;

import de.quinscape.exceed.domain.tables.pojos.AppUser;
import de.quinscape.exceed.domain.tables.records.AppUserRecord;
import de.quinscape.exceed.runtime.service.ComponentRegistry;
import org.jooq.DSLContext;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static de.quinscape.exceed.domain.Tables.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
    ModelConfiguration.class,
    DomainConfiguration.class,
    TestConfiguration.class,
    ServiceConfiguration.class,
})
@Transactional
public class DomainConfigurationTest
{
    private static Logger log = LoggerFactory.getLogger(DomainConfigurationTest.class);

    @Autowired
    private DSLContext dslContext;
    @Autowired
    private TransactionTestService testService;

    @Test
    public void testDSL() throws Exception
    {
        int deleted = dslContext.delete(APP_USER).where(APP_USER.LOGIN.eq("Footastic")).execute();

        log.info("Deleted {}", 0);

        AppUser foo = new AppUser();
        foo.setId(UUID.randomUUID().toString());
        foo.setLogin("Footastic");
        foo.setPassword("secret");
        foo.setRoles("ROLE_USER");

        AppUserRecord record = dslContext.newRecord(APP_USER, foo);
        record.store();

        List<AppUser> foos = dslContext.select().from(APP_USER).where(APP_USER.LOGIN.eq("Footastic")).fetchInto(AppUser.class);
        assertThat(foos.size(), is(1));
        assertThat(foos.get(0).getLogin(), is("Footastic"));
        assertThat(foos.get(0).getPassword(), is("secret"));

    }


//    @Test
//    public void testQuery() throws Exception
//    {
//
//        dslContext.select().from(FOO.as("f")).leftOuterJoin(BAR.as("b")).on(field("b.foo_id").eq(field("f.id"))).fetch(new RecordMapper<Record, String>()
//
//        {
//            @Override
//            public String map(Record record)
//            {
//                for (Field f : record.fields())
//                {
//                    log.info("{}", f);
//                }
//
//                return "";
//            }
//        });
//
//    }
}
