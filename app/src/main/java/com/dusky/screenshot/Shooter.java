package com.dusky.screenshot;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.nio.ByteBuffer;
import java.util.Objects;


public class Shooter {

    private final SoftReference<Context> mRefContext;
    private ImageReader mImageReader;

    private MediaProjection mMediaProjection;
    private VirtualDisplay mVirtualDisplay;

    private String mLocalUrl = "";

    private OnFinishedListener mOnShotListener;
    private int mHeight;
    private int mWidth;
    private int Y;//裁剪区域起点Y
    private int Yv;//裁剪区域高



    public Shooter(Context context, int reqCode, Intent data) {
        this.mRefContext = new SoftReference<>(context);

        mMediaProjection = getMediaProjectionManager().getMediaProjection(reqCode, data);

        WindowManager window = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        assert window != null;
        Display mDisplay = window.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        mDisplay.getRealMetrics(metrics);
        mWidth = metrics.widthPixels;//size.x;
        mHeight = metrics.heightPixels;//size.y;

        mImageReader = ImageReader.newInstance(
                mWidth,
                mHeight,
                PixelFormat.RGBA_8888,//此处必须和下面 buffer处理一致的格式 ，RGB_565在一些机器上出现兼容问题。
                1);
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void virtualDisplay() {

        mVirtualDisplay = mMediaProjection.createVirtualDisplay("screen-mirror",
                mWidth,
                mHeight,
                Resources.getSystem().getDisplayMetrics().densityDpi,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                mImageReader.getSurface(), null, null);

    }

    public void startScreenShot(OnFinishedListener onShotListener, String loc_url,int y,int yv) {
        this.Y=y;
        this.Yv=yv;
        mLocalUrl = loc_url;
        startScreenShot(onShotListener);
    }


    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void startScreenShot(OnFinishedListener onShotListener) {

        mOnShotListener = onShotListener;

        virtualDisplay();
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    Image image = mImageReader.acquireLatestImage();
                                    new SaveTask().doInBackground(image);
                                }
                            },
                1000);

    }


    @SuppressLint("StaticFieldLeak")
    public class SaveTask extends AsyncTask<Image, Void, Bitmap> {

        @TargetApi(Build.VERSION_CODES.KITKAT)
        @Override
        protected Bitmap doInBackground(Image... params) {
            if (params == null || params.length < 1 || params[0] == null) {
                return null;
            }

            Image image = params[0];
            int width = image.getWidth();
            int height = image.getHeight();
            int h2=Yv-Y;
            if(h2>height-Y||h2<=0){
                if (mOnShotListener != null) {
                    Log.d("Shooter path:", mLocalUrl + "");
                    mOnShotListener.onError();
                    image.close();
                    return null;
                }
            }

            final Image.Plane[] planes = image.getPlanes();
            final ByteBuffer buffer = planes[0].getBuffer();
            //每个像素的间距
            int pixelStride = planes[0].getPixelStride();
            //总的间距
            int rowStride = planes[0].getRowStride();
            int rowPadding = rowStride - pixelStride * width;
            Bitmap bitmap = Bitmap.createBitmap(width + rowPadding / pixelStride, height,
                    Bitmap.Config.ARGB_8888);//虽然这个色彩比较费内存但是 兼容性更好
            bitmap.copyPixelsFromBuffer(buffer);

            bitmap = Bitmap.createBitmap(bitmap, 0, Y, width, h2);
            image.close();
            File fileImage = null;
            if (bitmap != null) {
                try {
                    fileImage = new File(mLocalUrl);

                    if (!fileImage.exists()) {
                        fileImage.createNewFile();
                    }
                    FileOutputStream out = new FileOutputStream(fileImage);
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                    out.flush();
                    out.close();

                } catch (IOException e) {
                    e.printStackTrace();
                    fileImage = null;
                }
            }

            if (bitmap != null && !bitmap.isRecycled()) {
                bitmap.recycle();
            }

            if (mVirtualDisplay != null) {
                mVirtualDisplay.release();
            }
            if (mMediaProjection != null) {
                mMediaProjection.stop();
            }

            if (mOnShotListener != null) {
                Log.d("Shooter path:", mLocalUrl + "");
                mOnShotListener.onFinish();
            } else {
                Log.d("Shooter:", "noShotListener");
            }

            if (fileImage != null) {
                return bitmap;
            }
            return null;
        }

        @TargetApi(Build.VERSION_CODES.KITKAT)
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            Log.d("Shooter:", "check onPostExecute");
            super.onPostExecute(bitmap);
        }
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private MediaProjectionManager getMediaProjectionManager() {

        return (MediaProjectionManager) getContext().getSystemService(
                Context.MEDIA_PROJECTION_SERVICE);
    }

    private Context getContext() {
        return mRefContext.get();
    }

    public interface OnFinishedListener {
        void onFinish();
        void onError();
    }


}
