package de.tlabs.thinkAir.faceDetection;

import de.tlabs.thinkAir.client.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.View;


public class FaceView extends View
{
	
	private Bitmap sourceImage;
	private Paint tmpPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

	public FaceView(Context context) {
		super(context);
	
		BitmapFactory.Options bfo = new BitmapFactory.Options();
		bfo.inPreferredConfig = Bitmap.Config.RGB_565;
		
		tmpPaint.setStyle(Paint.Style.STROKE);
		tmpPaint.setTextAlign(Paint.Align.CENTER);

		sourceImage = BitmapFactory.decodeResource( getResources() , R.drawable.prove2, bfo);

		
	}
	
	
	
	@Override
	protected void onDraw(Canvas canvas)
	{
		canvas.drawBitmap( sourceImage, null , new Rect(0,0,getWidth(),getHeight()),tmpPaint);
	}
	
	
	
	
	
	
	
	
}