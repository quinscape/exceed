package de.quinscape.exceed.runtime.service.model;

import de.quinscape.exceed.runtime.util.JSONUtil;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModelSchemaServiceTest
{
    private final static Logger log = LoggerFactory.getLogger(ModelSchemaServiceTest.class);


    @Test
    public void test() throws Exception
    {
        final ModelSchemaService svc = new ModelSchemaService();
        svc.init();

        log.info("Mappings: {}", JSONUtil.formatJSON(JSONUtil.DEFAULT_GENERATOR.forValue(svc.getModelDomainTypes().size())));
    }
}
