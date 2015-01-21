package com.w1.merchant.android.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class SupportTickets {
	
	@SerializedName("Items")
	public List<SupportTicket> items;
}
