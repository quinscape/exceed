package de.quinscape.exceed.tooling;

import de.quinscape.exceed.runtime.util.FileExtension;
import de.quinscape.exceed.runtime.util.Util;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;

import java.io.File;
import java.io.IOException;

/**
 * Generates a list of module resources packaged with the exceed library
 *
 * @goal generatefilelist
 * @phase generate-resources
 */
public class GenerateLibraryResourceLists
{
    public static void main(String[] args) throws IOException
    {
        GenerateLibraryResourceLists tool = new GenerateLibraryResourceLists();

        tool.generateResourceList(
            "src/main/models",
            "target/classes/de/quinscape/exceed/models/resource.lst",
            TrueFileFilter.INSTANCE);

        tool.generateResourceList(
            "src/main/js/components",
            "target/classes/de/quinscape/exceed/components/resource.lst",
            NonJsComponentFileFilter.INSTANCE);
    }


    public void generateResourceList(String dir, String targetFilePath, IOFileFilter fileFilter) throws IOException
    {
        File baseModulesDirectory = new File(dir);
        File targetFile = new File(targetFilePath);

        int start = baseModulesDirectory.getPath().length();

        StringBuilder sb = new StringBuilder();
        for (File file : FileUtils.listFiles(baseModulesDirectory, fileFilter, TrueFileFilter
            .INSTANCE))
        {
            sb.append(Util.path(file.getPath().substring(start))).append('\n');
        }

        FileUtils.writeStringToFile(targetFile, sb.toString(), "UTF-8");
    }

    private static class NonJsComponentFileFilter
        implements IOFileFilter
    {
        private final static NonJsComponentFileFilter INSTANCE = new NonJsComponentFileFilter();

        @Override
        public boolean accept(File file)
        {
            return match(file.getName());
        }

        private boolean match(String name)
        {
            return name.endsWith(FileExtension.CSS) || name.endsWith(FileExtension.JSON);
        }

        @Override
        public boolean accept(File dir, String name)
        {
            return match(name);
        }
    }
}
