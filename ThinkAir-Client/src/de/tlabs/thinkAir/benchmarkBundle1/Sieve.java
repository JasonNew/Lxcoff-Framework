package de.tlabs.thinkAir.benchmarkBundle1;

import java.lang.reflect.Method;

import de.tlabs.thinkAir.lib.ExecutionController;
import de.tlabs.thinkAir.lib.Remote;
import de.tlabs.thinkAir.lib.Remoteable;

public class Sieve extends Remoteable implements Benchmark {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7176684717080835962L;

	private transient ExecutionController controller;

	public Sieve(ExecutionController controller) {
		this.controller = controller;
	}

	@Remote
	public void localmain(int n) {
		int NUM = n;
		boolean[] flags = new boolean[8192 + 1];
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
	}

	@Override
	public void copyState(Remoteable arg0) {
	}

	public void main(int n) {
		Method toExecute;
		Class<?>[] paramTypes = { int.class };
		Object[] paramValues = { n };
		
		try {
			toExecute = this.getClass().getDeclaredMethod("localmain",
					paramTypes);
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
