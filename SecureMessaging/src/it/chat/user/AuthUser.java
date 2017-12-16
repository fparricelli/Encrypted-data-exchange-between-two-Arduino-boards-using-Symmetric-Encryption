package it.chat.user;

public class AuthUser {
	
	private String token;
	private String name;
	private String surname;
	private String role;
	private int roleNumber;
	private int telephone;
	
	public AuthUser(String t, String n, String s, String r, int tel,int rn) {
		this.token = t;
		this.name = n;
		this.surname = s;
		this.role = r;
		this.telephone = tel;
		this.roleNumber = rn;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSurname() {
		return surname;
	}

	public void setSurname(String surname) {
		this.surname = surname;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public int getTelephone() {
		return telephone;
	}

	public void setTelephone(int telephone) {
		this.telephone = telephone;
	}

	public int getRoleNumber() {
		return roleNumber;
	}

	public void setRoleNumber(int roleNumber) {
		this.roleNumber = roleNumber;
	}
	
	

}
