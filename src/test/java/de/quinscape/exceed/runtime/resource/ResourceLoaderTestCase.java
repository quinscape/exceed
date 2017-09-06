package de.quinscape.exceed.runtime.resource;

import de.quinscape.exceed.runtime.resource.file.FileResourceRoot;
import de.quinscape.exceed.runtime.resource.file.PathResources;
import de.quinscape.exceed.runtime.resource.stream.ClassPathResourceRoot;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.svenson.JSONParser;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

public class ResourceLoaderTestCase
{
    private final static Logger log = LoggerFactory.getLogger(ResourceLoaderTestCase.class);


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

        PathResources resource = resourceLoader.getResources("/a.json");

        assertThat(resource, is(notNullValue()));
        assertThat(resource.getHighestPriorityResource().getResourceRoot(), is(base));
        assertThat(resourceLoader.getResources("/b.json").getHighestPriorityResource().getResourceRoot(), is(ext2));
        assertThat(resourceLoader.getResources("/c.json").getHighestPriorityResource().getResourceRoot(), is(ext));
        assertThat(resourceLoader.getResources("/d.json").getHighestPriorityResource().getResourceRoot(), is(ext2));

        assertThat(JSONParser.defaultJSONParser().parse(resourceLoader.readResource("/a.json")), is("a-base"));
        assertThat(JSONParser.defaultJSONParser().parse(resourceLoader.readResource("/b.json")), is("b-ext2"));
        assertThat(JSONParser.defaultJSONParser().parse(resourceLoader.readResource("/c.json")), is("c-ext"));
        assertThat(JSONParser.defaultJSONParser().parse(resourceLoader.readResource("/d.json")), is("d-ext2"));

    }
}
