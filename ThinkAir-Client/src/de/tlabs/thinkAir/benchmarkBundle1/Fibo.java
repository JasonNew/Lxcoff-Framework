// $Id: fibo.java,v 1.2 2000/12/24 19:10:50 doug Exp $

package de.tlabs.thinkAir.benchmarkBundle1;

import java.lang.reflect.Method;

import de.tlabs.thinkAir.lib.ExecutionController;
import de.tlabs.thinkAir.lib.Remote;
import de.tlabs.thinkAir.lib.Remoteable;

public class Fibo extends Remoteable implements Benchmark {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6006353707339696217L;

	private transient ExecutionController controller;

	public Fibo(ExecutionController controller) {
		this.controller = controller;
	}

	public void main(int n) {
		firstStepFib(n);
	}

	@Remote
	public int localfirstStepFib(int n) {
		if (n < 2)
			return (1);
		return (fib(n - 2) + fib(n - 1));
	}

	public int fib(int n) {
		if (n < 2)
			return (1);
		return (fib(n - 2) + fib(n - 1));
	}

	@Remote
	public int localremoteFib(int n) {
		if (n < 2)
			return (1);
		return (remoteFib(n - 2) + remoteFib(n - 1));
	}

	@Override
	public void copyState(Remoteable arg0) {
	}

	public int firstStepFib(int n) {
		Method toExecute;
		Class<?>[] paramTypes = { int.class };
		Object[] paramValues = { n };
		Integer result = null;
		try {
			toExecute = this.getClass().getDeclaredMethod("localfirstStepFib",
					paramTypes);
			result = (Integer) controller.execute(toExecute, paramValues, this);
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

	public int remoteFib(int n) {
		Method toExecute;
		Class<?>[] paramTypes = { int.class };
		Object[] paramValues = { n };
		Integer result = null;
		try {
			toExecute = this.getClass().getDeclaredMethod("localremoteFib",
					paramTypes);
			result = (Integer) controller.execute(toExecute, paramValues, this);
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
