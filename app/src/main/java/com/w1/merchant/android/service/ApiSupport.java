package com.w1.merchant.android.service;

import com.w1.merchant.android.model.SupportTicket;
import com.w1.merchant.android.model.SupportTicketPost;
import com.w1.merchant.android.model.SupportTickets;
import com.w1.merchant.android.model.UploadFileResponse;

import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.Multipart;
import retrofit.http.POST;
import retrofit.http.Part;
import retrofit.http.Path;
import retrofit.mime.TypedOutput;

public interface ApiSupport {

    @GET("/support/tickets/{id}")
    public void getTicket(@Path("id") long ticketId, Callback<SupportTicket> cb);

    @GET("/support/tickets/")
    public void getTickets(Callback<SupportTickets> cb);

    @POST("/support/tickets")
    public void createTicket(@Body SupportTicket.CreateRequest request, Callback<SupportTicket> cb);

    @POST("/support/tickets/{id}/posts")
    public void postReply(@Path("id") long ticketId, @Body SupportTicket.ReplyRequest body, Callback<SupportTicketPost> cb);

    @Multipart
    @POST("/file")
    public void uploadFile(@Part("file") TypedOutput file, Callback<UploadFileResponse> cb);
}
