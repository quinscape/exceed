package de.quinscape.exceed.tooling;

import de.quinscape.exceed.runtime.config.ExpressionConfiguration;
import de.quinscape.exceed.runtime.js.def.Definitions;
import de.quinscape.exceed.runtime.util.JSONUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.TreeMap;


public class GenerateExpressionDocs
{
    private final static Logger log = LoggerFactory.getLogger(GenerateExpressionDocs.class);


    public static void main(String[] args)
    {
        final ConfigurableApplicationContext ctx = SpringApplication.run(ExpressionConfiguration.class);

        final Definitions definitions = ctx.getBean(Definitions.class);




        System.out.println(
            JSONUtil.formatJSON(
                JSONUtil.DEFAULT_GENERATOR.forValue(
                    new TreeMap<>(
                        definitions.getDefinitions()
                    )
                )
            )
        );
    }
}
