package de.quinscape.exceed.model.domain.type;

import de.quinscape.exceed.model.AbstractTopLevelModel;
import de.quinscape.exceed.model.TopLevelModelVisitor;
import de.quinscape.exceed.runtime.domain.property.ConverterException;
import org.svenson.JSONProperty;
import org.svenson.JSONTypeHint;

import java.util.List;

public class EnumType
    extends AbstractTopLevelModel
{
    private List<String> values;
    private String description;


    public void setValues(List<String> values)
    {
        this.values = values;
    }


    /**
     * String values for this enum
     * @return
     */
    @JSONTypeHint(String.class)
    public List<String> getValues()
    {
        return values;
    }


    /**
     * Returns the enum value for the give ordinal
     *
     * @param value ordinal
     * @return enum value
     *
     * @throws IndexOutOfBoundsException if the given ordinal is invalid.
     */
    public String forOrdinal(int value)
    {
        if (value < 0 || value >= values.size())
        {
            throw new IndexOutOfBoundsException("No enum value for index " + value);
        }
        return values.get(value);
    }


    /**
     * Return the ordinal for the given Enum constant.
     *
     * @param value     Enum value
     * @return  ordinal
     */
    public int findOrdinal(String value)
    {
        int index = values.indexOf(value);

        if (index < 0)
        {
            throw new ConverterException("Invalid enum value:" + value);
        }

        return index;
    }


    /**
     * Description for the enum type.
     */
    @JSONProperty(ignoreIfNull = true)
    public String getDescription()
    {
        return description;
    }


    public void setDescription(String description)
    {
        this.description = description;
    }


    @Override
    public <I,O> O accept(TopLevelModelVisitor<I,O> visitor, I in)
    {
        return visitor.visit(this, in);
    }
}
