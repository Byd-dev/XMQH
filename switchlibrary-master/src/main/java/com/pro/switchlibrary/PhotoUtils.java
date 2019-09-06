package com.pro.switchlibrary;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * Created by bvin on 2015/10/22.
 */
public class PhotoUtils {

    public static final String FILE_NAME_SUFFIX_ORIGIN = "_ORIGIN";
    public static final String FILE_NAME_SUFFIX_COMPRESS = "_COMPRESS";
    public static final int DEFAULT_WIDTH = 1200;
    public static final int DEFAULT_HEIGHT = 756;


    private static final int width = 198;
    private static final int height = 196;

    /**
     * 启动拍照，只返回缩略图
     * @param activity
     * @param requestCode
     */
    public static void launchCameraThumbnail(Activity activity, int requestCode){
        dispatchTakePictureIntent(activity,requestCode,null);
    }

    /**
     * 启动拍照，自动设定原始照片路径
     * @param activity
     * @param requestCode
     * @param callback 通过回掉返回自动生成的照片路径
     */
    public static void launchCameraOriginPhoto(Activity activity, int requestCode, LaunchCameraCallback callback){
        try {
            File image = createImageFile(activity);
            if (image!=null){
                callback.onMediaCapturePathReady(image.getAbsolutePath());
                dispatchTakePictureIntent(activity, requestCode, image);
            }
        } catch (IOException e) {
            e.printStackTrace();
            // Error occurred while creating the File
        }
    }

    /**
     * 启动拍照，指定原始照片路径
     * @param activity
     * @param requestCode
     * @param path 指定照片路径
     */
    public static void launchCameraOriginPhoto(Activity activity, int requestCode, String path){
        File image = new File(path);
        if (image!=null){
            dispatchTakePictureIntent(activity, requestCode, image);
        }
    }

    public static void launchPictureLibrary(Activity activity, int requestCode){
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        activity.startActivityForResult(intent,requestCode);
    }

    private static void dispatchTakePictureIntent(Activity activity, int requestCode, File photoFile) {
        if (activity.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (intent.resolveActivity(activity.getPackageManager()) != null) {
                //it's safe to use the intent.no app can handle, your app will crash.
                if (photoFile != null)
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                activity.startActivityForResult(intent, requestCode);
            }
        } else {
            //没有照相功能
        }
    }


    public interface LaunchCameraCallback {
        void onMediaCapturePathReady(String mediaCapturePath);
    }


    /**
     * 用来指定拍照路径
     * @param context 用来获取程序目录
     * @return
     * @throws IOException
     */
    private static File createImageFile(Context context) throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String imageFileName = timeStamp + FILE_NAME_SUFFIX_ORIGIN;//201510221454_origin
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);//私有目录
        /*File image = File.createTempFile(
                imageFileName,  *//* prefix *//*
                ".jpg",         *//* suffix *//*
                storageDir      *//* directory *//*
        );*/
        File image = new File(storageDir,imageFileName+".jpg");
        image.createNewFile();
        return image;
    }

    private static Bitmap decodeImage(String path, int desiredWidth, int desiredHeight){
        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        //占用大小 = w * h * 单位像素占用的字节数；
        //ALPHA_8：只有alpha值，没有RGB值，一个像素占用一个字节
        //ARGB_4444：一个像素占用2个字节，alpha(A)值，Red（R）值，Green(G)值，Blue（B）值各占4个bites,共16位,即2
        //ARGB_8888：一个像素占用4个字节，alpha(A)值，Red（R）值，Green(G)值，Blue（B）值各占8个bites,共32bites,即4
        //RGB_565：一个像素占用2个字节，没有alpha(A)值，即不支持透明和半透明，Red（R）值占5个bites ，
        // Green(G)值占6个bites  ，Blue（B）值占5个bites,共16bites,即2个字节
        bmOptions.inPreferredConfig = Bitmap.Config.RGB_565;
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.max(photoW/desiredWidth, photoH/desiredHeight);
        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(path, bmOptions);
        return  bitmap;
    }

    /**
     * 处理缩略图，onActivityResult
     * @param data cannot be null
     * @return 如果不是横屏拍摄的旋转成横屏的bitmap，否则返回原始的图片
     */
    public static Bitmap getThumbnailPhoto(Intent data) {
        Bundle extras = data.getExtras();
        String key = "data";
        if (extras!=null&&extras.containsKey(key)){
            Bitmap image = (Bitmap) extras.get(key);
            if (image!=null){
                return rotateToLandscapeIfNecessary(image);
            }
        }
        return null;
    }

    /**
     * 获取原始照片用来显示的图片
     * @param path 原始图片文件路径
     * @param desiredWidth 希望设定的宽度
     * @param desiredHeight 希望设定的高度
     * @return 返回缩小版并且旋至横屏的图像
     */
    public static Bitmap getOriginPhotoToDisplay(String path, int desiredWidth, int desiredHeight){
        Bitmap scaledImage = decodeImage(path, desiredWidth, desiredHeight);
        if (scaledImage!=null){
            return rotateToLandscapeIfNecessary(scaledImage);
        }else
            return scaledImage;
    }

    /**
     * 获取原始照片用来上传的图片，已压缩
     * @param path 原始路径
     * @return 输出压缩后的图片路径
     */
    public static String getOriginPhotoToUpload(Context context, String path){
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        //获取源文件文件名，加上后缀，放在程序私有外部目录目录
        File compressFile = genCompressFile(path, storageDir);
        return compressImage(path,compressFile.getAbsolutePath());
    }

    public static Bitmap getOriginPhotoToDisplay(Context context, Uri data, int desiredWidth, int desiredHeight){
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(),data);
            if (bitmap==null) return null;
            if (bitmap.getWidth()>DEFAULT_WIDTH||bitmap.getHeight()>DEFAULT_HEIGHT){//直接查大小需要api>=16
                String path = AppUtil.getFilePathFromContentUri(data, context);
                Bitmap scaledImage = decodeImage(path, desiredWidth, desiredHeight);
                if (scaledImage!=null){
                    return rotateToLandscapeIfNecessary(scaledImage);
                }else
                    return scaledImage;
            }else{
                return bitmap;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getOriginPhotoToUpload(Context context, Uri data){
        String path = AppUtil.getFilePathFromContentUri(data, context);
        //应该先查询图片大小再做压缩，不大就直接上传
        if (TextUtils.isEmpty(path)) return null;//空就返回null
        return getOriginPhotoToUpload(context,path);
    }

    /**
     * 压缩图片
     * @param inputPath 输入文件路径，必须是原始照片路径
     * @return 输出压缩文件路径，与原文件同一目录
     */
    private static String compressImage(String inputPath, String outputPath){
        Bitmap scaledImage = decodeImage(inputPath, DEFAULT_WIDTH, DEFAULT_HEIGHT);
        try {
            Bitmap.CompressFormat format;
            if (Build.VERSION.SDK_INT>=14){
                format = Bitmap.CompressFormat.WEBP;
            }else {
                format = Bitmap.CompressFormat.JPEG;
            }
            OutputStream stream = new FileOutputStream(new File(outputPath));
            boolean suc = scaledImage.compress(Bitmap.CompressFormat.JPEG,80,stream);
            if (suc)
                return outputPath;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 生成压缩图片文件
     * @param inputPath 输入文件路径，为了获取原文件名，加后缀_COMPRESS
     * @param outputDir 输出目录
     * @return 压缩文件
     */
    private static File genCompressFile(String inputPath, File outputDir){
        String fileNameSuffix = ".jpg";
        /*if (Build.VERSION.SDK_INT>=14){
            fileNameSuffix = ".webp";
        }else {
            fileNameSuffix = ".jpg";
        }*/
        File originFile = new File(inputPath);
        String originFileName =  originFile.getName();
        String originFileNameNoSuffix = originFileName.substring(0, originFileName.lastIndexOf("."));
        String compressFileName ;
        if (originFileNameNoSuffix.endsWith(FILE_NAME_SUFFIX_ORIGIN)){//_origin后缀就替换成_compress后缀
            compressFileName = originFileNameNoSuffix.replace(FILE_NAME_SUFFIX_ORIGIN,FILE_NAME_SUFFIX_COMPRESS);
        }else{//没有_origin后缀就直接追加_compress后缀
            compressFileName = originFileNameNoSuffix + FILE_NAME_SUFFIX_COMPRESS;
        }
        try {
            File image = new File(outputDir,compressFileName+fileNameSuffix);
            image.createNewFile();
            return  image;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 旋转成横屏，如宽度小于高度
     * @param image
     * @return
     */
    private static Bitmap rotateToLandscapeIfNecessary(Bitmap image){
        if (image.getWidth()<image.getHeight()){
            return rotateImage(image,90);
        }else
            return image;
    }

    /**
     * 旋转并缩小
     * @param imagePath
     * @return
     */
    private static Bitmap rotateToLandscapeAndScale(String imagePath, int w, int h){
        int orientation = getCorrectionOrientation(getExifOrientation(imagePath));
        //根据原始照片路径创建缩小的图片
        Bitmap image = decodeImage(imagePath, w, h);
        if (orientation!=0){
            return image;
        }else{
            //修正方向
            return rotateImage(image,orientation);
        }
    }

    private static Bitmap rotateImage(Bitmap image, int  degrees){
        Matrix matrix = new Matrix();
        matrix.setRotate(degrees, image.getWidth() / 2, image.getHeight() / 2);
        Bitmap rotatedImage = Bitmap.createBitmap(image, 0, 0, image.getWidth(), image.getHeight(), matrix, true);
        image.recycle();//释放之前竖版的bitmap
        return rotatedImage;
    }

    /**
     * 获取纠正后的角度
     * @param orientation
     * @return
     */
    private static int getCorrectionOrientation(int  orientation){
        int rotationAngle = 0;
        if (orientation == ExifInterface.ORIENTATION_ROTATE_90) rotationAngle = 90;
        if (orientation == ExifInterface.ORIENTATION_ROTATE_180) rotationAngle = 180;
        if (orientation == ExifInterface.ORIENTATION_ROTATE_270) rotationAngle = 270;
        return rotationAngle;
    }

    private static int getExifOrientation(String imagePath){
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(imagePath);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
            return orientation;
        } catch (IOException e) {
            e.printStackTrace();
            return ExifInterface.ORIENTATION_UNDEFINED;
        }

    }

    /**
     * 清除所有图片
     * @param context
     */
    public static void clearAll(Context context){
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        for (File file: storageDir.listFiles()){
            if (file.isFile())
                file.delete();
        }
    }

    /**
     * 清除所有原始图片
     * @param context
     */
    public static void clearOriginImage(Context context){
        clearImage(context,FILE_NAME_SUFFIX_ORIGIN);
    }

    /**
     * 清除所有压缩图片
     * @param context
     */
    public static void clearCompressImage(Context context){
        clearImage(context,FILE_NAME_SUFFIX_COMPRESS);
    }

    private static void clearImage(Context context, String suffix){
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        for (File file: storageDir.listFiles()){
            if (file.isFile()){
                String fileName = file.getName();
                String originFileNameNoSuffix = fileName.substring(0, fileName.lastIndexOf("."));
                if (originFileNameNoSuffix.endsWith(suffix)){
                    file.delete();
                }
            }
        }
    }

    /**
     * 2016.2.24新加的类似
     */

    private static Bitmap compressImage(String filePath, Bitmap image) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);// 质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        int options = 100;
        while (baos.toByteArray().length / 1024 > 350) { // 循环判断如果压缩后图片是否大于100kb,大于继续压缩
            baos.reset();// 重置baos即清空baos
            image.compress(Bitmap.CompressFormat.JPEG, options, baos);// 这里压缩options%，把压缩后的数据存放到baos中
            options -= 5;// 每次都减少5
        }


        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());// 把压缩后的数据baos存放到ByteArrayInputStream中
        try {
            if(!filePath.equals("")){
                FileOutputStream f = new FileOutputStream(new File(filePath));
                f.write(baos.toByteArray());
                f.flush();
                f.close();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);// 把ByteArrayInputStream数据生成图片
        return bitmap;
    }

    /***
     * 图片按比例大小压缩方法（根据路径获取图片并压缩）
     *
     * @param srcPath
     * @return
     */
    public static Bitmap getImage(String srcPath) {
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        // 开始读入图片，此时把options.inJustDecodeBounds 设回true了
        newOpts.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeFile(srcPath, newOpts);// 此时返回bm为空

        newOpts.inJustDecodeBounds = false;
        int w = newOpts.outWidth;
        int h = newOpts.outHeight;
        // 现在主流手机比较多是800*480分辨率，所以高和宽我们设置为
        float hh = 1000f;// 这里设置高度为800f
        float ww = 750f;// 这里设置宽度为600f
        // 缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
        int be = 1;// be=1表示不缩放
        if (w > h && w > ww) {// 如果宽度大的话根据宽度固定大小缩放
            be = (int) (newOpts.outWidth / ww);
        } else if (w < h && h > hh) {// 如果高度高的话根据宽度固定大小缩放
            be = (int) (newOpts.outHeight / hh);
        }
        if (be <= 0)
            be = 1;
        newOpts.inSampleSize = be;// 设置缩放比例
        // 重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
        bitmap = BitmapFactory.decodeFile(srcPath, newOpts);
        return compressImage(srcPath,bitmap);// 压缩好比例大小后再进行质量压缩
    }

    public static String getImagePath(String srcPath, int i) {
        File file = null;
        try {
            file = createImageFile(i);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if(file.exists()){
            return file.getAbsolutePath();
        }else{
            try {
                file.createNewFile();
//				copySdcardFile(srcPath,file.getAbsolutePath());
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        String filePath = file.getAbsolutePath();
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        // 开始读入图片，此时把options.inJustDecodeBounds 设回true了
        newOpts.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeFile(srcPath, newOpts);// 此时返回bm为空

        newOpts.inJustDecodeBounds = false;
        int w = newOpts.outWidth;
        int h = newOpts.outHeight;
        // 现在主流手机比较多是800*480分辨率，所以高和宽我们设置为
        float hh = 1000f;// 这里设置高度为800f
        float ww = 750f;// 这里设置宽度为600f
        // 缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
        int be = 1;// be=1表示不缩放
        if (w > h && w > ww) {// 如果宽度大的话根据宽度固定大小缩放
            be = (int) (newOpts.outWidth / ww);
        } else if (w < h && h > hh) {// 如果高度高的话根据宽度固定大小缩放
            be = (int) (newOpts.outHeight / hh);
        }
        if (be <= 0)
            be = 1;
        newOpts.inSampleSize = be;// 设置缩放比例
        // 重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
        bitmap = BitmapFactory.decodeFile(srcPath, newOpts);
        compressImage(filePath,bitmap);// 压缩好比例大小后再进行质量压缩

        return filePath;
    }

    /**
     * 把bitmap转换成String
     *
     * @param filePath
     * @return
     */
    public static String bitmapToString(String filePath) {

        Bitmap bm = getSmallBitmap(filePath);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 40, baos);
        byte[] b = baos.toByteArray();

        return Base64.encodeToString(b, Base64.DEFAULT);

    }

    /**
     * 根据路径获得突破并压缩返回bitmap用于显示
     *
     * @param filePath
     * @return
     */
    public static Bitmap getSmallBitmap(String filePath) {
//		 final BitmapFactory.Options options = new BitmapFactory.Options();
//		 options.inJustDecodeBounds = true;
//		 BitmapFactory.decodeFile(filePath, options);
//
//		 // Calculate inSampleSize
//		 options.inSampleSize = calculateInSampleSize(options, width, height);
//
//		 // Decode bitmap with inSampleSize set
//		 options.inJustDecodeBounds = false;
//
//		 return BitmapFactory.decodeFile(filePath, options);

        return ThumbnailUtils.extractThumbnail(
                BitmapFactory.decodeFile(filePath), width, height);
    }

    /**
     * 计算图片的缩放值
     *
     * @param options
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    public static int calculateInSampleSize(BitmapFactory.Options options,
                                            int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            // Calculate ratios of height and width to requested height and
            // width
            final int heightRatio = Math.round((float) height
                    / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);

            // Choose the smallest ratio as inSampleSize value, this will
            // guarantee
            // a final image with both dimensions larger than or equal to the
            // requested height and width.
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }

        return inSampleSize;
    }

    /**
     * 根据路径获得突破并压缩返回bitmap用于显示
     *
     * @param filePath
     * @return
     */
    public static Bitmap getSmallBitmapByCompress(String filePath) {
        Bitmap bitmap = BitmapFactory.decodeFile(filePath);
        bitmap = compressImage("",bitmap);
        return bitmap;
    }

    /**
     * 获取保存图片的目录
     *
     * @return
     */
    public static File getAlbumDir() {
        File dir = new File(Environment.getExternalStorageDirectory(),
                getAlbumName());
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    /**
     * 获取保存 隐患检查的图片文件夹名称
     *
     * @return
     */
    public static String getAlbumName() {
        return "temporary";
    }

    /**
     * 把程序拍摄的照片放到 SD卡的 Pictures目录中
     *
     * @return
     * @throws IOException
     */
    @SuppressLint("SimpleDateFormat")
    public static File createImageFile() throws IOException {

        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String timeStamp = format.format(new Date());
        String imageFileName =AppConfig.NAME_OF_USERHEADPIC;

        File image = new File(getAlbumDir(), imageFileName);
        return image;
    }

    public static File createImageFileRandom(String name) throws IOException {
        File image = new File(getAlbumDir(), "/" + name + System.currentTimeMillis() + ".jpg");
        return image;
    }

    public static File createImageFileVideo(String name) throws IOException {
        File image = new File(getAlbumDir(), "/" + name + System.currentTimeMillis() + ".mp4");
        return image;
    }
    /**
     * 把从相册内选择的照片放到 SD卡中的 .missge 文件夹中
     *
     * @return
     * @throws IOException
     */
    @SuppressLint("SimpleDateFormat")
    public static File createImageFile(int id) throws IOException {

//		SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd_HHmmss");
//		String timeStamp = format.format(new Date());
        String imageFileName = AppConfig.NAME_OF_USERHEADPIC;

        File image = new File(getAlbumDir(), imageFileName);
        return image;
    }

    public static File saveBitmap(byte[] data) {
        File file = null;
        try {
            file = createImageFile();
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(data);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return file;
    }

    private static int copySdcardFile(String fromFile, String toFile) {

        try {
            InputStream fosfrom = new FileInputStream(fromFile);
            OutputStream fosto = new FileOutputStream(toFile);
            byte bt[] = new byte[1024];
            int c;
            while ((c = fosfrom.read(bt)) > 0) {
                fosto.write(bt, 0, c);
                fosto.flush();
            }
            fosfrom.close();
            fosto.close();
            return 0;

        } catch (Exception ex) {
            return -1;
        }
    }

    public static File saveBitmap(String path) {
        // Bitmap bitmap = BitmapFactory.decodeFile(path); // 此时返回bm为空
        // ByteArrayOutputStream baos = new ByteArrayOutputStream();
        // bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);// 把数据写入文件

        // File f = null;
        // try {
        // f = createImageFile();
        // try {
        // f.createNewFile();
        // FileOutputStream fOut = null;
        // fOut = new FileOutputStream(f);
        // bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
        // fOut.flush();
        // fOut.close();
        // } catch (IOException e1) {
        // // TODO Auto-generated catch block
        // f = null;
        // e1.printStackTrace();
        // }
        // } catch (IOException e) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // }
        File f = null;
        try {
            f = createImageFile();
            if (!f.exists()) {
                f.createNewFile();
            }
            copySdcardFile(path, f.getAbsolutePath());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return f;
    }

    /**
     * 添加到图库
     */
    public static void galleryAddPic(Context context, String path) {
        Intent mediaScanIntent = new Intent(
                Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(path);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        context.sendBroadcast(mediaScanIntent);
    }

    /**
     * 根据路径删除图片
     *
     * @param path
     */
    public static void deleteTempFile(String path) {
        File file = new File(path);
        if (file.exists()) {
            file.delete();
        }
    }

    public static void delete(File file) {
        if (file.isFile()) {
            file.delete();
            return;
        }

        if(file.isDirectory()){
            File[] childFiles = file.listFiles();
            if (childFiles == null || childFiles.length == 0) {
                file.delete();
                return;
            }

            for (int i = 0; i < childFiles.length; i++) {
                delete(childFiles[i]);
            }
            file.delete();
        }
    }

    public static void deleteAlbumDir(){
        File dir = new File(Environment.getExternalStorageDirectory(),
                getAlbumName());
        if (dir.exists()) {
            dir.delete();
        }
    }

    public static void saveBitmap(Bitmap photoBitmap, String path) {
        FileOutputStream fileOutputStream = null;
        File photoFile = new File(path);
        try {
            fileOutputStream = new FileOutputStream(photoFile);
            if (photoBitmap != null) {
                if (photoBitmap.compress(Bitmap.CompressFormat.PNG, 100,
                        fileOutputStream)) {
                    fileOutputStream.flush();
                }
            }
        } catch (FileNotFoundException e) {
            photoFile.delete();
            e.printStackTrace();
        } catch (IOException e) {
            photoFile.delete();
            e.printStackTrace();
        } finally {
            try {
                if(!photoBitmap.isRecycled()){
                    photoBitmap.recycle();
                }
                fileOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


}
}
