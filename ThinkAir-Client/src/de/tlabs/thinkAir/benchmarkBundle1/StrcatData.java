/*
 * Strcat benchmark with different decision point - expensive result returning
 */

package de.tlabs.thinkAir.benchmarkBundle1;

import java.lang.reflect.Method;

import de.tlabs.thinkAir.lib.ExecutionController;
import de.tlabs.thinkAir.lib.Remote;
import de.tlabs.thinkAir.lib.Remoteable;

public class StrcatData extends Remoteable implements Benchmark {

    /**
	 * 
	 */
    private static final long serialVersionUID = 4181065109307153236L;

    private transient ExecutionController controller;

	public StrcatData(ExecutionController controller) {
		this.controller = controller;
	}
    
    public void main(int n) {
    	strcat(n);
    }
    
    @Remote
    private StringBuffer localstrcat(int n){
        StringBuffer str = new StringBuffer();
        for (int i = 0; i < n; i++) {
            str.append("hello\n");
        }
        return str;
    }

    @Override
    public void copyState(Remoteable arg0) {
    }
    
    public StringBuffer strcat (int n) {
       Method toExecute;
       Class<?>[] paramTypes = {int.class};
       Object[] paramValues = { n};
       StringBuffer result = null;
       try {
           toExecute = this.getClass().getDeclaredMethod("localstrcat", paramTypes);
           result = (StringBuffer) controller.execute(toExecute, paramValues, this);
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
   
}

