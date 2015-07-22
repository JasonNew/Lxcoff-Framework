package rs.pedjaapps.Linpack;

import java.lang.reflect.Method;

import org.jason.lxcoff.lib.ExecutionController;
import org.jason.lxcoff.lib.Remoteable;

public class Linpack extends Remoteable{
	transient private static String TAG = "Linpack";
	transient private ExecutionController controller;

	public Linpack(){
		
	}
	
	public Linpack(ExecutionController controller){
		this.controller = controller;
	}
	
	public Result doLinpack() {
		Method toExecute;
		Class<?>[] paramTypes = {};
		Object[] paramValues = {};

		Result result = null;
		long starttime = System.nanoTime();
		try {
			toExecute = this.getClass().getDeclaredMethod("localRunLinpack", paramTypes);
			result = (Result) controller.execute(toExecute, paramValues, this);
		} catch (SecurityException e) {
			// Should never get here
			e.printStackTrace();
			throw e;
		} catch (NoSuchMethodException e) {
			// Should never get here
			e.printStackTrace();
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return result;
	}
	
	public Result localRunLinpack(){
		Result result = runLinpack(Result.class);
		return result;
	}
	
    public native Result runLinpack(Class mClass);
	
	
	@Override
	public void copyState(Remoteable state) {

	}
}
