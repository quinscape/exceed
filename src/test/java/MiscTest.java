import de.quinscape.exceed.model.expression.ExpressionValue;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.UUID;

public class MiscTest
{

    private final static Logger log = LoggerFactory.getLogger(MiscTest.class);

    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();


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


    @Test
    public void name() throws Exception
    {
        log.info("{}",ExpressionValue.forValue("", true));
    }
}
