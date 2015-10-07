/**
 * This class is generated by jOOQ
 */
package de.quinscape.exceed.domain;


import de.quinscape.exceed.domain.tables.AppState;
import de.quinscape.exceed.domain.tables.AppUser;
import de.quinscape.exceed.domain.tables.PersistentLogins;
import de.quinscape.exceed.domain.tables.records.AppStateRecord;
import de.quinscape.exceed.domain.tables.records.AppUserRecord;
import de.quinscape.exceed.domain.tables.records.PersistentLoginsRecord;

import javax.annotation.Generated;

import org.jooq.UniqueKey;
import org.jooq.impl.AbstractKeys;


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

	public static final UniqueKey<AppStateRecord> APP_STATE_PKEY = UniqueKeys0.APP_STATE_PKEY;
	public static final UniqueKey<AppUserRecord> APP_USER_PKEY = UniqueKeys0.APP_USER_PKEY;
	public static final UniqueKey<PersistentLoginsRecord> PERSISTENT_LOGINS_PKEY = UniqueKeys0.PERSISTENT_LOGINS_PKEY;

	// -------------------------------------------------------------------------
	// FOREIGN KEY definitions
	// -------------------------------------------------------------------------


	// -------------------------------------------------------------------------
	// [#1459] distribute members to avoid static initialisers > 64kb
	// -------------------------------------------------------------------------

	private static class UniqueKeys0 extends AbstractKeys {
		public static final UniqueKey<AppStateRecord> APP_STATE_PKEY = createUniqueKey(AppState.APP_STATE, AppState.APP_STATE.ID);
		public static final UniqueKey<AppUserRecord> APP_USER_PKEY = createUniqueKey(AppUser.APP_USER, AppUser.APP_USER.ID);
		public static final UniqueKey<PersistentLoginsRecord> PERSISTENT_LOGINS_PKEY = createUniqueKey(PersistentLogins.PERSISTENT_LOGINS, PersistentLogins.PERSISTENT_LOGINS.SERIES);
	}
}
