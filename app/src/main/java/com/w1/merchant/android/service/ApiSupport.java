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
import rx.Observable;

public interface ApiSupport {

    @GET("/support/tickets/{id}")
    public Observable<SupportTicket> getTicket(@Path("id") long ticketId);

    @GET("/support/tickets/")
    public Observable<SupportTickets> getTickets();

    @POST("/support/tickets")
    public Observable<SupportTicket> createTicket(@Body SupportTicket.CreateRequest request);

    @POST("/support/tickets/{id}/posts")
    public Observable<SupportTicketPost> postReply(@Path("id") long ticketId, @Body SupportTicket.ReplyRequest body);

    @Multipart
    @POST("/file")
    public Observable<UploadFileResponse> uploadFile(@Part("file") TypedOutput file);
}
