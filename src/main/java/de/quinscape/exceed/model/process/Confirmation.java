package de.quinscape.exceed.model.process;

import de.quinscape.exceed.model.expression.ExpressionValue;
import org.svenson.JSONProperty;

public class Confirmation
{
    private static final ExpressionValue DEFAULT_OK_LABEL = ExpressionValue.forValue("i18n('Ok')", true);
    private static final ExpressionValue DEFAULT_TITLE = ExpressionValue.forValue("i18n('Confirm')", true);

    private ExpressionValue okLabel = DEFAULT_OK_LABEL;

    private ExpressionValue message;

    private ExpressionValue title = DEFAULT_TITLE;


    public String getMessage()
    {
        return message != null ? message.getValue() : null;
    }


    /**
     * Message expression for this confirmation.
     */
    public void setMessage(String message)
    {
        this.message = ExpressionValue.forValue(message, true);
    }


    @JSONProperty(ignore = true)
    public ExpressionValue getMessageValue()
    {
        return message;
    }


    public String getOkLabel()
    {
        return okLabel != null ? okLabel.getValue() : null;
    }


    /**
     * OK-label expression for this confirmation. It is good UI practice to label confirmations with the actual operation
     * being done instead of just "Ok"
     */
    public void setOkLabel(String okLabel)
    {
        this.okLabel = ExpressionValue.forValue(okLabel, true);
    }


    @JSONProperty(ignore = true)
    public ExpressionValue getOkLabelValue()
    {
        return okLabel;
    }


    public String getTitle()
    {
        return title != null ? title.getValue() : null;
    }


    public void setTitle(String title)
    {
        this.title = ExpressionValue.forValue(title, true);
    }


    @JSONProperty(ignore = true)
    public ExpressionValue getTitleValue()
    {
        return title;
    }

    @Override
    public String toString()
    {
        return super.toString() + ": "
            + "okLabel = " + okLabel
            + ", message = " + message
            + ", title = " + title
            ;
    }
}
