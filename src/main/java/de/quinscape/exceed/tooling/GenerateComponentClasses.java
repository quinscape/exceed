package de.quinscape.exceed.tooling;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseException;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import de.quinscape.exceed.runtime.util.RequestUtil;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GenerateComponentClasses
{
    public static void main(String[] args) throws IOException, ParseException
    {
        final GenerateComponentClasses prg = new GenerateComponentClasses();
        prg.main();
    }


    public void main() throws IOException, ParseException
    {

        CompilationUnit cu = JavaParser.parse(new File("./src/main/java/de/quinscape/exceed/component/ComponentClasses.java"));

        final Visitor v = new Visitor();
        cu.accept(v, null);

        StringBuilder sb = new StringBuilder();
        sb.append("/**  \n" +
            " * These are special component descriptor signal classes that are used for purposes other than " +
            "completion checking.\n" +
            " * \n" +
            " * MODULE IS AUTO-GENERATED. DO NOT EDIT. \n" +
            " * ( Edit de.quinscape.exceed.component.ComponentClasses instead )\n" +
            " */\n" +
            "module.exports = {\n");

        for (Iterator<Definition> iterator = v.getDefinitions().iterator(); iterator.hasNext(); )
        {
            Definition definition = iterator.next();
            sb.append("    /**\n    ")
                .append(definition.getDoc())
                .append("\n    */\n    ")
                .append(definition.getName())
                .append(": ")
                .append(definition.getValue());

            if (iterator.hasNext())
            {
                sb.append(",");
            }

            sb.append("\n");

        }
        sb.append("};\n");

        FileUtils.writeStringToFile(
            new File("./src/main/js/components/component-classes.js"),
            sb.toString(),
            "UTF-8"
        );

    }

    public static class Visitor
    extends VoidVisitorAdapter<Object>
    {
        private List<Definition> definitions = new ArrayList<>();


        public List<Definition> getDefinitions()
        {
            return definitions;
        }


        @Override
        public void visit(FieldDeclaration n, Object arg)
        {
            for (VariableDeclarator variableDeclarator : n.getVariables())
            {
                definitions.add(
                    new Definition(
                        JavaSourceUtil.getJavaDocText(n.getComment()),
                        variableDeclarator.getId().getName(),
                        variableDeclarator.getInit().toString()
                    )
                );
            }
        }
    }


    public static class Definition
    {
        private final String doc;
        private final String name;
        private final String value;


        public String getDoc()
        {
            return doc;
        }


        public String getName()
        {
            return name;
        }


        public String getValue()
        {
            return value;
        }


        public Definition(String doc, String name, String value)
        {
            this.doc = doc;

            this.name = name;
            this.value = value;
        }
    }
}
