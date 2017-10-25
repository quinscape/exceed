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
public class AppUser extends GeneratedDomainObject implements Serializable {

	private static final long serialVersionUID = 1696334754;

	private String    id;
	private String    login;
	private String    password;
	private Timestamp created;
	private Timestamp lastLogin;
	private String    roles;

	public AppUser() {}

	public AppUser(AppUser value) {
		this.id = value.id;
		this.login = value.login;
		this.password = value.password;
		this.created = value.created;
		this.lastLogin = value.lastLogin;
		this.roles = value.roles;
	}

	public AppUser(
		String    id,
		String    login,
		String    password,
		Timestamp created,
		Timestamp lastLogin,
		String    roles
	) {
		this.id = id;
		this.login = login;
		this.password = password;
		this.created = created;
		this.lastLogin = lastLogin;
		this.roles = roles;
	}

	@NotNull
	@Size(max = 36)
	public String getId() {
		return this.id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@NotNull
	@Size(max = 64)
	public String getLogin() {
		return this.login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	@NotNull
	@Size(max = 255)
	public String getPassword() {
		return this.password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	@NotNull
	public Timestamp getCreated() {
		return this.created;
	}

	public void setCreated(Timestamp created) {
		this.created = created;
	}

	@NotNull
	public Timestamp getLastLogin() {
		return this.lastLogin;
	}

	public void setLastLogin(Timestamp lastLogin) {
		this.lastLogin = lastLogin;
	}

	@NotNull
	@Size(max = 255)
	public String getRoles() {
		return this.roles;
	}

	public void setRoles(String roles) {
		this.roles = roles;
	}
}
