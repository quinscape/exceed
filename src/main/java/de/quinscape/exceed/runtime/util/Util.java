package de.quinscape.exceed.runtime.util;


import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import de.quinscape.exceed.runtime.ExceedRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public final class Util
{
    public static final String LIBRARY_SOURCE_SYSTEM_PROPERTY = "exceed.library.source";

    private final static Logger log = LoggerFactory.getLogger(Util.class);


    private Util()
    {

    }

    public static String toSystemPath(String path)
    {
        char separatorChar = File.separatorChar;
        if (separatorChar != '/')
        {
            return path.replace(separatorChar, '/');
        }
        return path;

    }


    /**
     * Converts normed pathes with / to their system-specific counter part containing
     * {@link File#separatorChar} instead.
     *
     * @param path  path with /
     * @return path with system dependent file separator
     */
    public static String toSlashPath(String path)
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
            value = dump32(sb, value);
        } while (value != 0);
        return sb.reverse().toString();
    }


    private static long dump32(StringBuilder sb, long value)
    {
        int lowerBits = (int) (value & 31);
        sb.append(BASE32_ALPHABET[lowerBits]);
        value >>>= 5;
        return value;
    }


    /**
     * Returns a base32 representation of a 128-bit value split into two longs.
     *
     * @param lo        lower 64 bits
     * @param hi        upper 64 bits
     * @return  base32 string
     */
    public static String base32(long lo, long hi)
    {
        StringBuilder sb = new StringBuilder(26);

        long value = lo;
        for (int i=0; i < 12; i++)
        {
            value = dump32(sb, value);
        }

        // upper 4 bits of lo (unsigned) and lower bit of hi
        //noinspection NumericOverflow
        final int carry = (int) (((hi & 1) << 4) + (lo >>> 60));

        sb.append(BASE32_ALPHABET[carry]);

        value = hi >>> 1;
        for (int i=0; i < 13; i++)
        {
            value = dump32(sb, value);
        }
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
        return sourceDir.isDirectory() && new File(sourceDir, toSlashPath
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
                        int unicode = (hexValue(str.charAt(pos++)) << 12) + (hexValue(str.charAt(pos++)) << 8) + (hexValue(str.charAt(pos++)) << 4) + hexValue(str.charAt(pos++));
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

    public static int hashcodeOver(Object... objects)
    {
        int hashcode = 17;
        for (Object obj : objects)
        {
            final int objHash = obj != null ? obj.hashCode() : 0;

            hashcode = (hashcode + objHash) * 37;
        }
        return hashcode;
    }

    public static String base32UUID()
    {
        UUID uuid = UUID.randomUUID();
        return base32(uuid.getLeastSignificantBits(), uuid.getMostSignificantBits());
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

    public static <T> List<T> listOrEmpty(List<T> in)
    {
        return in == null ? Collections.emptyList() : in;
    }

    public static <T> Set<T> settOrEmpty(Set<T> in)
    {
        return in == null ? Collections.emptySet() : in;
    }


    public static String join(Collection<?> values, String sep)
    {
        StringBuilder sb = new StringBuilder();
        for (Iterator<?> iterator = values.iterator(); iterator.hasNext(); )
        {
            Object value = iterator.next();
            sb.append(value);

            if (iterator.hasNext())
            {
                sb.append(sep);
            }
        }

        return sb.toString();
    }


    public static String capitalize(String name)
    {
        final int length = name.length();
        if (length == 0)
        {
            return "";
        }
        else if (length == 1)
        {
            return name.toUpperCase();
        }
        else
        {
            return name.substring(0, 1).toUpperCase() + name.substring(1);
        }
    }


    public static String toDate(long timestamp)
    {
        return DateTimeFormatter.ISO_DATE_TIME.format(

            Instant.ofEpochMilli(timestamp)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
        );
    }


    /**
     * Inverts a map so that the former values map to the former keys.
     *
     * @param map       map to be inverted
     * @param <T>       type of map key and value
     * @return  map
     */
    public static <T> Map<T,T> invertMap(Map<T,T> map)
    {
        Map<T, T> inverted = Maps.newHashMapWithExpectedSize(map.size());

        for (Map.Entry<T, T> e : map.entrySet())
        {
            inverted.put(e.getValue(), e.getKey());
        }
        return inverted;
    }
}


