package de.quinscape.exceed.model.config;

/**
 * Bignumber.js compatible decimal format specification.
 * 
 */
public class DecimalFormat
{
    /** the decimal separator
     */
    private String decimalSeparator = ".";

    /** the grouping separator of the integer part
     */
    private String groupSeparator = ",";

    /**
     the primary grouping size of the integer part. (<code>0</code> for no grouping)
     */
    private int groupSize = 3;

    /**
     the secondary grouping size of the integer part (<code>0</code> for no grouping, which is the default)
     */
    private int secondaryGroupSize = 0;

    /**
     *  the grouping separator of the fraction part
     */
    private String fractionGroupSeparator = " ";

    /**
     *  the grouping size of the fraction part
     */
    private int fractionGroupSize = 0;


    /**
     * Returns the decimal separator between integer part and fractional part.
     *
     * @return decimal separator
     */
    public String getDecimalSeparator()
    {
        return decimalSeparator;
    }


    public void setDecimalSeparator(String decimalSeparator)
    {
        this.decimalSeparator = decimalSeparator;
    }


    /**
     * Returns the grouping separator of the integer part.
     * @return grouping separator of the integer part
     */
    public String getGroupSeparator()
    {
        return groupSeparator;
    }


    public void setGroupSeparator(String groupSeparator)
    {
        this.groupSeparator = groupSeparator;
    }


    /**
     * Returns the primary grouping size of the integer part. If <code>0</code>, grouping of the integer part will be disabled.
     *
     * @return primary grouping size of the integer part
     */
    public int getGroupSize()
    {
        return groupSize;
    }


    public void setGroupSize(int groupSize)
    {
        this.groupSize = groupSize;
    }


    /**
     * Returns the secondary grouping size of the integer part
     * 
     * @return secondary grouping size of the integer part
     */
    public int getSecondaryGroupSize()
    {
        return secondaryGroupSize;
    }


    public void setSecondaryGroupSize(int secondaryGroupSize)
    {
        this.secondaryGroupSize = secondaryGroupSize;
    }


    /**
     * Returns the grouping separator of the fraction groups.
     *
     * @return grouping separator of the fraction groups
     */
    public String getFractionGroupSeparator()
    {
        return fractionGroupSeparator;
    }


    public void setFractionGroupSeparator(String fractionGroupSeparator)
    {
        this.fractionGroupSeparator = fractionGroupSeparator;
    }


    /**
     * Returns the grouping size for the fractional part. If <code>0</code>, grouping of the integer part will be disabled, which is the default.
     * 
     * @return grouping size for the fractional part
     */
    public int getFractionGroupSize()
    {
        return fractionGroupSize;
    }


    public void setFractionGroupSize(int fractionGroupSize)
    {
        this.fractionGroupSize = fractionGroupSize;
    }
}
