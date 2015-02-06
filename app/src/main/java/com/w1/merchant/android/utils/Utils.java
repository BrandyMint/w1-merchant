package com.w1.merchant.android.utils;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.util.Log;

import com.w1.merchant.android.R;
import com.w1.merchant.android.Session;
import com.w1.merchant.android.activity.LoginActivity;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public final class Utils {

    private Utils() {}

    public static int compare(long lhs, long rhs) {
        return lhs < rhs ? -1 : (lhs == rhs ? 0 : 1);
    }

    public static String hex(byte[] array) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < array.length; ++i) {
            sb.append(Integer.toHexString((array[i]
                    & 0xFF) | 0x100).substring(1,3));
        }
        return sb.toString();
    }

    public static String md5Hex (String message) {
        try {
            MessageDigest md =
                    MessageDigest.getInstance("MD5");
            return hex (md.digest(message.getBytes("CP1252")));
        } catch (NoSuchAlgorithmException e) {
        } catch (UnsupportedEncodingException e) {
        }
        return null;
    }

    public static Intent createPickPhotoActivityIntent() {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        return photoPickerIntent;
    }

    public static Intent createMakePhotoIntent(Context context) throws MakePhotoException {
        Intent takePictureIntent;
        Uri currentPhotoUri;

        takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(context.getPackageManager()) == null) {
            throw new MakePhotoException(R.string.error_camera_not_available);
        }

        currentPhotoUri = createPictureOutputPath(context);
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, currentPhotoUri);
        return takePictureIntent;
    }


    public static Uri createPictureOutputPath(Context context) throws MakePhotoException {
        Date currentDate;
        File storageDir;
        File image;
        String imageFileName;

        storageDir = Utils.getPicturesDirectory(context);
        if (storageDir == null) {
            throw new MakePhotoException(R.string.error_no_place_to_save);
        }
        currentDate = new Date();
        imageFileName = Utils.getOutputMediaFileName(currentDate,"IMG_");
        image = new File(storageDir, imageFileName + ".jpg");
        return Uri.fromFile(image);
    }

    public static boolean isInPicturesDirectory(Context context, Uri uri) {
        File picturesDir = getPicturesDirectory(context);
        if (picturesDir == null) return false;
        return uri.getPath().startsWith(picturesDir.getAbsolutePath());
    }

    @Nullable
    public static File getPicturesDirectory(Context context) {
        File mediaStorageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (mediaStorageDir == null) return null;

        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("ImageUtils", "failed to create directory");
                return null;
            }
        }
        return mediaStorageDir;
    }

    public static String getOutputMediaFileName(Date timestamp, String prefix) {
        String ts = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(timestamp);
        return prefix + ts;
    }

    public static class MakePhotoException extends Exception {
        public int errorResourceId;

        public MakePhotoException(int resourceId) {
            super();
            errorResourceId = resourceId;
        }

        public MakePhotoException(int resourceId, Throwable e) {
            super(e);
            errorResourceId = resourceId;
        }

    }

    public static void restartApp(@Nullable Context context) {
        if (context != null) {
            Intent mStartActivity = new Intent(context.getApplicationContext(), LoginActivity.class);
            mStartActivity.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP|Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
            int mPendingIntentId = 123456;
            PendingIntent mPendingIntent = PendingIntent.getActivity(context.getApplicationContext(), mPendingIntentId, mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
            AlarmManager mgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
            if (context instanceof Activity) ((Activity) context).finish();
        }

        Session.getInstance().clear();
        System.exit(0);
    }

}
