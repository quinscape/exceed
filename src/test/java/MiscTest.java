import com.jolbox.bonecp.BoneCPConfig;
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


    @Test
    public void name() throws Exception
    {
        final JSONClassInfo classInfo = JSONUtil.getClassInfo(BoneCPConfig.class);

        StringBuilder sb = new StringBuilder();

        final BoneCPConfig defaultValues = new BoneCPConfig();

        for (JSONPropertyInfo info : classInfo.getPropertyInfos())
        {
            if (!info.isReadable() || !info.isWriteable())
            {
                continue;
            }

            Object defaultValue = JSONUtil.DEFAULT_UTIL.getProperty(defaultValues, info.getJsonName());

            sb
                .append("private ")
                .append( info.getType().getSimpleName())
                .append(" ")
                .append(info.getJavaPropertyName())
                .append(" = ")
                .append(defaultValue)
                .append("\n");
        }

        log.info("{}", sb.toString());
        //log.info("{}",ExpressionValue.forValue("", true));
    }
}
