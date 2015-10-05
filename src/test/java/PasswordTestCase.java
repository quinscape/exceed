import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.UUID;

/**
 * Created by sven on 05.10.15.
 */
public class PasswordTestCase
{

    private static Logger log = LoggerFactory.getLogger(PasswordTestCase.class);

    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Test
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
