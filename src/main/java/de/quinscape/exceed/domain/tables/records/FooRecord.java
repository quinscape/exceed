/**
 * This class is generated by jOOQ
 */
package de.quinscape.exceed.domain.tables.records;


import de.quinscape.exceed.domain.tables.Foo;

import java.sql.Timestamp;

import javax.annotation.Generated;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record8;
import org.jooq.Row8;
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
public class FooRecord extends UpdatableRecordImpl<FooRecord> implements Record8<String, String, Integer, Integer, Timestamp, String, String, String> {

	private static final long serialVersionUID = -1515221209;

	/**
	 * Setter for <code>public.foo.id</code>.
	 */
	public void setId(String value) {
		setValue(0, value);
	}

	/**
	 * Getter for <code>public.foo.id</code>.
	 */
	@NotNull
	@Size(max = 36)
	public String getId() {
		return (String) getValue(0);
	}

	/**
	 * Setter for <code>public.foo.name</code>.
	 */
	public void setName(String value) {
		setValue(1, value);
	}

	/**
	 * Getter for <code>public.foo.name</code>.
	 */
	@NotNull
	@Size(max = 64)
	public String getName() {
		return (String) getValue(1);
	}

	/**
	 * Setter for <code>public.foo.num</code>.
	 */
	public void setNum(Integer value) {
		setValue(2, value);
	}

	/**
	 * Getter for <code>public.foo.num</code>.
	 */
	@NotNull
	public Integer getNum() {
		return (Integer) getValue(2);
	}

	/**
	 * Setter for <code>public.foo.type</code>.
	 */
	public void setType(Integer value) {
		setValue(3, value);
	}

	/**
	 * Getter for <code>public.foo.type</code>.
	 */
	@NotNull
	public Integer getType() {
		return (Integer) getValue(3);
	}

	/**
	 * Setter for <code>public.foo.created</code>.
	 */
	public void setCreated(Timestamp value) {
		setValue(4, value);
	}

	/**
	 * Getter for <code>public.foo.created</code>.
	 */
	@NotNull
	public Timestamp getCreated() {
		return (Timestamp) getValue(4);
	}

	/**
	 * Setter for <code>public.foo.description</code>.
	 */
	public void setDescription(String value) {
		setValue(5, value);
	}

	/**
	 * Getter for <code>public.foo.description</code>.
	 */
	public String getDescription() {
		return (String) getValue(5);
	}

	/**
	 * Setter for <code>public.foo.owner</code>.
	 */
	public void setOwner(String value) {
		setValue(6, value);
	}

	/**
	 * Getter for <code>public.foo.owner</code>.
	 */
	@Size(max = 36)
	public String getOwner() {
		return (String) getValue(6);
	}

	/**
	 * Setter for <code>public.foo.another</code>.
	 */
	public void setAnother(String value) {
		setValue(7, value);
	}

	/**
	 * Getter for <code>public.foo.another</code>.
	 */
	public String getAnother() {
		return (String) getValue(7);
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
	// Record8 type implementation
	// -------------------------------------------------------------------------

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Row8<String, String, Integer, Integer, Timestamp, String, String, String> fieldsRow() {
		return (Row8) super.fieldsRow();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Row8<String, String, Integer, Integer, Timestamp, String, String, String> valuesRow() {
		return (Row8) super.valuesRow();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Field<String> field1() {
		return Foo.FOO.ID;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Field<String> field2() {
		return Foo.FOO.NAME;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Field<Integer> field3() {
		return Foo.FOO.NUM;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Field<Integer> field4() {
		return Foo.FOO.TYPE;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Field<Timestamp> field5() {
		return Foo.FOO.CREATED;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Field<String> field6() {
		return Foo.FOO.DESCRIPTION;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Field<String> field7() {
		return Foo.FOO.OWNER;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Field<String> field8() {
		return Foo.FOO.ANOTHER;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String value1() {
		return getId();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String value2() {
		return getName();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Integer value3() {
		return getNum();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Integer value4() {
		return getType();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Timestamp value5() {
		return getCreated();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String value6() {
		return getDescription();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String value7() {
		return getOwner();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String value8() {
		return getAnother();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public FooRecord value1(String value) {
		setId(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public FooRecord value2(String value) {
		setName(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public FooRecord value3(Integer value) {
		setNum(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public FooRecord value4(Integer value) {
		setType(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public FooRecord value5(Timestamp value) {
		setCreated(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public FooRecord value6(String value) {
		setDescription(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public FooRecord value7(String value) {
		setOwner(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public FooRecord value8(String value) {
		setAnother(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public FooRecord values(String value1, String value2, Integer value3, Integer value4, Timestamp value5, String value6, String value7, String value8) {
		value1(value1);
		value2(value2);
		value3(value3);
		value4(value4);
		value5(value5);
		value6(value6);
		value7(value7);
		value8(value8);
		return this;
	}

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * Create a detached FooRecord
	 */
	public FooRecord() {
		super(Foo.FOO);
	}

	/**
	 * Create a detached, initialised FooRecord
	 */
	public FooRecord(String id, String name, Integer num, Integer type, Timestamp created, String description, String owner, String another) {
		super(Foo.FOO);

		setValue(0, id);
		setValue(1, name);
		setValue(2, num);
		setValue(3, type);
		setValue(4, created);
		setValue(5, description);
		setValue(6, owner);
		setValue(7, another);
	}
}
