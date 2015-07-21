package de.tlabs.thinkAir.queens;

import java.lang.reflect.Method;

import org.jason.lxcoff.lib.ControlMessages;
import org.jason.lxcoff.lib.ExecutionController;
import org.jason.lxcoff.lib.Remote;
import org.jason.lxcoff.lib.Remoteable;
import android.util.Log;

public class NQueens extends Remoteable {

	//private static final long	serialVersionUID = 5687713591581731140L;
	private static final long serialVersionUID = 1L;
	private static final String TAG = "NQueens";
	private int 				N = 8;
	private int					nrClones;
	private transient ExecutionController controller;

	/**
	 * @param controller	The execution controller taking care of the execution
	 * @param nrClones		In case of remote execution specify the number of clones needed
	 */
	public NQueens(ExecutionController controller, int nrClones) {
		this.controller = controller;
		this.nrClones	= nrClones;
	}
	
	/**
	 * @param controller	The execution controller taking care of the execution
	 * @param nrClones		In case of remote execution specify the number of clones needed
	 */
	public NQueens(ExecutionController controller) {
		this.controller = controller;
	}

	/**
	 * Solve the N-queens problem
	 * @param N	The number of queens
	 * @return The number of solutions found
	 */
	public int solveNQueens(int N)
	{
		this.N = N;
		Method toExecute;
		Class<?>[] paramTypes = {int.class, MyClass.class};
		Object[] paramValues = {N, new MyClass()};

		int result = 0;
		try {
			toExecute = this.getClass().getDeclaredMethod("localSolveNQueens", paramTypes);
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

	@Remote
	public int localSolveNQueens(int N, MyClass myclass) {

		byte[][] board = new byte[N][N];
		int countSolutions = 0;

		// cloneId == -1 if this is the main clone
		// or [0, nrClones-2] otherwise
		int cloneId 		= ControlMessages.readCloneId();
		int howManyRows 	= (int) (N / nrClones);				// Integer division, we may loose some rows. 
		int start 			= (cloneId + 1) * howManyRows; 		// Add 1 since cloneId == -1 if this is the main clone 
		int end 			= start + howManyRows;
		
		// If this is the clone with the highest id let him take care
		// of the rows not considered due to the integer division. 
		if (cloneId == nrClones-2)
			end += N % nrClones;
		
		Log.i(TAG, "Finding solutions for " + N + "-queens puzzle.");
		Log.i(TAG, "Analyzing rows: " + start + "-" +(end-1));
		
		//my god! Complexity N^N 
		for (int i = start; i < end; i++) {
			for (int j = 0; j < N; j++) {
				for (int k = 0; k < N; k++) {
					for (int l = 0; l < N; l++) {
						if (N == 4) {
							countSolutions += setAndCheckBoard(board, new int[]{i, j, k, l});
							continue;
						}
						for (int m = 0; m < N; m++) {
							if (N == 5) {
								countSolutions += setAndCheckBoard(board, new int[]{i, j, k, l, m});
								continue;
							}
							for (int n = 0; n < N; n++) {
								if (N == 6) {
									countSolutions += setAndCheckBoard(board, new int[]{i, j, k, l, m, n});
									continue;
								}
								for (int o = 0; o < N; o++) {
									if (N == 7) {
										countSolutions += setAndCheckBoard(board, new int[]{i, j, k, l, m, n, o});
										continue;
									}
									for (int p = 0; p < N; p++) {
											countSolutions += setAndCheckBoard(board, new int[]{i, j, k, l, m, n, o, p});
									}
								}
							}
						}
					}
				}
			}
		}

		Log.i(TAG, "Found " + countSolutions + " solutions.");
		return countSolutions;
	}
	
	/**
	 * When having more than one clone running the method there will be partial results
	 * which should be combined to get the total result.
	 * This will be done automatically by the main clone by calling
	 * this method.
	 * @param params Array of partial results.
	 * @return The total result.
	 */
	public int localSolveNQueensReduce(int[] params) {
		int solutions = 0;
		for (int i = 0; i < params.length; i++) {
			Log.i(TAG, "Adding " + params[i] + " partial solutions.");
			solutions += params[i];
		}
		return solutions;
	}
	
	private int setAndCheckBoard(byte[][] board, int... cols) {
		
		clearBoard(board);
		
		for (int i = 0; i < N; i++)
			board[i][cols[i]] = 1;
		
		if (isSolution(board)) return 1;
		
		return 0;
	}

	private void clearBoard(byte[][] board) {
		for (int i = 0; i < N; i ++) {
			for (int j = 0; j < N; j++) {
				board[i][j] = 0;
			}
		}
	}

	private boolean isSolution(byte[][] board) {

		int rowSum = 0;
		int colSum = 0;

		for (int i = 0; i < N; i++) {
			for (int j = 0;  j < N; j++) {
				rowSum += board[i][j];
				colSum += board[j][i];

				if (i == 0 || j == 0)
					if ( !checkDiagonal1(board, i, j) ) return false;

				if (i == 0 || j == N-1)
					if ( !checkDiagonal2(board, i, j) ) return false;

			}
			if (rowSum > 1 || colSum > 1) return false;
			rowSum = 0;
			colSum = 0;
		}

		return true;
	}

	private boolean checkDiagonal1(byte[][] board, int row, int col) {
		int sum = 0;
		int i = row;
		int j = col;
		while (i < N && j < N) {
			sum += board[i][j];
			i++;
			j++;
		}
		return sum <= 1;
	}

	private boolean checkDiagonal2(byte[][] board, int row, int col) {
		int sum = 0;
		int i = row;
		int j = col;
		while (i < N && j >=0) {
			sum += board[i][j];
			i++;
			j--;
		}
		return sum <= 1;
	}


	private void printBoard(byte[][] board) {
		for (int i = 0; i < N; i++) {
			StringBuilder row = new StringBuilder();
			for (int j = 0;  j < N; j++) {
				row.append(board[i][j]);
				if (j < N - 1)
					row.append(" ");
			}
			Log.i(TAG, row.toString());
		}
		Log.i(TAG, "\n");
	}

	public void setNumberOfClones(int nrClones) {
		this.nrClones = nrClones;
	}
	
	@Override
	public void copyState(Remoteable state) {

	}
}

