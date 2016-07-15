package com.example.fertilizercrm.common.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Created by tong on 15/12/10.
 */
public class ImageCropHelper {
    private static final int PICK_FROM_CAMERA = 100;
    private static final int CROP_FROM_CAMERA = 200;
    private static final int PICK_FROM_FILE = 300;

    private Activity activity;
    private Uri imgUri;
    private ImageView imageView;
    private File lastImageFile;
    private boolean cropImg = true;
    private OnCropListener mOnCropListener;

    public ImageCropHelper(Activity activity) {
        this(activity, null);
    }

    public ImageCropHelper(Activity activity, ImageView imageView) {
        this.activity = activity;
        this.imageView = imageView;
    }

    public ImageCropHelper(Fragment fragment) {
        this(fragment, null);
    }

    public ImageCropHelper(Fragment fragment, ImageView imageView) {
        this(fragment.getActivity(), imageView);
    }

    /**
     * 显示选择框(拍照、相册)
     */
    public void start() {
        new AlertDialog.Builder(activity).setTitle("选择头像")
                .setPositiveButton("相册", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        openAlbum();
                    }
                }).setNegativeButton("拍照", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                takePicture();
            }
        }).create().show();
    }

    /**
     * 拍照
     */
    public void takePicture() {
        Intent intent = new Intent(
                MediaStore.ACTION_IMAGE_CAPTURE);
        imgUri = Uri.fromFile(new File(Environment
                .getExternalStorageDirectory(), "avatar_"
                + String.valueOf(System.currentTimeMillis())
                + ".png"));
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imgUri);
        activity.startActivityForResult(intent, PICK_FROM_CAMERA);
        System.out.println();
    }

    /**
     * 打开相册
     */
    public void openAlbum() {
        // 方式1，直接打开图库，只能选择图库的图片
        //Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        // 方式2，会先让用户选择接收到该请求的APP，可以从文件系统直接选取图片
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_PICK);
        activity.startActivityForResult(intent, PICK_FROM_FILE);
    }

    /**
     * 在activity中调用这个方法
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        switch (requestCode) {
            case PICK_FROM_CAMERA:
                if (isCropImg()) {
                    doCrop();
                } else {
//                    try {
//                        File outFile =  new File(new URI(imgUri.toString()));
//                        Bitmap bitmap = BitmapFactory.decodeStream(new FileInputStream(outFile));
//                        saveBitmap(outFile, bitmap);
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }

                    File imgFile = getFileFromURI(imgUri);
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = false;
                    options.inSampleSize = 10;   //width，hight设为原来的十分一
                    Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath(),options);
                    saveBitmap(new File(activity.getCacheDir(), "/crop" + System.currentTimeMillis() + ".png"), bitmap);
                }
                break;
            case PICK_FROM_FILE:
                imgUri = data.getData();

                if (isCropImg()) {
                    doCrop();
                } else {
                    File imgFile = getFileFromURI(imgUri);
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = false;
                    options.inSampleSize = 10;   //width，hight设为原来的十分一
                    Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath(),options);
                    saveBitmap(new File(activity.getCacheDir(), "/crop" + System.currentTimeMillis() + ".png"), bitmap);
                }
                break;
            case CROP_FROM_CAMERA:
                if (null != data) {
                    setCropImg(data);
                }
                break;
        }
    }

    private File getFileFromURI(Uri contentUri) {
        String filePath = getPath(activity,contentUri);
//        if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT){
//            String wholeID = DocumentsContract.getDocumentId(contentUri);
//            String id = wholeID.split(":")[1];
//            String[] column = { MediaStore.Images.Media.DATA };
//            String sel = MediaStore.Images.Media._ID + "=?";
//            Cursor cursor = activity.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, column,
//                    sel, new String[] { id }, null);
//            int columnIndex = cursor.getColumnIndex(column[0]);
//            if (cursor.moveToFirst()) {
//                filePath = cursor.getString(columnIndex);
//            }
//            cursor.close();
//        }else{
//            String[] projection = { MediaStore.Images.Media.DATA };
//            Cursor cursor = activity.getContentResolver().query(contentUri, projection, null, null, null);
//            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
//            cursor.moveToFirst();
//            filePath = cursor.getString(column_index);
//        }
        return new File(filePath);
    }

    /**
     * Get a file path from a Uri. This will get the the path for Storage Access
     * Framework Documents, as well as the _data field for the MediaStore and
     * other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri The Uri to query.
     * @author paulburke
     */
    public static String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[] {
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri The Uri to query.
     * @param selection (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * 剪切图片
     */
    protected void doCrop() {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setType("image/*");
        List<ResolveInfo> list = activity.getPackageManager().queryIntentActivities(intent, 0);
        int size = list.size();
        if (size == 0) {
            Toast.makeText(activity, "can't find crop app", Toast.LENGTH_SHORT).show();
            return;
        } else {
            intent.setData(imgUri);
            fillCropExtras(intent);

            //使用第一个剪切器
            Intent i = new Intent(intent);
            ResolveInfo res = list.get(0);
            i.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            activity.startActivityForResult(i, CROP_FROM_CAMERA);
        }
    }

    /**
     * 填充剪切参数
     * @param intent
     */
    protected void fillCropExtras(Intent intent) {
        intent.putExtra("outputX", 300);
        intent.putExtra("outputY", 300);
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("scale", true);
        intent.putExtra("return-data", true);
    }

    /**
     * set the bitmap
     *
     * @param picdata
     */
    protected void setCropImg(Intent picdata) {
        Bundle bundle = picdata.getExtras();
        if (bundle != null) {
            Bitmap bitmap = bundle.getParcelable("data");
            if (imageView != null) {
                imageView.setImageBitmap(bitmap);
            }
            saveBitmap(new File(activity.getCacheDir(), "/crop" + System.currentTimeMillis() + ".png"), bitmap);
        }
    }

    /**
     * save the crop bitmap
     *
     * @param outFile
     * @param bitmap
     */
    public void saveBitmap(File outFile, Bitmap bitmap) {
        this.lastImageFile = outFile;
        FileOutputStream fOut = null;
        try {
            outFile.createNewFile();
            fOut = new FileOutputStream(outFile);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
            fOut.flush();

            if (imageView != null) {
                imageView.setImageBitmap(bitmap);
            }
            if (mOnCropListener != null) {
                mOnCropListener.onGetImage(outFile,bitmap);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fOut.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 获取最后一次选择的图片
     * @return
     */
    public File getImageFile() {
        return lastImageFile;
    }

    /**
     * 是否剪切图片
     * @return
     */
    public boolean isCropImg() {
        return cropImg;
    }

    public void setCropImg(boolean cropImg) {
        this.cropImg = cropImg;
    }

    public void setOnCropListener(OnCropListener listener) {
        this.mOnCropListener = listener;
    }

    public interface OnCropListener {
        void onGetImage(File imgFile,Bitmap bitmap);
    }
}
