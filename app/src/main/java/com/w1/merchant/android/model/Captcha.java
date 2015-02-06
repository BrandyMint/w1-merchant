package com.w1.merchant.android.model;

public class Captcha {

    public long captchaId;

    public String captchaUrl;


    public static final class CaptchaRequest {

        public int width;

        public int height;

        public CaptchaRequest() {
        }

        public CaptchaRequest(int width, int height) {
            this.width = width;
            this.height = height;
        }

    }
}