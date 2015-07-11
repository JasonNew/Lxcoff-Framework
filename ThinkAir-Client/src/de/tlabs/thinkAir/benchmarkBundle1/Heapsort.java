// $Id: heapsort.java,v 1.6 2001/05/08 02:46:59 doug Exp $

package de.tlabs.thinkAir.benchmarkBundle1;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.text.NumberFormat;

import de.tlabs.thinkAir.lib.ExecutionController;
import de.tlabs.thinkAir.lib.Remote;
import de.tlabs.thinkAir.lib.Remoteable;

public class Heapsort extends Remoteable implements Benchmark {

    /**
	 * 
	 */
    private static final long serialVersionUID = -6197257058118323962L;

    private transient ExecutionController controller;

	public Heapsort(ExecutionController controller) {
		this.controller = controller;
	}
    public static final long IM = 139968;

    public static final long IA = 3877;

    public static final long IC = 29573;

    @Remote
    public void localmain(int n) {
        int N = n;
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(10);
        nf.setMinimumFractionDigits(10);
        nf.setGroupingUsed(false);
        double[] ary = (double[]) Array.newInstance(double.class, N + 1);
        for (int i = 1; i <= N; i++) {
            ary[i] = gen_random(1);
        }
        heapsort(N, ary);
    }

    public long last = 42;

    public double gen_random(double max) {
        return (max * (last = (last * IA + IC) % IM) / IM);
    }

    public void heapsort(int n, double ra[]) {
        int l, j, ir, i;
        double rra;
        l = (n >> 1) + 1;
        ir = n;
        for (; ; ) {
            if (l > 1) {
                rra = ra[--l];
            } else {
                rra = ra[ir];
                ra[ir] = ra[1];
                if (--ir == 1) {
                    ra[1] = rra;
                    return;
                }
            }
            i = l;
            j = l << 1;
            while (j <= ir) {
                if (j < ir && ra[j] < ra[j + 1]) {
                    ++j;
                }
                if (rra < ra[j]) {
                    ra[i] = ra[j];
                    j += (i = j);
                } else {
                    j = ir + 1;
                }
            }
            ra[i] = rra;
        }
    }

    @Override
    public void copyState(Remoteable state) {
        Heapsort localState = (Heapsort) state;
        this.last = localState.last;
    }
    
    public void main (int n) {
       Method toExecute;
       Class<?>[] paramTypes = {int.class};
       Object[] paramValues = { n};
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
