package de.quinscape.exceed.runtime.util;

import com.google.common.collect.ImmutableMap;
import de.quinscape.exceed.expression.ASTExpression;
import de.quinscape.exceed.expression.ExpressionParser;
import de.quinscape.exceed.expression.ParseException;
import de.quinscape.exceed.model.ApplicationModel;
import de.quinscape.exceed.model.context.ContextModel;
import de.quinscape.exceed.model.context.ScopedPropertyModel;
import de.quinscape.exceed.model.meta.PropertyType;
import de.quinscape.exceed.model.process.Process;
import de.quinscape.exceed.model.process.ViewState;
import de.quinscape.exceed.model.view.View;
import de.quinscape.exceed.runtime.model.ExpressionRenderer;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

public class AssignmentReplacementVisitorTest
{
    private final static Logger log = LoggerFactory.getLogger(AssignmentReplacementVisitorTest.class);

    @Test
    public void testReplacement() throws Exception
    {
        assertThat(replace("propName = 1"), is("set({name: 'propName', value: 1, path: null})"));
        assertThat(replace("objName = a"), is("set({name: 'objName', value: a, path: null})"));
        assertThat(replace("listName = b"), is("set({name: 'listName', value: b, path: null})"));

    }

    @Test
    @Ignore
    public void testPath() throws Exception
    {
        assertThat(replace("objName.val = 1"), is("set({name: 'objName', value: 1, path: 'val'})"));
        assertThat(replace("objName.val.sub = 2"), is("set({name: 'objName', value: 2, path: 'val.sub'})"));

    }

    @Test
    public void testNested() throws Exception
    {
        // nested doesn't work, is not transformed => runs into Assignment errors
        assertThat(replace("propName = (propName = 1)"), is("set({name: 'propName', value: (propName = 1), path: null})"));
    }


    @Test(expected = AssignmentReplacementException.class)
    public void testError() throws Exception
    {
        replace("foo('name') = b");
    }

    @Test(expected = AssignmentReplacementException.class)
    public void testError2() throws Exception
    {
        replace("a = b");
    }


    public String replace(String expr) throws ParseException
    {
        ASTExpression ast = ExpressionParser.parse(expr);

        final ApplicationModel applicationModel = new ApplicationModel();
        final Process process = new Process();
        final ContextModel processContext = new ContextModel();

        final ContextModel viewContext = new ContextModel();


        final ScopedPropertyModel scopedPropertyModel = new ScopedPropertyModel();
        scopedPropertyModel.setName("propName");
        scopedPropertyModel.setType(PropertyType.PLAIN_TEXT);

        final ScopedPropertyModel scopedObjectModel = new ScopedPropertyModel();
        scopedObjectModel.setName("objName");
        scopedObjectModel.setType(PropertyType.DOMAIN_TYPE);
        scopedObjectModel.setTypeParam("Foo");

        final ScopedPropertyModel scopedListModel = new ScopedPropertyModel();
        scopedListModel.setName("listName");
        scopedListModel.setType(PropertyType.DATA_LIST_PROPERTY_TYPE);

        viewContext.setProperties(ImmutableMap.of(
            "propName", scopedPropertyModel,
            "objName", scopedObjectModel,
            "listName", scopedListModel
        ));

        process.setName("testProcess");
        process.setContextModel(processContext);

        process.setStates(ImmutableMap.of("testView", new ViewState()));

        View view = new View();
        view.setName("testProcess/testView");
        view.setContextModel(viewContext);

        applicationModel.addProcess(process);
        applicationModel.addView(view);

        applicationModel.getMetaData().getScopeMetaModel().addDeclarations(view);
        applicationModel.getMetaData().getScopeMetaModel().addDeclarations(view);


        ast.jjtAccept(new AssignmentReplacementVisitor(applicationModel, "testProcess/testView"), null);
        return ExpressionRenderer.render(ast);
    }
}
