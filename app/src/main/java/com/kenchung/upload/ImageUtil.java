package com.kenchung.upload;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;

/**
 * 图片选择/拍照
 *
 * @Author KenChung
 */
public class ImageUtil {

    private static final String TAG = "ImageUtil";

    public static Intent choosePicture() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        return Intent.createChooser(intent, null);
    }

    /**
     * 拍照后返回
     */
    public static Intent takeBigPicture(Context context) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, newPictureUri(context, getNewPhotoPath()));
        return intent;
    }

    public static String getDirPath() {
        return Environment.getExternalStorageDirectory().getPath() + "/WebViewUploadImage";
    }

    private static String getNewPhotoPath() {
        return getDirPath() + "/" + System.currentTimeMillis() + ".jpg";
    }

    public static String retrievePath(Context context, Intent sourceIntent, Intent dataIntent) {
        String picPath = null;
        try {
            Uri uri;
            if (dataIntent != null) {
                uri = dataIntent.getData();
                if (uri != null) {
                    picPath = ContentUtil.getPath(context, uri);
                }
                if (isFileExists(picPath)) {
                    return picPath;
                }

                Log.w(TAG, String.format("retrievePath failed from dataIntent:%s, extras:%s", dataIntent, dataIntent.getExtras()));
            }

            if (sourceIntent != null) {
                uri = sourceIntent.getParcelableExtra(MediaStore.EXTRA_OUTPUT);
                if (uri != null) {
                    String scheme = uri.getScheme();
                    if (scheme != null) {
                        if (scheme.startsWith("file")) {
                            picPath = uri.getPath();
                        } else if (scheme.startsWith("content")) {
                            picPath = getRealPathFromURI(context, uri);
                        }
                    }
                }
                if (!TextUtils.isEmpty(picPath)) {
                    File file = new File(picPath);
                    if (!file.exists() || !file.isFile()) {
                        Log.w(TAG, String.format("retrievePath file not found from sourceIntent path:%s", picPath));
                    }
                }
            }
            return picPath;
        } finally {
            Log.d(TAG, "retrievePath(" + sourceIntent + "," + dataIntent + ") ret: " + picPath);
        }
    }

    private static String getRealPathFromURI(Context context, Uri contentUri) {
        String res = "";
        if (contentUri != null) {
            Cursor cursor = null;
            try {
                cursor = context.getContentResolver().query(contentUri, null, null, null, null);
                if (cursor != null) {
                    cursor.moveToFirst();
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex > -1) {
                        res = getDirPath() + "/" + cursor.getString(nameIndex);
                    }
                }
            } catch (Exception e) {
            }
            if (cursor != null) {
                cursor.close();
            }
        }
        return res;
    }

    private static Uri newPictureUri(Context context, String path) {
        return FileProvider.getUriForFile(context, "com.kenchung.upload.fileprovider", new File(path));
    }

    private static boolean isFileExists(String path) {
        if (TextUtils.isEmpty(path)) {
            return false;
        }
        File f = new File(path);
        if (!f.exists()) {
            return false;
        }
        return true;
    }
}
