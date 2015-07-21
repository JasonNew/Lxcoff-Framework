package de.tlabs.thinkAir.queens;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;


public class FaceView
{
	
	private Bitmap sourceImage;
	private Paint tmpPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

	public FaceView() {
	
		BitmapFactory.Options bfo = new BitmapFactory.Options();
		bfo.inPreferredConfig = Bitmap.Config.RGB_565;
		
		tmpPaint.setStyle(Paint.Style.STROKE);
		tmpPaint.setTextAlign(Paint.Align.CENTER);
		
	}
	
	public int getsomething(){
		
		return tmpPaint.getAlpha();
	}
	
}