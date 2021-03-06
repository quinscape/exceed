/**
 * This class is generated by jOOQ
 */
package de.quinscape.exceed.domain.tables.pojos;


import de.quinscape.exceed.runtime.domain.GeneratedDomainObject;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.annotation.Generated;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;


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
public class PersistentLogins extends GeneratedDomainObject implements Serializable {

	private static final long serialVersionUID = -776475970;

	private String    username;
	private String    series;
	private String    token;
	private Timestamp lastUsed;

	public PersistentLogins() {}

	public PersistentLogins(PersistentLogins value) {
		this.username = value.username;
		this.series = value.series;
		this.token = value.token;
		this.lastUsed = value.lastUsed;
	}

	public PersistentLogins(
		String    username,
		String    series,
		String    token,
		Timestamp lastUsed
	) {
		this.username = username;
		this.series = series;
		this.token = token;
		this.lastUsed = lastUsed;
	}

	@NotNull
	@Size(max = 64)
	public String getUsername() {
		return this.username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	@NotNull
	@Size(max = 64)
	public String getSeries() {
		return this.series;
	}

	public void setSeries(String series) {
		this.series = series;
	}

	@NotNull
	@Size(max = 64)
	public String getToken() {
		return this.token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	@NotNull
	public Timestamp getLastUsed() {
		return this.lastUsed;
	}

	public void setLastUsed(Timestamp lastUsed) {
		this.lastUsed = lastUsed;
	}
}
