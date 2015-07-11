package de.tlabs.thinkAir.benchmarkBundle1;

import java.lang.reflect.Method;

import de.tlabs.thinkAir.lib.ExecutionController;
import de.tlabs.thinkAir.lib.Remote;
import de.tlabs.thinkAir.lib.Remoteable;

class Toggle {

    boolean state = true;

    public Toggle(boolean start_state) {
        this.state = start_state;
    }

    public boolean value() {
        return (this.state);
    }

    public Toggle activate() {
        this.state = !this.state;
        return (this);
    }
}

class NthToggle extends Toggle {

    int count_max = 0;

    int counter = 0;

    public NthToggle(boolean start_state, int max_counter) {
        super(start_state);
        this.count_max = max_counter;
        this.counter = 0;
    }

    public Toggle activate() {
        this.counter += 1;
        if (this.counter >= this.count_max) {
            this.state = !this.state;
            this.counter = 0;
        }
        return (this);
    }
}

public class Methcall extends Remoteable implements Benchmark {

    /**
	 * 
	 */
    private static final long serialVersionUID = 2434406844221520861L;


    private transient ExecutionController controller;

	public Methcall(ExecutionController controller) {
		this.controller = controller;
	}
	
    @Remote
    public void localmain(int n) {
        boolean val = true;
        Toggle toggle = new Toggle(val);
        for (int i = 0; i < n; i++) {
            val = toggle.activate().value();
        }
        System.out.println((val) ? "true" : "false");
        val = true;
        NthToggle ntoggle = new NthToggle(true, 3);
        for (int i = 0; i < n; i++) {
            val = ntoggle.activate().value();
        }
        //return ((val) ? "true" : "false");
    }

    @Override
    public void copyState(Remoteable arg0) {
    }
public void main (int n) {
       Method toExecute;
       Class<?>[] paramTypes = {int.class};
       Object[] paramValues = { n};
       //String result = null;
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
