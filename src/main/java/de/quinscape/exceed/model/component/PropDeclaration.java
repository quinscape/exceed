package de.quinscape.exceed.model.component;

import de.quinscape.exceed.expression.ASTExpression;
import de.quinscape.exceed.expression.ExpressionParser;
import de.quinscape.exceed.expression.ParseException;
import de.quinscape.exceed.model.annotation.DocumentedModelType;
import de.quinscape.exceed.model.expression.ExpressionValue;
import org.svenson.JSONParameter;
import org.svenson.JSONProperty;

/**
 * Declaration for a single property within a {@link ComponentDescriptor}.
 */
public class PropDeclaration
{
    /**
     * <code>true</code> if this prop is visible to the client as a value. Some props are only needed for server
     * operation and / or might contain expression types not available on the client per-se. Setting this to
     * <code>false</code> will cause such an attribute to be only added as a comment.
     */
    private final boolean client;

    /**
     * Contains a context expression for the value of this prop or null. <code>true</code> given in the the constructor
     * will be converted into "context".
     */
    private final String context;

    /**
     * Completion rule for this prop. Wizard dialogs can be specified by setting {@link ComponentDescriptor#componentPropWizards}
     */
    private final String rule;

    /**
     * AST for {@link #rule}
     */
    private final ASTExpression ruleExpression;

    /**
     * Proptype for for this prop.
     */
    private final PropType type;

    /**
     * Type of context this prop declaration is dependent on.
     */
    private final String contextType;

    /**
     * <code>true</code> if the prop is required for the component.
     */
    private final Boolean required;

    /**
     * Prop declaration description for autocomplete purposes.
     */
    private final String description;

    /**
     * Default value for this prop.
     */
    private final ExpressionValue defaultValue;

    public PropDeclaration(
        @JSONParameter("client")
        Boolean client,
        @JSONParameter("context")
        Object context,
        @JSONParameter("rule")
        String rule,
        @JSONParameter("type")
        PropType type,
        @JSONParameter("contextType")
        String contextType,
        @JSONParameter("required")
        Boolean required,
        @JSONParameter("description")
        String description,
        @JSONParameter("defaultValue")
        String defaultValue) throws ParseException
    {
        this.rule = rule;
        this.description = description;
        this.defaultValue = ExpressionValue.forValue(defaultValue, true);
        this.ruleExpression = ExpressionParser.parse(rule);
        this.type = type != null ? type :  PropType.PLAINTEXT;

        if (context instanceof Boolean)
        {
            context = ((Boolean) context) ? "context" : null;
        }
        else if (context != null && !(context instanceof String))
        {
            throw new IllegalArgumentException("context must be boolean or expression string.");
        }
        this.context = (String) context;
        this.contextType = contextType;
        this.client = client != null ? client : true;
        this.required = required;
    }


    /**
     * <code>true</code> if this prop is visible to the client as a value. Some props are only needed for server
     * operation and / or might contain expression types not available on the client per-se. Setting this to
     * <code>false</code> will cause such an attribute to be only added as a comment.
     */
    public boolean isClient()
    {
        return client;
    }


    /**
     * Contains a context expression for the value of this prop or null. <code>true</code> given in the the constructor
     * will be converted into "context".
     */
    @JSONProperty(ignoreIfNull = true)
    public String getContext()
    {
        return context;
    }


    /**
     * Type of context this prop declaration is dependent on.
     */
    @JSONProperty(ignoreIfNull = true)
    public String getContextType()
    {
        return contextType;
    }


    /**
     * <code>true</code> if the prop is required for the component.
     */
    public boolean isRequired()
    {
        return required != null && required;
    }


    /**
     * Proptype for for this prop.
     */
    public PropType getType()
    {
        return type;
    }


    /**
     * Completion rule for this prop. Wizard dialogs can be specified by setting {@link ComponentDescriptor#componentPropWizards}
     */
    @DocumentedModelType("Expression")
    public String getRule()
    {
        return rule;
    }

    @JSONProperty(ignore = true)
    public ASTExpression getRuleExpression()
    {
        return ruleExpression;
    }


    /**
     * Prop declaration description for autocomplete purposes.
     */
    public String getDescription()
    {
        return description;
    }


    /**
     * Default value for this prop.
     */
    @DocumentedModelType("Expression")
    public ExpressionValue getDefaultValue()
    {
        return defaultValue;
    }
}


