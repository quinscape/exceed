package de.quinscape.exceed.tooling;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseException;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.Introspector;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Encapsulates the javadocs extracted from a java source.
 */
public class JavaDocs
{
    private final static Logger log = LoggerFactory.getLogger(JavaDocs.class);

    /**
     * Javadoc for the class itself
     */
    private final String classDoc;

    /**
     * Map of java property names to the javadoc for that property
     */
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
        public void visit(MethodDeclaration methodDecl, Object arg)
        {
            Comment javadoc = methodDecl.getComment();
            if (javadoc != null)
            {
                final String javaDocText = JavaSourceUtil.getJavaDocText(javadoc);

                final String methodName = methodDecl.getName();
                boolean getter = methodName.startsWith("get");
                boolean setter = methodName.startsWith("set");
                boolean isser = methodName.startsWith("is");
                if (
                    (methodDecl.getParameters().size() == 0 && getter || isser) ||
                    (methodDecl.getParameters().size() == 1 && setter)
                )
                {
                    String propName = Introspector.decapitalize(methodName.substring(isser ? 2 : 3));
                    javaDocTexts.put(propName, javaDocText);
                }
                else
                {
                    final String key = getMethodKey(methodDecl, methodName);
                    javaDocTexts.put(key, javaDocText);
                }
            }
        }


        private String getMethodKey(MethodDeclaration methodDecl, String methodName)
        {
            StringBuilder sb = new StringBuilder();
            sb.append(methodName);
            sb.append("(");
            List<Parameter> parameters = methodDecl.getParameters();
            for (int i = 0; i < parameters.size(); i++)
            {
                if (i > 0)
                    sb.append(",");

                Parameter parameter = parameters.get(i);
                sb.append(degenerify(parameter.getType().toStringWithoutComments()));
            }
            sb.append(")");
            return sb.toString();
        }


        private String degenerify(String type)
        {
            final int pos = type.indexOf('<');
            if (pos >= 0)
            {
                return type.substring(0, pos);
            }
            return type;
        }
    }



    public String getClassDoc()
    {
        return classDoc;
    }


    public Map<String, String> getPropertyDocs()
    {
        if (propertyDocs == null)
        {
            return Collections.emptyMap();
        }

        return propertyDocs;
    }

}
