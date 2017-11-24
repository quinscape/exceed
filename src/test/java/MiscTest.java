import de.quinscape.exceed.runtime.util.JSONUtil;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.svenson.info.JSONClassInfo;
import org.svenson.info.JSONPropertyInfo;

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
}
