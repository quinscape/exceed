/**
 * This class is generated by jOOQ
 */
package de.quinscape.exceed.domain.tables;


import de.quinscape.exceed.domain.Keys;
import de.quinscape.exceed.domain.Public;
import de.quinscape.exceed.domain.tables.records.FooRecord;
import org.jooq.Field;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.UniqueKey;
import org.jooq.impl.TableImpl;

import javax.annotation.Generated;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;


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
public class Foo extends TableImpl<FooRecord> {

	private static final long serialVersionUID = -244441630;

	/**
	 * The reference instance of <code>public.foo</code>
	 */
	public static final Foo FOO = new Foo();

	/**
	 * The class holding records for this type
	 */
	@Override
	public Class<FooRecord> getRecordType() {
		return FooRecord.class;
	}

	/**
	 * The column <code>public.foo.id</code>.
	 */
	public final TableField<FooRecord, String> ID = createField("id", org.jooq.impl.SQLDataType.VARCHAR.length(36).nullable(false), this, "");

	/**
	 * The column <code>public.foo.name</code>.
	 */
	public final TableField<FooRecord, String> NAME = createField("name", org.jooq.impl.SQLDataType.VARCHAR.length(64).nullable(false), this, "");

	/**
	 * The column <code>public.foo.num</code>.
	 */
	public final TableField<FooRecord, Integer> NUM = createField("num", org.jooq.impl.SQLDataType.INTEGER.nullable(false), this, "");

	/**
	 * The column <code>public.foo.type</code>.
	 */
	public final TableField<FooRecord, Integer> TYPE = createField("type", org.jooq.impl.SQLDataType.INTEGER.nullable(false), this, "");

	/**
	 * The column <code>public.foo.created</code>.
	 */
	public final TableField<FooRecord, Timestamp> CREATED = createField("created", org.jooq.impl.SQLDataType.TIMESTAMP.nullable(false).defaulted(true), this, "");

	/**
	 * The column <code>public.foo.description</code>.
	 */
	public final TableField<FooRecord, String> DESCRIPTION = createField("description", org.jooq.impl.SQLDataType.CLOB, this, "");

	/**
	 * Create a <code>public.foo</code> table reference
	 */
	public Foo() {
		this("foo", null);
	}

	/**
	 * Create an aliased <code>public.foo</code> table reference
	 */
	public Foo(String alias) {
		this(alias, FOO);
	}

	private Foo(String alias, Table<FooRecord> aliased) {
		this(alias, aliased, null);
	}

	private Foo(String alias, Table<FooRecord> aliased, Field<?>[] parameters) {
		super(alias, Public.PUBLIC, aliased, parameters, "");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public UniqueKey<FooRecord> getPrimaryKey() {
		return Keys.FOO_PKEY;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<UniqueKey<FooRecord>> getKeys() {
		return Arrays.<UniqueKey<FooRecord>>asList(Keys.FOO_PKEY);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Foo as(String alias) {
		return new Foo(alias, this);
	}

	/**
	 * Rename this table
	 */
	public Foo rename(String name) {
		return new Foo(name, null);
	}
}
