package com.example.avatarimageview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by pengxinkai001 on 2016/11/8.
 *
 * 自定义圆形头像类  该类可直接在XML中使用
 *
 */
public class AvatarImageView extends ImageView {

    private Paint paint = new Paint();


    public AvatarImageView(Context context) {
        super(context);
    }

    public AvatarImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AvatarImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @Override
    public void draw(Canvas canvas) {

        Drawable drawable = getDrawable();
        if (drawable != null) {
            Bitmap rawbitmap = ((BitmapDrawable) drawable).getBitmap();

          //将bitmap转换成正方形
            Bitmap newBitmap = dealRawBitmap(rawbitmap);
          //将newBitmap 转换成圆形
            Bitmap circleBitmap = toRoundCorner(newBitmap,14);


            final Rect rect = new Rect(0,0,circleBitmap.getWidth(),circleBitmap.getHeight());
            paint.reset();

            //绘制在画布上

            canvas.drawBitmap(circleBitmap,rect,rect,paint);
        }else {

            super.draw(canvas);
        }


    }

    //将头像按比例缩放
    private Bitmap scaleBitmap(Bitmap bitmap) {

        int widght = getWidth();
        //一定要强转成float 不然有可能因为精度不够 出现 scale为0 的错误
        float scale = (float) widght / (float) bitmap.getWidth();

        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);

        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }


    //将原始头像裁剪成正方形
    private Bitmap dealRawBitmap(Bitmap bitmap) {

        int widght = bitmap.getWidth();
        int height = bitmap.getHeight();

        //获取宽度
        int minwidght = widght > height ? height : widght;
        //计算正方形的范围
        int leftTopx = (widght - minwidght) / 2;
        int leftTopy = (height - minwidght) / 2;

        //裁剪成正方形

        Bitmap newbitmap = Bitmap.createBitmap(bitmap, leftTopx, leftTopy, minwidght, minwidght, null, false);


        return scaleBitmap(newbitmap);
    }


    private Bitmap toRoundCorner(Bitmap bitmap, int piexls) {
        //指定为 ARGB_4444 可以减小图片大小

        Bitmap outputbitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(outputbitmap);


        final int color = 0xff424242;
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        paint.setAntiAlias(true);

        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);

        int x = bitmap.getWidth();
        canvas.drawCircle(x / 2, x / 2, x / 2, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return outputbitmap;
    }


}
