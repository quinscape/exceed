package de.quinscape.exceed.runtime.config;

import org.jooq.DSLContext;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
    ModelConfiguration.class,
    DomainConfiguration.class,
    TestDomainConfiguration.class,
    TestConfiguration.class,
    ServiceConfiguration.class,
})
@Transactional
public class PreparedStatementTest
{
    private final static Logger log = LoggerFactory.getLogger(PreparedStatementTest.class);

    @Autowired
    private DSLContext dslContext;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    public void testDSL() throws Exception
    {
//        jdbcTemplate.e
//        jdbcTemplate.query(
//
//            con -> con.prepareStatement(""),
//            ps -> ps.setString(0,""),
//            (RowMapper<DomainObject>) rs -> {
//            return new GenericDomainObject();
//        });
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
