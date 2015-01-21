package com.w1.merchant.android.utils;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;

import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;

import retrofit.converter.ConversionException;
import retrofit.converter.Converter;
import retrofit.mime.MimeUtil;
import retrofit.mime.TypedByteArray;
import retrofit.mime.TypedInput;
import retrofit.mime.TypedOutput;

public class W1GsonConverter implements Converter {

    private static final String charset = "UTF-8";
    private final Gson gson;

    public W1GsonConverter(Gson gson) {
        this.gson = gson;
    }

    @Override
    public Object fromBody(TypedInput body, Type type) throws ConversionException {
        String charset = W1GsonConverter.charset;
        if (body.mimeType() != null) {
            charset = MimeUtil.parseCharset(body.mimeType(), charset);
        }
        InputStreamReader isr = null;
        try {
            isr = new InputStreamReader(body.in(), charset);
            return gson.fromJson(isr, type);
        } catch (IOException e) {
            throw new ConversionException(e);
        } catch (JsonParseException e) {
            throw new ConversionException(e);
        } finally {
            if (isr != null) {
                try {
                    isr.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    @Override
    public TypedOutput toBody(Object object) {
        return new TypedByteArray("application/vnd.wallet.openapi.v1+json", gson.toJson(object).getBytes());
    }
}
