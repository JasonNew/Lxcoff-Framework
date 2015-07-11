
package de.tlabs.thinkAir.benchmarkBundle1;

import java.lang.reflect.Method;

import de.tlabs.thinkAir.lib.ExecutionController;
import de.tlabs.thinkAir.lib.Remote;
import de.tlabs.thinkAir.lib.Remoteable;

public class Strcat extends Remoteable implements Benchmark {

    /**
	 * 
	 */
    private static final long serialVersionUID = 4181065109307153236L;

    private transient ExecutionController controller;

	public Strcat(ExecutionController controller) {
		this.controller = controller;
	}
    @Remote
    public void localmain(int n) {
        StringBuffer str = new StringBuffer();
        for (int i = 0; i < n; i++) {
            str.append("hello\n");
        }
        //return str.length();
    }

    @Override
    public void copyState(Remoteable arg0) {
    }
    
    public void main (int n) {
       Method toExecute;
       Class<?>[] paramTypes = {int.class};
       Object[] paramValues = { n};
       //Integer result = null;
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
