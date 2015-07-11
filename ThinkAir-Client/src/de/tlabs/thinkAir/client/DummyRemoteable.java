package de.tlabs.thinkAir.client;

import java.lang.reflect.Method;
import java.util.Random;

import de.tlabs.thinkAir.lib.ExecutionController;
import de.tlabs.thinkAir.lib.Remoteable;

public class DummyRemoteable extends Remoteable {
    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;

    public transient ExecutionController controller;

    public DummyRemoteable(ExecutionController controller) {
        this.controller = controller;
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
        String result = "";
        try {
            toExecute = this.getClass().getDeclaredMethod("localCpuLoader2", (Class[]) null);
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

    public Long cpuLoader3(int seed) {
        Method toExecute;
        Class<?>[] paramTypes = { int.class };
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

    public String localCpuLoader1() {
        for (int i = 0; i < 10000; i++) {
        }
        return "cpuLoader1 finished";
    }

    public String localCpuLoader2() {
        for (int i = 0; i < 500000; i++) {
        }
        return "cpuLoader2 finished";
    }

    public Long localCpuLoader3(int seed) {
        Random rand = new Random(seed);
        return rand.nextLong();
    }

    @Override
    public void copyState(Remoteable state) {
        // No fields to restore
    }
    // private void readObject(java.io.ObjectInputStream stream) throws
    // IOException,
    // ClassNotFoundException {
    // stream.defaultReadObject();
    // controller = new ExecutionController();
    // }
}
