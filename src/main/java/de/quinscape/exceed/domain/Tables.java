/**
 * This class is generated by jOOQ
 */
package de.quinscape.exceed.domain;


import de.quinscape.exceed.domain.tables.AppUser;
import de.quinscape.exceed.domain.tables.PersistentLogins;

import javax.annotation.Generated;


/**
 * Convenience access to all tables in public
 */
@Generated(
	value = {
		"http://www.jooq.org",
		"jOOQ version:3.6.2"
	},
	comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Tables {

	/**
	 * The table public.app_user
	 */
	public static final AppUser APP_USER = de.quinscape.exceed.domain.tables.AppUser.APP_USER;

	/**
	 * The table public.persistent_logins
	 */
	public static final PersistentLogins PERSISTENT_LOGINS = de.quinscape.exceed.domain.tables.PersistentLogins.PERSISTENT_LOGINS;
}
