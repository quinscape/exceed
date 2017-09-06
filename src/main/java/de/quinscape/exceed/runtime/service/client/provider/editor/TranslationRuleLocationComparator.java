package de.quinscape.exceed.runtime.service.client.provider.editor;

import de.quinscape.exceed.model.translation.TranslationRuleLocation;

import java.util.Comparator;

/**
 * Compares {@link TranslationRuleLocation}s.
 */
public class TranslationRuleLocationComparator
    implements Comparator<TranslationRuleLocation>
{
    public final static TranslationRuleLocationComparator INSTANCE = new TranslationRuleLocationComparator();


    private TranslationRuleLocationComparator()
    {
    }


    @Override
    public int compare(TranslationRuleLocation a, TranslationRuleLocation b)
    {
        final String viewA = a.getViewName();
        final String viewB = b.getViewName();

        final String processA = a.getProcessName();
        final String processB = b.getProcessName();
        if (viewA == null && viewB == null)
        {
            return processA.compareTo(processB);
        }
        else if (viewA == null)
        {
            return -1;
        }
        else if (viewB == null)
        {
            return 1;
        }
        else
        {
            // => View A and B set

            if (processA == null && processB == null)
            {
                return viewA.compareTo(viewB);
            }
            else if (processA == null)
            {
                return 1;
            }
            else if (processB == null)
            {
                return -1;
            }
            else
            {
                // => Process A and B set
                int result = processA.compareTo(processB);

                if (result != 0)
                {
                    return result;
                }
                return viewA.compareTo(viewB);
            }
        }
    }
}
