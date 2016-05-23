package de.quinscape.exceed.runtime.util;


import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import de.quinscape.exceed.runtime.ExceedRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.atomic.AtomicBoolean;

public final class Util
{
    public static final String LIBRARY_SOURCE_SYSTEM_PROPERTY = "exceed.library.source";

    private final static Logger log = LoggerFactory.getLogger(Util.class);


    private Util()
    {

    }


    public static String path(String path)
    {
        char separatorChar = File.separatorChar;
        if (separatorChar != '/')
        {
            return path.replace('/', separatorChar);
        }
        return path;
    }


    final static char[] BASE32_ALPHABET = "0123456789abcdefghijklmnopqrstuv".toCharArray();

    public static String base32(long value)
    {
        StringBuilder sb = new StringBuilder();
        do
        {
            int lowerBits = (int) (value & 31);
            sb.append(BASE32_ALPHABET[lowerBits]);
            value >>>= 5;
        } while (value != 0);
        return sb.reverse().toString();
    }


    private static AtomicBoolean sourceChecked = new AtomicBoolean(false);


    public static File getExceedLibrarySource()
    {
        String property = System.getProperty(LIBRARY_SOURCE_SYSTEM_PROPERTY);
        if (property == null)
        {
            return null;
        }
        File sourceDir = new File(property);

        if (!sourceChecked.get())
        {
            if (!isValidSourceDir(sourceDir))
            {
                throw new IllegalStateException("System property " + LIBRARY_SOURCE_SYSTEM_PROPERTY + " is set but " +
                    "does not point to a valid source checkout");
            }
            sourceChecked.set(true);
        }

        return sourceDir;
    }


    private static boolean isValidSourceDir(File sourceDir)
    {
        return sourceDir.isDirectory() && new File(sourceDir, path
            ("src/main/java/de/quinscape/exceed/runtime/ExceedApplicationConfiguration.java")).isFile();
    }


    public static String parentDir(String relativePath)
    {
        int pos = relativePath.lastIndexOf('/');
        if (pos < 0)
        {
            return "";
        }
        return relativePath.substring(0, pos);
    }


    public static List<String> splitAtComma(String s)
    {
        return split(s, ",");
    }


    public static List<String> split(String s, String separator)
    {
        StringTokenizer tokenizer = new StringTokenizer(s, separator);
        List<String> list = new ArrayList<>();
        while (tokenizer.hasMoreElements())
        {
            list.add(tokenizer.nextToken().trim());
        }
        return list;
    }

    public static Set<String> splitToSet(String s, String separator)
    {
        StringTokenizer tokenizer = new StringTokenizer(s, separator);
        Set<String> set = new HashSet<>();
        while (tokenizer.hasMoreElements())
        {
            set.add(tokenizer.nextToken().trim());
        }
        return set;
    }


    public static List<String> splitAtWhitespace(String line)
    {
        return split(line, " \t\r\n");
    }


    public static String parseSingleQuotedString(String str)
    {
        return parseString('\'', str);
    }


    public static String parseDoubleQuotedString(String str)
    {
        return parseString('"', str);
    }


    private static String parseString(char quoteChar, String str)
    {
        if (str.charAt(0) != quoteChar)
        {
            throw new ExceedRuntimeException(str + " starts with invalid quote char.");
        }

        int pos = 1;

        StringBuilder sb = new StringBuilder();
        boolean escape = false;
        int c;
        while (pos < str.length())
        {
            c = str.charAt(pos++);

            if (c == quoteChar && !escape)
            {
                return sb.toString();
            }

            if (c == '\\')
            {
                if (escape)
                {
                    sb.append('\\');
                }
                escape = !escape;
            }
            else if (escape)
            {
                switch((char)c)
                {
                    case '\'':
                    case '"':
                    case '/':
                        sb.append((char)c);
                        break;
                    case 'b':
                        sb.append('\b');
                        break;
                    case 'f':
                        sb.append('\f');
                        break;
                    case 'n':
                        sb.append('\n');
                        break;
                    case 'r':
                        sb.append('\r');
                        break;
                    case 't':
                        sb.append('\t');
                        break;
                    case 'u':

                        if (pos + 4 >= str.length())
                        {
                            throw new ExceedRuntimeException("Unexpected end in unicode escape");
                        }
                        int unicode = (hexValue((char)str.charAt(pos++)) << 12) + (hexValue(str.charAt(pos++)) << 8) + (hexValue(str.charAt(pos++)) << 4) + hexValue(str.charAt(pos++));
                        sb.append((char)unicode);
                        break;
                    default:
                        throw new ExceedRuntimeException("Illegal escape character "+c+" / "+Integer.toHexString(c));
                }
                escape = false;
            }
            else
            {
                // we can't use java.lang.Character.isISOControl(int) because
                // that uses an incorrect definition of control character.
                // According to RFC4627 it's 0 to 31
                if (c < 32)
                {
                    throw new ExceedRuntimeException("Illegal control character 0x"+Integer.toHexString(c));
                }
                sb.append((char)c);
            }
        }
        throw new ExceedRuntimeException("Unclosed quotes");
    }

    private final static int HEX_LETTER_OFFSET = 'A' - '9' - 1;

    public static int hexValue(char c)
    {
        int n = c;
        if (n >= 'a')
        {
            n = n & ~32;
        }

        if ( (n >= '0' && n <= '9') || (n >= 'A' && n <= 'F'))
        {
            n -= '0';
            if (n > 9)
            {
                return n - HEX_LETTER_OFFSET;
            }
            else
            {
                return n;
            }

        }
        else
        {
            throw new NumberFormatException("Invalid hex character " + c);
        }
    }

    public static int hashcodeOver(Object... objs)
    {
        int hashcode = 17;
        for (Object obj : objs)
        {
            if (obj != null)
            {
                hashcode = (hashcode + obj.hashCode()) * 37;
            }
        }
        return hashcode;
    }


    public static <K,V> Map<K, V> immutableMap(Map<K, V> map)
    {
        return map != null ? ImmutableMap.copyOf(map) : Collections.emptyMap() ;
    }

    public static <V> List<V> immutableList(List<V> list)
    {
        return list != null ? ImmutableList.copyOf(list) : Collections.emptyList() ;
    }


    public static Set<String> immutableSet(List<String> set)
    {
        return set != null ? ImmutableSet.copyOf(set) : Collections.emptySet();
    }

}
