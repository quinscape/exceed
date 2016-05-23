/**
 * This class is generated by jOOQ
 */
package de.quinscape.exceed.domain.tables;


import de.quinscape.exceed.domain.Keys;
import de.quinscape.exceed.domain.Public;
import de.quinscape.exceed.domain.tables.records.AppStateRecord;

import java.util.Arrays;
import java.util.List;

import javax.annotation.Generated;

import org.jooq.Field;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.UniqueKey;
import org.jooq.impl.TableImpl;


/**
 * This class is generated by jOOQ.
 */
@Generated(
	value = {
		"http://www.jooq.org",
		"jOOQ version:3.6.2"
	},
	comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class AppState extends TableImpl<AppStateRecord> {

	private static final long serialVersionUID = 1044752296;

	/**
	 * The reference instance of <code>public.app_state</code>
	 */
	public static final AppState APP_STATE = new AppState();

	/**
	 * The class holding records for this type
	 */
	@Override
	public Class<AppStateRecord> getRecordType() {
		return AppStateRecord.class;
	}

	/**
	 * The column <code>public.app_state.id</code>.
	 */
	public final TableField<AppStateRecord, String> ID = createField("id", org.jooq.impl.SQLDataType.VARCHAR.length(36).nullable(false), this, "");

	/**
	 * The column <code>public.app_state.name</code>.
	 */
	public final TableField<AppStateRecord, String> NAME = createField("name", org.jooq.impl.SQLDataType.VARCHAR.length(64).nullable(false), this, "");

	/**
	 * The column <code>public.app_state.path</code>.
	 */
	public final TableField<AppStateRecord, String> PATH = createField("path", org.jooq.impl.SQLDataType.VARCHAR.length(255).nullable(false), this, "");

	/**
	 * The column <code>public.app_state.status</code>.
	 */
	public final TableField<AppStateRecord, Integer> STATUS = createField("status", org.jooq.impl.SQLDataType.INTEGER.nullable(false), this, "");

	/**
	 * The column <code>public.app_state.extensions</code>.
	 */
	public final TableField<AppStateRecord, String> EXTENSIONS = createField("extensions", org.jooq.impl.SQLDataType.VARCHAR.length(255), this, "");

	/**
	 * The column <code>public.app_state.context</code>.
	 */
	public final TableField<AppStateRecord, String> CONTEXT = createField("context", org.jooq.impl.SQLDataType.CLOB, this, "");

	/**
	 * Create a <code>public.app_state</code> table reference
	 */
	public AppState() {
		this("app_state", null);
	}

	/**
	 * Create an aliased <code>public.app_state</code> table reference
	 */
	public AppState(String alias) {
		this(alias, APP_STATE);
	}

	private AppState(String alias, Table<AppStateRecord> aliased) {
		this(alias, aliased, null);
	}

	private AppState(String alias, Table<AppStateRecord> aliased, Field<?>[] parameters) {
		super(alias, Public.PUBLIC, aliased, parameters, "");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public UniqueKey<AppStateRecord> getPrimaryKey() {
		return Keys.APP_STATE_PKEY;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<UniqueKey<AppStateRecord>> getKeys() {
		return Arrays.<UniqueKey<AppStateRecord>>asList(Keys.APP_STATE_PKEY);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AppState as(String alias) {
		return new AppState(alias, this);
	}

	/**
	 * Rename this table
	 */
	public AppState rename(String name) {
		return new AppState(name, null);
	}
}
