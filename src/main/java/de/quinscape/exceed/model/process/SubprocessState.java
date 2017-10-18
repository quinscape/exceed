package de.quinscape.exceed.model.process;

import com.google.common.collect.Maps;
import de.quinscape.exceed.model.expression.ExpressionValue;

import java.util.Map;

/**
 * Represents an end state within a process.
 */
public class SubprocessState
    extends ProcessState
{
    private String processName;

    private Map<String,ExpressionValue> input;

    private Map<String,String> output;


    /**
     * Name of the process to launch as a sub process.
     *
     * @return process name
     */
    public String getProcessName()
    {
        return processName;
    }


    /**
     * Sets the name of the process to launch as a sub process.
     *
     * @param processName   sub process name
     */
    public void setProcessName(String processName)
    {
        this.processName = processName;
    }


    /**
     * Returns the input values for the sub process. Maps input value names to expression
     * values.
     *
     * @return
     */
    public Map<String, String> getInput()
    {
        Map<String, String> map = Maps.newHashMapWithExpectedSize(input.size());

        for (Map.Entry<String, ExpressionValue> e : input.entrySet())
        {
            map.put(
                e.getKey(),
                e.getValue().getValue()
            );
        }
        return map;
    }


    public void setInput(Map<String, String> input)
    {
        Map<String, ExpressionValue> map = Maps.newHashMapWithExpectedSize(input.size());

        for (Map.Entry<String, String> e : input.entrySet())
        {
            map.put(
                e.getKey(),
                ExpressionValue.forValue(
                    e.getValue(),
                    true
                )
            );
        }
        this.input = map;
    }


    /**
     * Returns the output mapping.
     * <p>
     *      It defines the returning of values from the ending subprocess and how they are mapped to context values in the
     *      current process.
     * </p>
     * <p>
     *     For example <code>{ "foo" : "bar" }</code> would map the value of the subprocess context variable "foo" to the
     *     context variable "bar" in the current process.
     * </p>
     *
     * @return
     */
    public Map<String, String> getOutput()
    {
        return output;
    }


    public void setOutput(Map<String, String> output)
    {
        this.output = output;
    }
}
