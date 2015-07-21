// $Id: matrix.java,v 1.3 2001/05/27 14:52:57 doug Exp $

package de.tlabs.thinkAir.benchmarkBundle1;

import java.lang.reflect.Method;

import org.jason.lxcoff.lib.ExecutionController;
import org.jason.lxcoff.lib.Remote;
import org.jason.lxcoff.lib.Remoteable;

public class Matrix extends Remoteable implements Benchmark {

    /**
	 * 
	 */
    private static final long serialVersionUID = 8689575182013848097L;

    int SIZE = 30;

    private transient ExecutionController controller;

	public Matrix(ExecutionController controller) {
		this.controller = controller;
	}
	
    @Remote
    public void localmain(int n) {
        int m1[][] = mkmatrix(SIZE, SIZE);
        int m2[][] = mkmatrix(SIZE, SIZE);
        int mm[][] = new int[SIZE][SIZE];
        for (int i = 0; i < n; i++) {
            mmult(SIZE, SIZE, m1, m2, mm);
        }
    }

    public int[][] mkmatrix(int rows, int cols) {
        int count = 1;
        int m[][] = new int[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                m[i][j] = count++;
            }
        }
        return (m);
    }

    public static void mmult(int rows, int cols, int[][] m1, int[][] m2, int[][] m3) {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                int val = 0;
                for (int k = 0; k < cols; k++) {
                    val += m1[i][k] * m2[k][j];
                }
                m3[i][j] = val;
            }
        }
    }

    @Override
    public void copyState(Remoteable state) {
        Matrix localState = (Matrix) state;
        this.SIZE = localState.SIZE;
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
