package de.quinscape.exceed.runtime.domain.property;

import de.quinscape.exceed.runtime.ExceedRuntimeException;
import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.js.JsEnvironment;
import jdk.nashorn.api.scripting.JSObject;
import jdk.nashorn.api.scripting.NashornScriptEngine;

import javax.script.ScriptException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Map;

public class DecimalConverter
    implements PropertyConverter<BigDecimal, String, JSObject>
{

    private final MathContext mathContext;

    public final static String DECIMAL_PLACES = "decimalPlaces";
    public final static String PRECISION = "precision";
    public final static String TRAILING_ZEROES = "trailingZeroes";

    public DecimalConverter(int decimalPlaces, RoundingMode roundingMode)
    {
        this.mathContext = new MathContext(decimalPlaces, roundingMode);
    }

    @Override
    public BigDecimal convertToJava(RuntimeContext runtimeContext, String value)
    {
        return value != null ? new BigDecimal(value, mathContext) : null;
    }


    @Override
    public String convertToJSON(RuntimeContext runtimeContext, BigDecimal value)
    {
        return value != null ? value.setScale(mathContext.getPrecision(), mathContext.getRoundingMode()).toString() : null;
    }


    public MathContext getMathContext()
    {
        return mathContext;
    }

    private static <T> T configOrDefault(Map<String,Object> config, String name, T defaultValue)
    {
        if (config == null)
        {
            return defaultValue;
        }

        return (T) config.getOrDefault(name, defaultValue);
    }

    public static int getDecimalPlaces(int defaultDecimalPlaces, Map<String, Object> config)
    {
        return ((Number)configOrDefault(config, DECIMAL_PLACES, defaultDecimalPlaces)).intValue();
    }

    public static int getPrecision(int defaultPrecision, Map<String,Object> config)
    {
        return ((Number)configOrDefault(
                config,
                PRECISION,
                defaultPrecision
            )).intValue();
    }

    @Override
    public Class<BigDecimal> getJavaType()
    {
        return BigDecimal.class;
    }


    @Override
    public Class<String> getJSONType()
    {
        return String.class;
    }


    @Override
    public JSObject convertToJs(RuntimeContext runtimeContext, BigDecimal value)
    {
        if (value == null)
        {
            return null;
        }
        final JsEnvironment environment = runtimeContext.getJsEnvironment();
        return environment.toDecimal(runtimeContext, value);
    }


    @Override
    public BigDecimal convertFromJs(RuntimeContext runtimeContext, JSObject value)
    {
        try
        {
            final NashornScriptEngine nashorn = runtimeContext.getJsEnvironment().getNashorn();
            final String stringValue = (String) nashorn.invokeMethod(value, "toString");
            return new BigDecimal(stringValue);
        }
        catch (ScriptException | NoSuchMethodException e)
        {
            throw new ExceedRuntimeException(e);
        }
    }
}
