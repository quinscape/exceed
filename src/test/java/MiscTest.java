import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.impl.DSL;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.lang.reflect.Method;
import java.util.UUID;

public class MiscTest
{

    private final static Logger log = LoggerFactory.getLogger(MiscTest.class);

    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();


    @Test
    public void testName() throws Exception
    {
        for (Method m : Field.class.getMethods())
        {
            if (m.getParameterTypes().length == 1)
            {
                Class<?> type = m.getParameterTypes()[0];
                if (type.equals(Field.class) || type.equals(Object.class))
                {
                    log.info("DSL: {} ( {} )", m.getName(), type);
                }
            }
        }
    }


    @Test
    @Ignore
    public void testPW() throws Exception
    {
        pw("admin");
        pw("user");
        pw("editor");
    }

    private void pw(String name)
    {
        log.info("INSERT INTO app_user (id,login,password,roles) VALUES ('{}', '{}', '{}', '{}');", UUID.randomUUID().toString(), name, encoder.encode(name), "ROLE_" + name.toUpperCase());
    }
}
