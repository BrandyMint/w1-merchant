package com.w1.merchant.android.ui;

public interface IProgressbarProvider {
    Object startProgress();
    void stopProgress(Object token);
}
