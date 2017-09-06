import de.quinscape.exceed.model.component.ComponentDescriptor;
import de.quinscape.exceed.model.component.ComponentPackageDescriptor;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.svenson.JSONParser;
import org.svenson.tokenize.InputStreamSource;

import java.io.File;
import java.io.FileInputStream;
import java.util.Collection;
import java.util.Map;

public class ComponentDescTest
{
    private final static Logger log = LoggerFactory.getLogger(ComponentDescTest.class);


    @Test
    public void testComponentRules() throws Exception
    {
        final Collection<File> files = FileUtils.listFiles(
            new File("./src/main/js"),
            new NameFileFilter("components.json"),
            TrueFileFilter.INSTANCE
        );

        for (File file : files)
        {
            ComponentPackageDescriptor descriptor = JSONParser.defaultJSONParser().parse(ComponentPackageDescriptor.class, new InputStreamSource(new FileInputStream(file), true));

            for (Map.Entry<String, ComponentDescriptor> e : descriptor.getComponents().entrySet())
            {

                final String name = e.getKey();
                ComponentDescriptor componentDescriptor = e.getValue();
                log.info("{}: child = {}, parent = {}",  name, componentDescriptor.getChildRule(), componentDescriptor.getParentRule());
            }
        }
    }
}
