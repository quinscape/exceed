package de.quinscape.exceed.tooling;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseException;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.Introspector;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class JavaDocs
{
    private final static Logger log = LoggerFactory.getLogger(JavaDocs.class);

    private final String classDoc;

    private final Map<String, String> propertyDocs;


    public JavaDocs(File sourceFile) throws ParseException, IOException,
        IllegalAccessException, InstantiationException
    {
        CompilationUnit cu = JavaParser.parse(sourceFile);
        ClassJavaDocVisitor v1 = new ClassJavaDocVisitor();
        MethodJavaDocVisitor v2 = new MethodJavaDocVisitor();
        cu.accept(v1, null);
        cu.accept(v2, null);

        //cu.accept(new DebugVisitor(), null);

        this.classDoc = v1.getJavadoc();
        this.propertyDocs = v2.getJavaDocTexts();
    }


    static class ClassJavaDocVisitor
        extends VoidVisitorAdapter<Object>
    {
        private String javadoc;


        public String getJavadoc()
        {
            return javadoc;
        }


        @Override
        public void visit(ClassOrInterfaceDeclaration n, Object arg)
        {
            Comment javadoc = n.getComment();
            if (javadoc != null)
            {
                this.javadoc = JavaSourceUtil.getJavaDocText(javadoc);
            }

        }

    }

    static class MethodJavaDocVisitor
        extends VoidVisitorAdapter<Object>
    {
        private final Map<String, String> javaDocTexts = new HashMap<String, String>();


        public Map<String, String> getJavaDocTexts()
        {
            return javaDocTexts;
        }


        @Override
        public void visit(MethodDeclaration n, Object arg)
        {
            Comment javadoc = n.getComment();
            if (javadoc != null)
            {
                String methodName = n.getName();
                boolean getter = methodName.startsWith("get");
                boolean isser = methodName.startsWith("is");
                if (n.getParameters().size() == 0 && getter || isser)
                {
                    String propName = Introspector.decapitalize(methodName.substring(getter ? 3 : 2));
                    javaDocTexts.put(propName, JavaSourceUtil.getJavaDocText(javadoc));
                }
            }
        }
    }



    public String getClassDoc()
    {
        return classDoc;
    }


    public Map<String, String> getPropertyDocs()
    {
        return propertyDocs;
    }
}
