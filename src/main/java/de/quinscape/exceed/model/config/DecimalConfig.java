package de.quinscape.exceed.model.config;

import de.quinscape.exceed.runtime.model.InconsistentModelException;
import de.quinscape.exceed.runtime.util.JSONUtil;
import org.svenson.JSONProperty;

import javax.annotation.PostConstruct;
import java.util.Arrays;

/**
 * Encapsulates the global decimal configuration. The JSON representation is compatible with the bignumber.js
 * library, but note that we are using <code>"ROUND_DOWN"</code> or <code>"ROUND_HALF_EVEN"</code> strings
 * instead of the normal numeric values / <code>BigNumber.ENUM_VAR</code> configuration.
 * <p>
 * <a href="http://mikemcl.github.io/bignumber.js/#config">Bignumber.js config documentation</a>
 */
public class DecimalConfig
{
    /**
     * Maximum decimal places used in the current application
     */
    private RoundingMode roundingMode = RoundingMode.getDefault();

    private Object exponentialAt = Arrays.asList(-7,20);

    private boolean crypto = false;

    private ModuloMode moduloMode = ModuloMode.getDefault();

    private int powPrecision = 0;

    /**
     * Default precision for this application (0 = unlimited which is also the default)
     */
    private int defaultPrecision = 0;

    /**
     * Default decimal places for this application. (Default is 3)
     */
    private int defaultDecimalPlaces = 3;


    /**
     * Default value for trailingZeroes: Determines whether trailing fractional decimal places are padded to the amount of decimal places
     */
    private boolean defaultTrailingZeroes = false;

    private DecimalFormat format = new DecimalFormat();


    @JSONProperty("ROUNDING_MODE")
    public RoundingMode getRoundingMode()
    {
        return roundingMode;
    }


    public void setRoundingMode(RoundingMode roundingMode)
    {
        this.roundingMode = roundingMode;
    }


    @JSONProperty("EXPONENTIAL_AT")
    public Object getExponentialAt()
    {
        return exponentialAt;
    }


    public void setExponentialAt(Object exponentialAt)
    {
        this.exponentialAt = exponentialAt;
    }


    @JSONProperty("CRYPTO")
    public boolean isCrypto()
    {
        return crypto;
    }


    public void setCrypto(boolean crypto)
    {
        this.crypto = crypto;
    }


    @JSONProperty("MODULO_MODE")
    public ModuloMode getModuloMode()
    {
        return moduloMode;
    }


    public void setModuloMode(ModuloMode moduloMode)
    {
        this.moduloMode = moduloMode;
    }


    @JSONProperty("POW_PRECISION")
    public int getPowPrecision()
    {
        return powPrecision;
    }


    public void setPowPrecision(int powPrecision)
    {
        this.powPrecision = powPrecision;
    }


    @JSONProperty("FORMAT")
    public DecimalFormat getFormat()
    {
        return format;
    }


    public void setFormat(DecimalFormat format)
    {
        this.format = format;
    }


    /**
     * Return the default precision for this application (0 = unlimited which is also the default). Beware that leaving
     * this default is bound to be the slowest solution if the numeric property is finally backed by a database.
     * <p>
     *     this is not part of the bignumber.js configuration
     * </p>
     *
     * @return default precision
     */
    public int getDefaultPrecision()
    {
        return defaultPrecision;
    }


    public void setDefaultPrecision(int defaultPrecision)
    {
        this.defaultPrecision = defaultPrecision;
    }


    public int getDefaultDecimalPlaces()
    {
        return defaultDecimalPlaces;
    }

    public void setDefaultDecimalPlaces(int defaultDecimalPlaces)
    {
        this.defaultDecimalPlaces = defaultDecimalPlaces;
    }

    @PostConstruct
    public void validate()
    {
        if (defaultPrecision != 0 && defaultPrecision < defaultDecimalPlaces)
        {
            throw new InconsistentModelException("Default precision (" + defaultPrecision + ") must be greater or equal to the default decimal places (" + defaultDecimalPlaces + ")");
        }
    }

    public static void main(String[] args)
    {
        System.out.println(
            JSONUtil.formatJSON(
                JSONUtil.DEFAULT_GENERATOR.forValue(
                    new DecimalConfig()
                )
            )
        );
    }
}
