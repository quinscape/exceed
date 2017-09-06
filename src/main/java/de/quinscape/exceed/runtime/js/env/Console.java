package de.quinscape.exceed.runtime.js.env;

import jdk.nashorn.api.scripting.AbstractJSObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Nashorn js console object
 */
public class Console
{
    private final static Logger log = LoggerFactory.getLogger(Console.class);

    public static Map<String,Object> getConsoleAPI()
    {
        Map<String, Object> map = new HashMap<>();

        map.put("debug", new LogFunction(ConsoleLevel.DEBUG));
        map.put("log", new LogFunction(ConsoleLevel.LOG));
        map.put("info", new LogFunction(ConsoleLevel.INFO));
        map.put("warn", new LogFunction(ConsoleLevel.WARN));
        map.put("error", new LogFunction(ConsoleLevel.ERROR));
        map.put("dir", new DirFunction());
        map.put("trace", new TraceFunction());
        map.put("assert", new AssertFunction());

        map.put("time", NoOpFunction.INSTANCE);
        map.put("timeEnd", NoOpFunction.INSTANCE);

        return map;
    }

    private static class LogFunction
        extends AbstractJSObject
    {
        private final ConsoleLevel level;


        private LogFunction(ConsoleLevel level)
        {
            this.level = level;
        }


        @Override
        public boolean isFunction()
        {
            return true;
        }


        @Override
        public Object call(Object thiz, Object... args)
        {
            switch (level)
            {
                case WARN:
                    if (log.isWarnEnabled())
                    {
                        log.warn(inspect(args));
                    }
                    break;
                case ERROR:
                    if (log.isErrorEnabled())
                    {
                        log.error(inspect(args));
                    }
                    break;

                case DEBUG:
                    if (log.isDebugEnabled())
                    {
                        log.debug(inspect(args));
                    }
                    break;
                case LOG:
                case INFO:
                default:
                    if (log.isInfoEnabled())
                    {
                        log.info(inspect(args));
                    }
                    break;

            }

            return null;
        }


        private String inspect(Object[] args)
        {
            StringBuilder sb = new StringBuilder();
            for (Object arg : args)
            {
                sb.append(InspectUtil.inspect(arg)).append(" ");
            }

            return sb.toString();
        }
    }

    private static class TraceFunction
        extends AbstractJSObject
    {
        @Override
        public boolean isFunction()
        {
            return true;
        }


        @Override
        public Object call(Object thiz, Object... args)
        {
            if (log.isInfoEnabled())
            {
                final StackTraceElement[] stackTrace = new Throwable().getStackTrace();
                StringBuilder sb = new StringBuilder();
                sb.append("Js stacktrace\n");
                for (StackTraceElement element: stackTrace)
                {
                    sb.append("    at ")
                        .append(element.getClassName())
                        .append('.')
                        .append(element.getMethodName())
                        .append('(')
                        .append(element.getFileName())
                        .append(':')
                        .append(element.getLineNumber())
                        .append(")\n");
                }

                log.info(sb.toString());
            }

            return null;
        }
    }


    private static class DirFunction
        extends AbstractJSObject
    {
        @Override
        public boolean isFunction()
        {
            return true;
        }


        @Override
        public Object call(Object thiz, Object... args)
        {
            if (log.isInfoEnabled())
            {
                log.info(InspectUtil.inspect(args[0]));
            }
            return null;
        }
    }
}
