package de.quinscape.exceed.tooling;

import com.github.javaparser.ast.comments.Comment;

import java.io.File;

/**
 * Created by sven on 07.06.16.
 */
public class JavaSourceUtil
{
    public static File sourceFile(File base, Class<?> declaringClass)
    {
        return new File(base, declaringClass.getName().replace('.', File.separatorChar) + ".java");
    }


    /**
     * Extract and filter the comment text from the javadoc
     *
     * @param javadoc
     * @return
     */
    public static String getJavaDocText(Comment javadoc)
    {
        if (javadoc != null)
        {
            String text = javadoc.getContent();
            return text.replace("*", "").trim();
        }
        else
        {
            return "";
        }
    }
}
