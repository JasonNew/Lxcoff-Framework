package de.tlabs.thinkAir.benchmarkBundle1;

import java.lang.reflect.Method;

import de.tlabs.thinkAir.lib.ExecutionController;
import de.tlabs.thinkAir.lib.Remote;
import de.tlabs.thinkAir.lib.Remoteable;

public class Objinst extends Remoteable implements Benchmark {

    /**
	 * 
	 */
    private static final long serialVersionUID = 3853356519860757191L;

    private transient ExecutionController controller;

	public Objinst(ExecutionController controller) {
		this.controller = controller;
	}
    @Remote
    public void localmain(int n) {
        String result = "";
        Toggle toggle1 = new Toggle(true);
        for (int i = 0; i < 5; i++) {
            result = result + ((toggle1.activate().value()) ? "true" : "false") + "\n";
        }
        for (int i = 0; i < n; i++) {
            Toggle toggle = new Toggle(true);
        }
        result += "\n";
        NthToggle ntoggle1 = new NthToggle(true, 3);
        for (int i = 0; i < 8; i++) {
            result += ((ntoggle1.activate().value()) ? "true" : "false") + "\n";
        }
        for (int i = 0; i < n; i++) {
            NthToggle toggle = new NthToggle(true, 3);
        }
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
