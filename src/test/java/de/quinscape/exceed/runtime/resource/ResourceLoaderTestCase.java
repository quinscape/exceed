package de.quinscape.exceed.runtime.resource;

import de.quinscape.exceed.runtime.resource.classpath.ClassPathExtension;
import de.quinscape.exceed.runtime.resource.file.FileBasedExtension;
import de.quinscape.exceed.runtime.resource.file.ResourceLocation;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.svenson.JSONParser;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.*;

public class ResourceLoaderTestCase
{
    private static Logger log = LoggerFactory.getLogger(ResourceLoaderTestCase.class);


    @Test
    public void testLoad() throws Exception
    {
        ResourceLoader resourceLoader = new ResourceLoader();
        List<Extension> extensions = Arrays.asList(
            new ClassPathExtension("resource-base"),
            new FileBasedExtension(new File("./src/test/java/de/quinscape/exceed/runtime/resource/resource-ext")),
            new FileBasedExtension(new File("./src/test/java/de/quinscape/exceed/runtime/resource/resource-ext2"))
        );

        ApplicationResources resources = resourceLoader.lookupResources(extensions);

        ResourceLocation resource = resources.getResourceLocation("/a.json");

        assertThat(resource, is(notNullValue()));
        assertThat(resource.getHighestPriorityResource().getExtensionIndex(), is(0));
        assertThat(resources.getResourceLocation("/b.json").getHighestPriorityResource().getExtensionIndex(), is(2));
        assertThat(resources.getResourceLocation("/c.json").getHighestPriorityResource().getExtensionIndex(), is(1));
        assertThat(resources.getResourceLocation("/d.json").getHighestPriorityResource().getExtensionIndex(), is(2));

        assertThat(JSONParser.defaultJSONParser().parse(resources.readResource("/a.json")), is("a-base"));
        assertThat(JSONParser.defaultJSONParser().parse(resources.readResource("/b.json")), is("b-ext2"));
        assertThat(JSONParser.defaultJSONParser().parse(resources.readResource("/c.json")), is("c-ext"));
        assertThat(JSONParser.defaultJSONParser().parse(resources.readResource("/d.json")), is("d-ext2"));

        log.info("resources: {}", resources);
    }
}
