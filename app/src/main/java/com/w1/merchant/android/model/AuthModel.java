package com.w1.merchant.android.model;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

// TODO Переименовать во что-нибудь более вменяемое
public class AuthModel {

    public static final String SCOPE_ALL = "All";

    /**
     * Идентификатор приложения OpenApi
     */
    @SerializedName("ClientId")
    public String clientId;

	/**
	 * Дата создания токена
	 */
	@SerializedName("CreateDate")
	public Date createDate;
	
	/**
	 * Дата/время истечения действия токена
	 */
	@SerializedName("ExpireDate")
    public Date expireDate;
	
	/**
	 * Время, на которое продлевается действие токена в секундах
	 */
	@SerializedName("Timeout")
    public long timeout = -1;
	
	/**
	 * Операции доступные по данному токену
	 */
	@SerializedName("Scope")
    public String scope = "All";
	
	/**
	 *  Номер кошелька
	 */
	@SerializedName("UserId")
    public String userId;
	
	/**
	 * Токен сессии
	 */
	@SerializedName("Token")
    public String token;

}
