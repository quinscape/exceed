package de.quinscape.exceed.model.config;

/**
 * Exceed decimal property type rounding modes.
 * <p>
 * Base on the rounding modes of ( https://mikemcl.github.io/bignumber.js/ )
 */
public enum RoundingMode
{
    ROUND_UP,
    ROUND_DOWN,
    ROUND_CEIL,
    ROUND_FLOOR,
    ROUND_HALF_UP,
    ROUND_HALF_DOWN,
    ROUND_HALF_EVEN,
    ROUND_HALF_CEIL;
    // XXX: no equivalent in BigDecimal
    //ROUND_HALF_FLOOR;


    public static RoundingMode getDefault()
    {

        return ROUND_HALF_UP;

    }

    public java.math.RoundingMode getJavaRoundingMode()
    {
        switch(this)
        {
            case ROUND_UP:
                return java.math.RoundingMode.UP;
            case ROUND_DOWN:
                return java.math.RoundingMode.DOWN;
            case ROUND_CEIL:
                return java.math.RoundingMode.CEILING;
            case ROUND_FLOOR:
                return java.math.RoundingMode.FLOOR;
            case ROUND_HALF_UP:
                return java.math.RoundingMode.HALF_UP;
            case ROUND_HALF_DOWN:
                return java.math.RoundingMode.HALF_DOWN;
            case ROUND_HALF_EVEN:
                return java.math.RoundingMode.HALF_EVEN;
            case ROUND_HALF_CEIL:
                return java.math.RoundingMode.CEILING;

            default:
                throw new IllegalStateException("Unhandled constant " + this);

        }
    }
}
