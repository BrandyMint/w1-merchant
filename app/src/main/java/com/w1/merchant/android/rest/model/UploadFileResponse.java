package com.w1.merchant.android.rest.model;

import android.text.TextUtils;

public class UploadFileResponse {

    public String link;

    public String fileId;

    public String getLinkAImg() {
        if (TextUtils.isEmpty(link)) return "";
        String link = TextUtils.htmlEncode(this.link);
        return "<a target=\"_blank\" href=\"" + link
                +  "\"><img src=\"" + link + "\"></a>";
    }
}
