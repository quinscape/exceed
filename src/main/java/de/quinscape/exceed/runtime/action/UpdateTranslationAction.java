package de.quinscape.exceed.runtime.action;

import de.quinscape.exceed.expression.ParseException;
import de.quinscape.exceed.model.action.UpdateTranslationActionModel;
import de.quinscape.exceed.model.domain.DomainType;
import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.domain.DomainObjectBase;
import de.quinscape.exceed.runtime.domain.DomainService;
import de.quinscape.exceed.runtime.util.DBUtil;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class UpdateTranslationAction
    implements Action<UpdateTranslationActionModel>
{
    private final static Logger log = LoggerFactory.getLogger(UpdateTranslationAction.class);

    private static final String APP_TRANSLATION = "AppTranslation";


    public UpdateTranslationAction()
    {
        log.info("Create StoreAction");
    }

    @Autowired
    private DSLContext dslContext;

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE, propagation = Propagation.REQUIRES_NEW)
    public Object execute(RuntimeContext runtimeContext, UpdateTranslationActionModel model) throws ParseException
    {
        log.info("UpdateTranslationActionModel: {}", model);


        final DomainService domainService = runtimeContext.getDomainService();
        final DomainType appTranslationType = domainService.getDomainType(APP_TRANSLATION);
        final Table<Record> table = DBUtil.jooqTableFor(appTranslationType, null);

        final Field<Object> idField = DBUtil.jooqField(appTranslationType, DomainType.ID_PROPERTY);

        for (DomainObjectBase domainObject : model.getNewTranslations())
        {
            domainObject.setDomainType(APP_TRANSLATION);
            domainService.insert(domainObject);
        }

        dslContext.delete(table)
            .where(
                idField.in(model.getRemovedTranslationIds()
                )
            ).execute();

        for (DomainObjectBase changed : model.getChangedTranslations())
        {
            changed.setDomainType(APP_TRANSLATION);
            changed.update();
        }


        runtimeContext.getTranslator().refreshTranslations(runtimeContext.getApplicationModel().getName());
        return true;
    }

    @Override
    public Class<UpdateTranslationActionModel> getActionModelClass()
    {
        return UpdateTranslationActionModel.class;
    }
}
