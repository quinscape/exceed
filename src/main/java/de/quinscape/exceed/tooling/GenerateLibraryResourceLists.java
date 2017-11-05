package de.quinscape.exceed.tooling;

import de.quinscape.exceed.runtime.util.FileExtension;
import de.quinscape.exceed.runtime.util.Util;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FalseFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;

import java.io.File;
import java.io.IOException;
import java.util.List;

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
        System.out.println("Generating library resource list");

        GenerateLibraryResourceLists tool = new GenerateLibraryResourceLists();

        if (args.length != 2)
        {
            throw new IllegalArgumentException("GenerateLibraryResourceLists takes exatcly 2 arguments (sourceDirList, targetFileList) both semicolon separated");
        }

        List<String> sourceDirList = Util.split(args[0], ";");
        List<String> targetFileList = Util.split(args[1], ";");

        if (sourceDirList.size() != targetFileList.size())
        {
            throw new IllegalArgumentException("sourceDirList has "+ sourceDirList.size() + " entries, but targetFileList has " + targetFileList.size() + ": must be the same" );
        }

        for (int i = 0; i < sourceDirList.size(); i++)
        {
            String sourceDir = sourceDirList.get(i);
            String targetFile = targetFileList.get(i);

            if (sourceDir.endsWith("/*"))
            {
                final int baseLength = sourceDir.length() - 2;
                final File wildcardBase = new File(sourceDir.substring(0, baseLength));

                System.out.println("BASE " + wildcardBase);

                if (!wildcardBase.exists())
                {
                    throw new IllegalArgumentException(wildcardBase + " does not exist");
                }

                for (File file : FileUtils.listFilesAndDirs(wildcardBase, FalseFileFilter.INSTANCE, new SubDirFilter(wildcardBase)))
                {
                    final String path = file.getPath();

                    String target = targetFile + path.substring(baseLength) + "/resource.lst";

                    System.out.println("GENERATE " + path + " => " + target);

                    tool.generateResourceList(
                        path,
                        target,
                        TrueFileFilter.INSTANCE
                    );
                }
            }
            else
            {
                tool.generateResourceList(
                    sourceDir,
                    targetFile,
                    TrueFileFilter.INSTANCE
                );

            }
        }
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
            sb.append(Util.toSystemPath(file.getPath().substring(start))).append('\n');
        }

        FileUtils.writeStringToFile(targetFile, sb.toString(), "UTF-8");
    }


    private static class SubDirFilter
        implements IOFileFilter
    {
        private final File wildcardBase;


        public SubDirFilter(File wildcardBase)
        {
            this.wildcardBase = wildcardBase;
        }


        @Override
        public boolean accept(File file)
        {
            return file.isDirectory() && file.getParentFile().equals(wildcardBase);
        }


        @Override
        public boolean accept(File file, String s)
        {
            return accept(new File(file, s));
        }
    }
}
