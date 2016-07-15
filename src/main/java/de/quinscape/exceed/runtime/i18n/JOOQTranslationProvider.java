package de.quinscape.exceed.runtime.i18n;

import de.quinscape.exceed.domain.tables.pojos.AppTranslation;
import de.quinscape.exceed.runtime.RuntimeContext;
import org.jooq.DSLContext;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static de.quinscape.exceed.domain.Tables.APP_TRANSLATION;

@Transactional
public class JOOQTranslationProvider
    implements TranslationProvider
{
    private final DSLContext dslContext;

    public JOOQTranslationProvider(DSLContext dslContext)
    {
        this.dslContext = dslContext;
    }

    public List<AppTranslation> provideTranslations(RuntimeContext runtimeContext)
    {
        return dslContext.select()
            .from(APP_TRANSLATION)
            .orderBy(APP_TRANSLATION.TAG)
            .fetchInto(AppTranslation.class);
    }

}
