package de.tlabs.thinkAir.benchmarkBundle1;

import java.lang.reflect.Method;

import org.jason.lxcoff.lib.ExecutionController;
import org.jason.lxcoff.lib.Remote;
import org.jason.lxcoff.lib.Remoteable;

public class SieveData extends Remoteable implements Benchmark {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7176684717080835962L;

	private transient ExecutionController controller;

	public SieveData(ExecutionController controller) {
		this.controller = controller;
	}

	public void main(int n) {
		boolean[] flags = new boolean[8192 + 1];
		sieve(n, flags);
	}
	
	@Remote
	public Integer localsieve(int n, boolean[] flags){
		int NUM = n;
		int count = 0;
		while (NUM-- > 0) {
			count = 0;
			for (int i = 2; i <= 8192; i++) {
				flags[i] = true;
			}
			for (int i = 2; i <= 8192; i++) {
				if (flags[i]) {
					for (int k = i + i; k <= 8192; k += i) {
						flags[k] = false;
					}
					count++;
				}
			}
		}
		return count;
	}

	@Override
	public void copyState(Remoteable arg0) {
	}

	public Integer sieve(int n, boolean[] flags) {
		Method toExecute;
		Class<?>[] paramTypes = { int.class, boolean[].class };
		Object[] paramValues = { n, flags };
		Integer result = null;
		try {
			toExecute = this.getClass().getDeclaredMethod("localsieve",
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
