package de.tlabs.thinkAir.benchmarkBundle1;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import de.tlabs.thinkAir.lib.ExecutionController;
import de.tlabs.thinkAir.lib.Remote;
import de.tlabs.thinkAir.lib.Remoteable;

public class Hash2 extends Remoteable implements Benchmark {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1788403775229369611L;

	private transient ExecutionController controller;

	public Hash2(ExecutionController controller) {
		this.controller = controller;
	}

	public void main(int n) {
		hash(n);
	}

	@Remote
	public String localhash(int n) {
		HashMap<String, Val> hash1 = new HashMap<String, Val>(10000);
		HashMap<String, Val> hash2 = new HashMap<String, Val>(n);
		for (int i = 0; i < 10000; i++)
			hash1.put("foo_" + Integer.toString(i, 10), new Val(i));
		for (int i = 0; i < n; i++) {
			Iterator<Entry<String, Val>> it = hash1.entrySet().iterator();
			while (it.hasNext()) {
				Entry<String, Val> h1 = it.next();
				String key = (String) h1.getKey();
				int v1 = ((Val) h1.getValue()).val;
				if (hash2.containsKey(key))
					hash2.get(key).val += v1;
				else
					hash2.put(key, new Val(v1));
			}
		}
		return (hash1.get("foo_1").val + " "
				+ hash1.get("foo_9999").val + " "
				+ hash2.get("foo_1").val + " "
				+ hash2.get("foo_9999").val + "\n");
	}

	@Override
	public void copyState(Remoteable arg0) {
	}

	public String hash(int n) {
		Method toExecute;
		Class<?>[] paramTypes = { int.class };
		Object[] paramValues = { n };
		String result = null;
		try {
			toExecute = this.getClass().getDeclaredMethod("localhash",
					paramTypes);
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

}

class Val {

	int val;

	Val(int init) {
		val = init;
	}

}
