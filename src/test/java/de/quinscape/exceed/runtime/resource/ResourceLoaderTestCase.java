package de.quinscape.exceed.runtime.resource;

import de.quinscape.exceed.runtime.resource.stream.ClassPathResourceRoot;
import de.quinscape.exceed.runtime.resource.file.FileResourceRoot;
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
        ClassPathResourceRoot base = new ClassPathResourceRoot("resource-base");
        FileResourceRoot ext = new FileResourceRoot(new File("" +
            "./src/test/java/de/quinscape/exceed/runtime/resource/resource-ext"), false);
        FileResourceRoot ext2 = new FileResourceRoot(new File("" +
            "./src/test/java/de/quinscape/exceed/runtime/resource/resource-ext2"), false);

        List<ResourceRoot> resourceRoots = Arrays.asList(base,ext,ext2);

        ResourceLoader resourceLoader = new ResourceLoader(resourceRoots);

        ResourceLocation resource = resourceLoader.getResourceLocation("/a.json");

        assertThat(resource, is(notNullValue()));
        assertThat(resource.getHighestPriorityResource().getResourceRoot(), is(base));
        assertThat(resourceLoader.getResourceLocation("/b.json").getHighestPriorityResource().getResourceRoot(), is(ext2));
        assertThat(resourceLoader.getResourceLocation("/c.json").getHighestPriorityResource().getResourceRoot(), is(ext));
        assertThat(resourceLoader.getResourceLocation("/d.json").getHighestPriorityResource().getResourceRoot(), is(ext2));

        assertThat(JSONParser.defaultJSONParser().parse(resourceLoader.readResource("/a.json")), is("a-base"));
        assertThat(JSONParser.defaultJSONParser().parse(resourceLoader.readResource("/b.json")), is("b-ext2"));
        assertThat(JSONParser.defaultJSONParser().parse(resourceLoader.readResource("/c.json")), is("c-ext"));
        assertThat(JSONParser.defaultJSONParser().parse(resourceLoader.readResource("/d.json")), is("d-ext2"));

    }
}
