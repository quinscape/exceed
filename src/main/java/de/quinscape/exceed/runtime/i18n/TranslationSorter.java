package de.quinscape.exceed.runtime.i18n;

import de.quinscape.exceed.domain.tables.pojos.AppTranslation;

import java.util.Comparator;

/**
 * Sorts AppTranslation items by reference specificity.
 *
 * The current order is
 *
 * <ul>
 *     <li>Translations with view rule and process rule</li>
 *     <li>Translations only process rule</li>
 *     <li>Translations without view or process rule</li>
 * </ul>
 *
 */
public final class TranslationSorter
    implements Comparator<AppTranslation>
{
    public static final TranslationSorter INSTANCE = new TranslationSorter();

    private TranslationSorter()
    {
    }

    @Override
    public int compare(AppTranslation a, AppTranslation b)
    {
        int scoreA = score(a);
        int scoreB = score(b);

        return scoreB - scoreA;
    }


    private int score(AppTranslation a)
    {
        return
            (a.getViewName() != null ? 1 : 0) +
            (a.getProcessName() != null ? 1 : 0);
    }
}
