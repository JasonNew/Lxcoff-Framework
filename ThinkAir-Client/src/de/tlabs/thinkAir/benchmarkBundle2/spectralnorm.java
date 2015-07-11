package de.tlabs.thinkAir.benchmarkBundle2;

/*
 The Great Computer Language Shootout
 http://shootout.alioth.debian.org/

 contributed by Java novice Jarkko Miettinen
 modified ~3 lines of the original C#-version
 by Isaac Gouy
 */

import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import de.tlabs.thinkAir.benchmarkBundle1.Benchmark;
import de.tlabs.thinkAir.lib.ExecutionController;
import de.tlabs.thinkAir.lib.Remote;
import de.tlabs.thinkAir.lib.Remoteable;

/**
 * Each program should calculate the spectral norm of an infinite matrix A, with
 * entries a11=1, a12=1/2, a21=1/3, a13=1/4, a22=1/5, a31=1/6, etc. <br/>
 * Each program must implement 4 separate functions / procedures / methods like
 * the C# program.<br/>
 * For more information see challenge #3 in Eric W. Weisstein,
 * "Hundred-Dollar, Hundred-Digit Challenge Problems" and "Spectral Norm".
 * 
 * Modified for the PowerDroid framework by Andrius Aucinas
 * 
 * @author Andrius
 * 
 */
public class spectralnorm extends Remoteable implements Benchmark{

	/**
	 * 
	 */
	private static final long serialVersionUID = -4686682671338337510L;
	
	private transient ExecutionController controller;

	public spectralnorm(ExecutionController controller) {
		this.controller = controller;
	}

	
	private static final NumberFormat formatter = new DecimalFormat(
			"#.000000000");

	public void main(int n) {
		System.out.println(formatter.format(new spectralnorm(controller).Approximate(n)));
	}

	@Remote
	private final double localApproximate(int n) {
		// create unit vector
		double[] u = new double[n];
		for (int i = 0; i < n; i++)
			u[i] = 1;

		// 20 steps of the power method
		double[] v = new double[n];
		for (int i = 0; i < n; i++)
			v[i] = 0;

		for (int i = 0; i < 10; i++) {
			MultiplyAtAv(n, u, v);
			MultiplyAtAv(n, v, u);
		}

		// B=AtA A multiplied by A transposed
		// v.Bv /(v.v) eigenvalue of v
		double vBv = 0, vv = 0;
		for (int i = 0; i < n; i++) {
			vBv += u[i] * v[i];
			vv += v[i] * v[i];
		}

		return Math.sqrt(vBv / vv);
	}

	/* return element i,j of infinite matrix A */
	private final double A(int i, int j) {
		return 1.0 / ((i + j) * (i + j + 1) / 2 + i + 1);
	}

	/* multiply vector v by matrix A */
	private final void MultiplyAv(int n, double[] v, double[] Av) {
		for (int i = 0; i < n; i++) {
			Av[i] = 0;
			for (int j = 0; j < n; j++)
				Av[i] += A(i, j) * v[j];
		}
	}

	/* multiply vector v by matrix A transposed */
	private final void MultiplyAtv(int n, double[] v, double[] Atv) {
		for (int i = 0; i < n; i++) {
			Atv[i] = 0;
			for (int j = 0; j < n; j++)
				Atv[i] += A(j, i) * v[j];
		}
	}

	/* multiply vector v by matrix A and then by matrix A transposed */
	private final void MultiplyAtAv(int n, double[] v, double[] AtAv) {
		double[] u = new double[n];
		MultiplyAv(n, v, u);
		MultiplyAtv(n, u, AtAv);
	}

	@Override
	public void copyState(Remoteable arg0) {
		// No fields - no copying
	}
	
	private final  double Approximate (int n) {
	       Method toExecute;
	       Class<?>[] paramTypes = {int.class};
	       Object[] paramValues = { n};
	       double result = 0;
	       try {
	           toExecute = this.getClass().getDeclaredMethod("localApproximate", paramTypes);
	           result = (Double) controller.execute(toExecute, paramValues, this);
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
