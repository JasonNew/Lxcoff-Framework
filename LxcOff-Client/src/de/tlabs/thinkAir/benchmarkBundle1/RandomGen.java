package de.tlabs.thinkAir.benchmarkBundle1;

import java.lang.reflect.Method;
import java.text.NumberFormat;

import org.jason.lxcoff.lib.ExecutionController;
import org.jason.lxcoff.lib.Remote;
import org.jason.lxcoff.lib.Remoteable;

public class RandomGen extends Remoteable implements Benchmark {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8677397173995345604L;

	private transient ExecutionController controller;

	public RandomGen(ExecutionController controller) {
		this.controller = controller;
	}

	public static final long IM = 139968;

	public static final long IA = 3877;

	public static final long IC = 29573;

	@Remote
	public String localmain(int n) {
		int N = n - 1;
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(9);
		nf.setMinimumFractionDigits(9);
		nf.setGroupingUsed(false);
		while (N-- > 0) {
			gen_random(100);
		}
		return nf.format(gen_random(100));
	}

	public static long last = 42;

	
	public static double gen_random(double max) {
		return (max * (last = (last * IA + IC) % IM) / IM);
	}

	@Override
	public void copyState(Remoteable state) {
		RandomGen localState = (RandomGen) state;
		RandomGen.last = localState.last;
	}

	public void main(int n) {
		Method toExecute;
		Class<?>[] paramTypes = { int.class };
		Object[] paramValues = { n };
		String result = null;
		try {
			toExecute = this.getClass().getDeclaredMethod("localmain",
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
	}

}
