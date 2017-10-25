/**
 * This class is generated by jOOQ
 */
package de.quinscape.exceed.domain;


import de.quinscape.exceed.domain.tables.AppState;
import de.quinscape.exceed.domain.tables.AppTranslation;
import de.quinscape.exceed.domain.tables.AppUser;
import de.quinscape.exceed.domain.tables.Bar;
import de.quinscape.exceed.domain.tables.Foo;
import de.quinscape.exceed.domain.tables.PersistentLogins;
import de.quinscape.exceed.domain.tables.records.AppStateRecord;
import de.quinscape.exceed.domain.tables.records.AppTranslationRecord;
import de.quinscape.exceed.domain.tables.records.AppUserRecord;
import de.quinscape.exceed.domain.tables.records.BarRecord;
import de.quinscape.exceed.domain.tables.records.FooRecord;
import de.quinscape.exceed.domain.tables.records.PersistentLoginsRecord;
import org.jooq.ForeignKey;
import org.jooq.UniqueKey;
import org.jooq.impl.AbstractKeys;

import javax.annotation.Generated;


/**
 * A class modelling foreign key relationships between tables of the <code>public</code> 
 * schema
 */
@Generated(
	value = {
		"http://www.jooq.org",
		"jOOQ version:3.6.2"
	},
	comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Keys {

	// -------------------------------------------------------------------------
	// IDENTITY definitions
	// -------------------------------------------------------------------------


	// -------------------------------------------------------------------------
	// UNIQUE and PRIMARY KEY definitions
	// -------------------------------------------------------------------------

	public static final UniqueKey<AppStateRecord> PK_APP_STATE = UniqueKeys0.PK_APP_STATE;
	public static final UniqueKey<AppTranslationRecord> PK_APP_TRANSLATION = UniqueKeys0.PK_APP_TRANSLATION;
	public static final UniqueKey<AppUserRecord> PK_APP_USER = UniqueKeys0.PK_APP_USER;
	public static final UniqueKey<BarRecord> PK_BAR = UniqueKeys0.PK_BAR;
	public static final UniqueKey<FooRecord> PK_FOO = UniqueKeys0.PK_FOO;
	public static final UniqueKey<PersistentLoginsRecord> PERSISTENT_LOGINS_PKEY = UniqueKeys0.PERSISTENT_LOGINS_PKEY;

	// -------------------------------------------------------------------------
	// FOREIGN KEY definitions
	// -------------------------------------------------------------------------

	public static final ForeignKey<BarRecord, FooRecord> BAR__FK_BAR_FOO_ID = ForeignKeys0.BAR__FK_BAR_FOO_ID;
	public static final ForeignKey<FooRecord, AppUserRecord> FOO__FK_FOO_OWNER = ForeignKeys0.FOO__FK_FOO_OWNER;

	// -------------------------------------------------------------------------
	// [#1459] distribute members to avoid static initialisers > 64kb
	// -------------------------------------------------------------------------

	private static class UniqueKeys0 extends AbstractKeys {
		public static final UniqueKey<AppStateRecord> PK_APP_STATE = createUniqueKey(AppState.APP_STATE, AppState.APP_STATE.ID);
		public static final UniqueKey<AppTranslationRecord> PK_APP_TRANSLATION = createUniqueKey(AppTranslation.APP_TRANSLATION, AppTranslation.APP_TRANSLATION.ID);
		public static final UniqueKey<AppUserRecord> PK_APP_USER = createUniqueKey(AppUser.APP_USER, AppUser.APP_USER.ID);
		public static final UniqueKey<BarRecord> PK_BAR = createUniqueKey(Bar.BAR, Bar.BAR.ID);
		public static final UniqueKey<FooRecord> PK_FOO = createUniqueKey(Foo.FOO, Foo.FOO.ID);
		public static final UniqueKey<PersistentLoginsRecord> PERSISTENT_LOGINS_PKEY = createUniqueKey(PersistentLogins.PERSISTENT_LOGINS, PersistentLogins.PERSISTENT_LOGINS.SERIES);
	}

	private static class ForeignKeys0 extends AbstractKeys {
		public static final ForeignKey<BarRecord, FooRecord> BAR__FK_BAR_FOO_ID = createForeignKey(de.quinscape.exceed.domain.Keys.PK_FOO, Bar.BAR, Bar.BAR.FOO_ID);
		public static final ForeignKey<FooRecord, AppUserRecord> FOO__FK_FOO_OWNER = createForeignKey(de.quinscape.exceed.domain.Keys.PK_APP_USER, Foo.FOO, Foo.FOO.OWNER);
	}
}
