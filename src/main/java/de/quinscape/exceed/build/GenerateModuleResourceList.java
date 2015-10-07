package de.quinscape.exceed.build;

import de.quinscape.exceed.runtime.resource.file.JSONFileFilter;
import de.quinscape.exceed.runtime.util.Util;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;

import java.io.File;
import java.io.IOException;

/**
 * Generates a list of module resources packaged with the exceed library
 *
 * @goal generatefilelist
 * @phase generate-resources
 */
public class GenerateModuleResourceList
{
    public static void main(String[] args) throws IOException
    {
        System.out.println("Generating model resource list...");

        if (args.length != 2)
        {
            usage();
        }
        else
        {
            File baseDir = new File(args[0]);
            File targetFile = new File(args[1]);

            if (!baseDir.isDirectory())
            {
                System.err.println(baseDir + " is no directory");
                usage();
            }
            else
            {
                File parentFile = targetFile.getParentFile();
                if (!parentFile.isDirectory())
                {
                    if (!parentFile.mkdirs())
                    {
                        throw new RuntimeException("Could not create " + parentFile );
                    }
                }
                new GenerateModuleResourceList().execute(baseDir, targetFile);
            }
        }
    }

    private static void usage()
    {
        System.err.println("Usage: de.quinscape.exceed.build.GenerateModuleResourceList <model-base> <target-file>");
    }

    public void execute(File baseModulesDirectory, File targetFile) throws IOException
    {
        int start = baseModulesDirectory.getPath().length();

        StringBuilder sb = new StringBuilder();
        for (File file : FileUtils.listFiles(baseModulesDirectory, TrueFileFilter.INSTANCE, TrueFileFilter
            .INSTANCE))
        {
            sb.append(Util.path(file.getPath().substring(start))).append('\n');
        }

        FileUtils.writeStringToFile(targetFile, sb.toString(), "UTF-8");
    }
}
