package de.quinscape.exceed.runtime.component.translation;

import java.util.Comparator;

/**
 * Created by sven on 05.07.16.
 */
public class RuleLocationComparator
    implements Comparator<RuleLocation>
{
    public final static RuleLocationComparator INSTANCE = new RuleLocationComparator();


    private RuleLocationComparator()
    {

    }


    @Override
    public int compare(RuleLocation a, RuleLocation b)
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
