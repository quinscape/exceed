package de.quinscape.exceed.runtime.i18n;

import de.quinscape.exceed.domain.tables.pojos.AppTranslation;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.*;

public class TranslationSorterTest
{
    private final static Logger log = LoggerFactory.getLogger(TranslationSorterTest.class);


    @Test
    public void testCompare() throws Exception
    {
        List<AppTranslation> l = sort(translation("view", "process"), translation(null, null), translation(null, "process"));

        assertThat(l.get(0).getViewName(), is("view"));
        assertThat(l.get(1).getProcessName(), is("process"));
        assertThat(l.get(2).getProcessName(), is(nullValue()));

        log.info("{}", l);

    }

    private List<AppTranslation> sort(AppTranslation... translations)
    {
        List<AppTranslation> l = Arrays.asList(translations);
        Collections.sort(l, TranslationSorter.INSTANCE);
        return l;
    }

    private AppTranslation translation(String view, String process)
    {
        final AppTranslation appTranslation = new AppTranslation();
        appTranslation.setTag("Test Tag");
        appTranslation.setLocale("en_US");
        appTranslation.setViewName(view);
        appTranslation.setProcessName(process);
        return appTranslation;

    }
}
