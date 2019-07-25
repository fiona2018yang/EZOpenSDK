package com.videogo.ui.util;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;

//make circle image
public class ImageUtil {
    public static Bitmap createCircleImage(Bitmap source, int min) {
        // init a paint
        final Paint paint = new Paint();
        Bitmap target = Bitmap.createBitmap(min, min, Bitmap.Config.ARGB_8888);
        /**
         * create a canvas same as big as up
         */
        Canvas canvas = new Canvas(target);
        /**
         * first paint circle
         */
        canvas.drawCircle(min/2,min/2,min/2,paint);
        /**
         * use SRC_IN
         */
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        /**
         * paint photo
         */
        canvas.drawBitmap(source,0,0,paint);
        return target;
    }
}
