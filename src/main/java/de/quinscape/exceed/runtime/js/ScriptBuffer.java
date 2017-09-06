package de.quinscape.exceed.runtime.js;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Collects the code generation result as two sets of code snippets with their unique names.
 */
public final class ScriptBuffer
{
    private Set<String> names = new HashSet<>();

    /**
     * Maps result code to its identifier
     */
    private Map<String,String> codeToIdentifier = new HashMap<>();

    /**
     * Maps pushed constant code to its identifier
     */
    private Map<String,String> pushedToIdentifier = new HashMap<>();

    /**
     * Contains a set of alias names. Maps each identifier in {@link #codeToIdentifier} and {@link #pushedToIdentifier}
     * to the set of aliases that code snippet was reused as for being the exact same source code
     */
    private Map<String,Set<String>> aliases = new HashMap<>();

    /**
     * Returns a new identifier for the given identifier base. If the base does not exist as identifier in the current
     * set, the base itself is returned. Otherwise the method adds increasing numbers to the base identifier until it
     * is unique.
     *
     * @param base      identifier base
     *
     * @return  unique identifier starting with the base identifier
     */
    public String getIdentifier(String base)
    {
        base = filter(base);

        if (names.contains(base))
        {
            int count = 2;
            String name;
            do
            {
                name = base + count++;

            } while (names.contains(name));

            names.add(name);
            return name;
        }
        else
        {
            names.add(base);
            return base;
        }
    }


    /**
     * Returns a replacement string for a given invalid character.
     *
     * @param c     character
     * @return  replacement string
     */
    private static String replacement(char c)
    {
        switch (c)
        {
            case 'ä':
                return "ae";
            case 'ö':
                return "oe";
            case 'ü':
                return "ue";
            case 'Ä':
                return "ae";
            case 'Ö':
                return "Oe";
            case 'Ü':
                return "UE";
            case 'ß':
                return "ss";
            case '-':
            case ' ':
                return "_";
            case '\u1e9e':
                return "SS";

            default:
                // we just swallow all other invalid characters. The algorithm will prevent collisions in any case
                return "";
        }
    }


    /**
     * Filters the given identifier base to contain only valid characters.
     *
     * @param base      identifier base
     *
     * @return filtered identifier base
     */
    private static String filter(String base)
    {
        StringBuilder sb = new StringBuilder();
        char first = base.charAt(0);
        if (isValidStart(first))
        {
            sb.append(first);
        }
        else
        {
            sb.append(replacement(first));
        }

        for (int i = 1; i < base.length(); i++)
        {
            char c = base.charAt(i);
            if (isValidPart(c))
            {
                sb.append(c);
            }
            else
            {
                sb.append(replacement(c));
            }
        }

        return sb.toString();
    }


    /**
     * Returns <code>true</code> if the given character is valid as first character of an identifier.
     *
     * <p>
     *      We use a limited set of characters compared to {@link Character#isJavaIdentifierStart(char)} for readability and
     *      compatibility with js.
     * </p>
     *
     * @param c     character
     * @return  <code>true</code> if valid as first character of an identifier
     */
    private static boolean isValidStart(char c)
    {
        return c == '_' || (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
    }


    /**
     * Returns <code>true</code> if the given character is valid as part an identifier.
     * <p>
     *      We use a limited set of characters compared to {@link Character#isJavaIdentifierPart(char)} for readability and
     *      compatibility with js.
     * </p>
     *
     * @param c     character
     * @return  <code>true</code> if valid as part an identifier
     */
    private static boolean isValidPart(char c)
    {
        return c == '_' || (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9');
    }


    /**
     * Removes the given identifier from the set.
     *
     * @param identifier    identifier
     *
     * @return <code>true</code> if the identifier was part of the set before being removed now.
     */
    public boolean removeIdentifier(String identifier)
    {
        return names.remove(identifier);
    }


    /**
     * Adds the given Javascript code value to the script buffer. If there already is an entry with the exact same
     * code, that code value is reused.
     *
     * @param name      identifier prefix
     * @param code      Javascript code
     *
     * @return unique identifier the code was stored under
     */
    public String addCodeBlock(String name, String code)
    {
        return addCodeBlock(codeToIdentifier, name, code);
    }


    /**
     * Pushes the given Javascript code value to the script buffer. If there already is an entry with the exact same
     * code, that code value is reused.
     * <p>
     *     Pushed code is kept in a separate map and is generated so that it lies upwards the Javascript scope chain to
     *     be reusable without instantiation penalties
     * </p>
     *
     * @param name      identifier prefix
     * @param code      Javascript code
     *
     * @return unique identifier the pushed code was stored under
     */
    public String pushCodeBlock(String name, String code)
    {
        return addCodeBlock(pushedToIdentifier, name, code);
    }


    /**
     * Internal method to add the code blocks to the strangely mapped code maps. We map code to name for an easy
     * elimination of code duplicates.
     *
     * @param codeMap   codeMap to add the code to
     * @param name      identifier prefix
     * @param code      code
     *
     * @return unique identifier the code was stored under
     */
    private String addCodeBlock(Map<String, String> codeMap, String name, String code)
    {
        final String existing = codeMap.get(code);
        if (existing == null)
        {
            final String unique = getIdentifier(name);
            codeMap.put(code, unique);
            return unique;
        }
        else
        {
            Set<String> set = aliases.get(existing);
            if (set == null)
            {
                set = new HashSet<>();
                aliases.put(existing, set);
            }
            set.add(name);

            return existing;
        }
    }


    /**
     * Returns the set of aliases for the given unique identifier
     *
     * @param name      unique identifier
     *
     * @return set of aliases
     */
    public Set<String> getAliases(String name)
    {
        return aliases.get(name);
    }


    /**
     * Returns the map of generated code
     *
     * @return  map mapping <em>code</em> to its identifier
     */
    public Map<String, String> getResults()
    {
        return codeToIdentifier;
    }


    /**
     * Returns the map of pushed constant code
     *
     * @return  map mapping <em>code</em> to its identifier
     */
    public Map<String, String> getPushed()
    {
        return pushedToIdentifier;
    }
}
