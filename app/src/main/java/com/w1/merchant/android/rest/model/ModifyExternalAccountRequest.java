package com.w1.merchant.android.rest.model;

/**
 * Created by alexey on 13.06.15.
 */
public class ModifyExternalAccountRequest {

    public String title;

    public String color;

    public boolean isDefault;

    public ModifyExternalAccountRequest(String title, int color, boolean isDefault) {
        this.title = title;
        this.color = String.format("#%06X", 0xFFFFFF & color);
        this.isDefault = isDefault;
    }

}
