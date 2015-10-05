package de.quinscape.exceed.app.config;

import static de.quinscape.exceed.domain.Tables.APP_USER;

import de.quinscape.exceed.domain.tables.pojos.AppUser;
import de.quinscape.exceed.domain.tables.records.AppUserRecord;
import org.jooq.DSLContext;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.UUID;

@Transactional
public class TransactionTestService
{
    private final DSLContext dslContext;

    public TransactionTestService(DSLContext dslContext)
    {
        this.dslContext = dslContext;
    }

    @Transactional(propagation = Propagation.NESTED)
    public void create(String name, boolean rollback)
    {
        AppUser foo = new AppUser();
        foo.setId(UUID.randomUUID().toString());
        foo.setLastLogin(new Timestamp(System.currentTimeMillis()));
        foo.setLogin("xxx");

        AppUserRecord searcherRecord = dslContext.newRecord(APP_USER, foo);
        searcherRecord.store();

        if (rollback)
        {
            throw new RuntimeException("Rollback triggered");
        }

    }

    @Transactional
    public void deleteAll()
    {
        dslContext.delete(APP_USER).execute();
    }
}
