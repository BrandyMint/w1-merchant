package com.w1.merchant.android.rest.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

// TODO Переименовать во что-нибудь более вменяемое
public class AuthModel implements Parcelable {

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


	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(this.clientId);
		dest.writeLong(createDate != null ? createDate.getTime() : -1);
		dest.writeLong(expireDate != null ? expireDate.getTime() : -1);
		dest.writeLong(this.timeout);
		dest.writeString(this.scope);
		dest.writeString(this.userId);
		dest.writeString(this.token);
	}

	public AuthModel() {
	}

	protected AuthModel(Parcel in) {
		this.clientId = in.readString();
		long tmpCreateDate = in.readLong();
		this.createDate = tmpCreateDate == -1 ? null : new Date(tmpCreateDate);
		long tmpExpireDate = in.readLong();
		this.expireDate = tmpExpireDate == -1 ? null : new Date(tmpExpireDate);
		this.timeout = in.readLong();
		this.scope = in.readString();
		this.userId = in.readString();
		this.token = in.readString();
	}

	public static final Parcelable.Creator<AuthModel> CREATOR = new Parcelable.Creator<AuthModel>() {
		public AuthModel createFromParcel(Parcel source) {
			return new AuthModel(source);
		}

		public AuthModel[] newArray(int size) {
			return new AuthModel[size];
		}
	};
}
