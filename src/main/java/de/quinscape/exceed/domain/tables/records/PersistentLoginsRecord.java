/**
 * This class is generated by jOOQ
 */
package de.quinscape.exceed.domain.tables.records;


import de.quinscape.exceed.domain.tables.PersistentLogins;

import java.sql.Timestamp;

import javax.annotation.Generated;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record4;
import org.jooq.Row4;
import org.jooq.impl.UpdatableRecordImpl;


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
public class PersistentLoginsRecord extends UpdatableRecordImpl<PersistentLoginsRecord> implements Record4<String, String, String, Timestamp> {

	private static final long serialVersionUID = 1894862702;

	/**
	 * Setter for <code>public.persistent_logins.username</code>.
	 */
	public void setUsername(String value) {
		setValue(0, value);
	}

	/**
	 * Getter for <code>public.persistent_logins.username</code>.
	 */
	@NotNull
	@Size(max = 64)
	public String getUsername() {
		return (String) getValue(0);
	}

	/**
	 * Setter for <code>public.persistent_logins.series</code>.
	 */
	public void setSeries(String value) {
		setValue(1, value);
	}

	/**
	 * Getter for <code>public.persistent_logins.series</code>.
	 */
	@NotNull
	@Size(max = 64)
	public String getSeries() {
		return (String) getValue(1);
	}

	/**
	 * Setter for <code>public.persistent_logins.token</code>.
	 */
	public void setToken(String value) {
		setValue(2, value);
	}

	/**
	 * Getter for <code>public.persistent_logins.token</code>.
	 */
	@NotNull
	@Size(max = 64)
	public String getToken() {
		return (String) getValue(2);
	}

	/**
	 * Setter for <code>public.persistent_logins.last_used</code>.
	 */
	public void setLastUsed(Timestamp value) {
		setValue(3, value);
	}

	/**
	 * Getter for <code>public.persistent_logins.last_used</code>.
	 */
	@NotNull
	public Timestamp getLastUsed() {
		return (Timestamp) getValue(3);
	}

	// -------------------------------------------------------------------------
	// Primary key information
	// -------------------------------------------------------------------------

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Record1<String> key() {
		return (Record1) super.key();
	}

	// -------------------------------------------------------------------------
	// Record4 type implementation
	// -------------------------------------------------------------------------

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Row4<String, String, String, Timestamp> fieldsRow() {
		return (Row4) super.fieldsRow();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Row4<String, String, String, Timestamp> valuesRow() {
		return (Row4) super.valuesRow();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Field<String> field1() {
		return PersistentLogins.PERSISTENT_LOGINS.USERNAME;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Field<String> field2() {
		return PersistentLogins.PERSISTENT_LOGINS.SERIES;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Field<String> field3() {
		return PersistentLogins.PERSISTENT_LOGINS.TOKEN;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Field<Timestamp> field4() {
		return PersistentLogins.PERSISTENT_LOGINS.LAST_USED;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String value1() {
		return getUsername();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String value2() {
		return getSeries();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String value3() {
		return getToken();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Timestamp value4() {
		return getLastUsed();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PersistentLoginsRecord value1(String value) {
		setUsername(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PersistentLoginsRecord value2(String value) {
		setSeries(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PersistentLoginsRecord value3(String value) {
		setToken(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PersistentLoginsRecord value4(Timestamp value) {
		setLastUsed(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PersistentLoginsRecord values(String value1, String value2, String value3, Timestamp value4) {
		value1(value1);
		value2(value2);
		value3(value3);
		value4(value4);
		return this;
	}

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * Create a detached PersistentLoginsRecord
	 */
	public PersistentLoginsRecord() {
		super(PersistentLogins.PERSISTENT_LOGINS);
	}

	/**
	 * Create a detached, initialised PersistentLoginsRecord
	 */
	public PersistentLoginsRecord(String username, String series, String token, Timestamp lastUsed) {
		super(PersistentLogins.PERSISTENT_LOGINS);

		setValue(0, username);
		setValue(1, series);
		setValue(2, token);
		setValue(3, lastUsed);
	}
}
