package com.w1.merchant.android.model;

import com.google.gson.annotations.SerializedName;

import java.util.Collections;
import java.util.List;

// TODO Переименовать во что-нибудь более вменяемое
public class AuthCreateModel  {
	
	@SerializedName("Login")
	private String login;
	
	@SerializedName("Password")
	private String password;
	
	@SerializedName("Scope")
	private String scope;
	
	@SerializedName("Params")
	private List<Param> params = Collections.emptyList();

	public AuthCreateModel(String login, String password, String scope) {
		super();
		this.login = login;
		this.password = password;
		this.scope = scope;
	}

    public AuthCreateModel(String login, String password) {
        this(login, password, AuthModel.SCOPE_ALL);
    }
	
	private static class Param {
		@SerializedName("Name")
		private String name;
		
		@SerializedName("Value")
		private String value;
		
		public Param(String name, String value) {
			this.name = name;
			this.value = value;
		}
	}

}
