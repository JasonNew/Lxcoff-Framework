// $Id: hash.java,v 1.3 2001/03/02 02:17:29 doug Exp $

package de.tlabs.thinkAir.benchmarkBundle1;

import java.lang.reflect.Method;
import java.util.HashMap;

import de.tlabs.thinkAir.lib.ExecutionController;
import de.tlabs.thinkAir.lib.Remote;
import de.tlabs.thinkAir.lib.Remoteable;

public class Hash extends Remoteable implements Benchmark {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4381889120619226140L;
	private transient ExecutionController controller;

	public Hash(ExecutionController controller) {
		this.controller = controller;
	}
	
	public void main(int n){
		hash(n);
	}

	@Remote
	public int localhash(int n) {
		int i, c;
		HashMap<String, Integer> ht = new HashMap<String, Integer>();
		c = 0;
		for (i = 1; i <= n; i++)
			ht.put(Integer.toString(i, 16), new Integer(i));
		for (i = 1; i <= n; i++)
			if (ht.containsKey(Integer.toString(i, 10)))
				c++;
		return c;
	}

	@Override
	public void copyState(Remoteable arg0) {
	}

	public int hash(int n) {
		Method toExecute;
		Class<?>[] paramTypes = { int.class };
		Object[] paramValues = { n };
		Integer result = null;
		try {
			toExecute = this.getClass().getDeclaredMethod("localhash",
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
