package de.tlabs.thinkAir.synthBenchmark;

import java.lang.reflect.Method;
import java.util.Random;

import android.util.Log;
import de.tlabs.thinkAir.client.DummyRemoteable;
import org.jason.lxcoff.lib.ExecutionController;
import org.jason.lxcoff.lib.Remote;
import org.jason.lxcoff.lib.Remoteable;

/**
 * A completely synthetic 'benchmark' with some very basic CPU loading methods -
 * empty loops, basic computations, very little memory operations.
 * 
 * @author Andrius
 * 
 */
public class CalcIntensive extends Remoteable {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private transient ExecutionController controller;

    public int temp = 0;

    public CalcIntensive(ExecutionController controller) {
        this.controller = controller;
    }

    public void scenario1() {
        cpuLoader1();
        cpuLoader2();
        cpuLoader3(cpuLoader3(cpuLoader3(System.nanoTime())));
        cpuLoader3(System.nanoTime());
        cpuLoader2();
    }

    public void scenario2() {
        Method toExecute;
        try {
            toExecute = this.getClass().getDeclaredMethod("localscenario2", (Class[]) null);
            controller.execute(toExecute, this);
            Log.d("PowerDroid-Client", "Value of temp - " + temp);
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

    public void scenario2Helper() {
        Method toExecute;
        try {
            toExecute = this.getClass().getDeclaredMethod("localscenario2Helper", (Class[]) null);
            controller.execute(toExecute, this);
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

    public String cpuLoader1() {
        Method toExecute;
        String result = "";
        try {
            toExecute = this.getClass().getDeclaredMethod("localCpuLoader1", (Class[]) null);
            result = (String) controller.execute(toExecute, this);
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

    public String cpuLoader2() {
        Method toExecute;
        Class<?>[] paramTypes = null;
        Object[] paramValues = null;
        String result = "";
        try {
            toExecute = this.getClass().getDeclaredMethod("localCpuLoader2", paramTypes);
            result = (String) controller.execute(toExecute, paramValues, this);
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

    public Long cpuLoader3(long seed) {
        Method toExecute;
        Class<?>[] paramTypes = { long.class };
        Object[] paramValues = { seed };
        Long result = null;
        try {
            toExecute = this.getClass().getDeclaredMethod("localCpuLoader3", paramTypes);
            result = (Long) controller.execute(toExecute, paramValues, this);
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

    @Remote
    public void localscenario2() {
        temp++;
        cpuLoader1();
        scenario2Helper();
        cpuLoader2();
        DummyRemoteable dummy = new DummyRemoteable(controller);
        dummy.cpuLoader1();
    }

    @SuppressWarnings("unused")
	@Remote
    private void localscenario2Helper() {
        temp++;
        cpuLoader3(System.nanoTime());
        cpuLoader1();
    }

    public String localCpuLoader1() {
        int t = 0;
        for (int i = 0; i < 10000; i++) {
            t = t + i * i;
        }
        return "cpuLoader1 finished";
    }

    public String localCpuLoader2() {
        for (int i = 0; i < 500000; i++) {
        }
        return "cpuLoader2 finished";
    }

    @Remote
    public Long localCpuLoader3(long seed) {
        Random rand = new Random(seed);
        return rand.nextLong();
    }
    

    @Override
    public void copyState(Remoteable state) {
        CalcIntensive localState = (CalcIntensive) state;
        this.temp = localState.temp;
    }

    /**
     * Override the readObject method to construct server-side
     * ExecutionController upon deserialization
     * 
     * @param stream
     * @throws IOException
     * @throws ClassNotFoundException
     */
    // private void readObject(java.io.ObjectInputStream stream) throws
    // IOException,
    // ClassNotFoundException {
    // stream.defaultReadObject();
    // controller = new ExecutionController();
    // }
}
