package de.tlabs.thinkAir.benchmarkBundle1;

import java.lang.reflect.Method;

import de.tlabs.thinkAir.lib.ExecutionController;
import de.tlabs.thinkAir.lib.Remote;
import de.tlabs.thinkAir.lib.Remoteable;

public class Nestedloop extends Remoteable implements Benchmark {

    /**
	 * 
	 */
    private static final long serialVersionUID = -2093250151921469345L;

    private transient ExecutionController controller;

	public Nestedloop(ExecutionController controller) {
		this.controller = controller;
	}
	
    @Remote
    public void localmain(int n) {
        int x = 0;
        for (int a = 0; a < n; a++) for (int b = 0; b < n; b++) for (int c = 0; c < n; c++) for (int d = 0; d < n; d++) for (int e = 0; e < n; e++) for (int f = 0; f < n; f++) x++;
        
    }

    @Override
    public void copyState(Remoteable arg0) {
    }
public  void main (int n) {
       Method toExecute;
       Class<?>[] paramTypes = {int.class};
       Object[] paramValues = { n};
       try {
           toExecute = this.getClass().getDeclaredMethod("localmain", paramTypes);
           controller.execute(toExecute, paramValues, this);
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
   }
   
}
