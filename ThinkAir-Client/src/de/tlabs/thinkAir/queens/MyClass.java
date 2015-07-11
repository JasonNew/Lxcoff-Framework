package de.tlabs.thinkAir.queens;

public class MyClass {
	public String className = "MyClass";
	
	public FaceView faceview = null;
	public int alpha = 414;
	
	protected String getClassName(){
		return className;
	}
	
	public MyClass(){
		this.faceview = new FaceView();
		this.alpha = faceview.getsomething();
	}

}
