package de.quinscape.exceed.runtime.service.model;

import de.quinscape.exceed.model.ApplicationConfig;
import de.quinscape.exceed.runtime.util.JSONUtil;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.svenson.JSON;

public class ModelSchemaServiceTest
{
    private final static Logger log = LoggerFactory.getLogger(ModelSchemaServiceTest.class);


    @Test
    public void test() throws Exception
    {
        final ModelSchemaService svc = new ModelSchemaService();
        svc.init();

        log.info("Mappings: {}", JSONUtil.formatJSON(JSON.defaultJSON().forValue(svc.getModelDomainTypes().size())));
    }
}
