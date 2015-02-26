package com.w1.merchant.android.activity;

public interface IProgressbarProvider {
    public Object startProgress();
    public void stopProgress(Object token);
}
