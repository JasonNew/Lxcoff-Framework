package de.tlabs.thinkAir.client;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;

import android.util.Log;
import de.tlabs.thinkAir.benchmarkBundle1.Benchmark;
import de.tlabs.thinkAir.lib.ControlMessages;
import de.tlabs.thinkAir.lib.ExecutionController;

public class BenchmarkRunner {
	private static final int ITERATIONS = 100;
	/** Initial upper bound of the boundary value search range */
	private static final int MAXN = 512;
	/**
	 * Hard bound of the boundary value search range (e.g. when the price of
	 * offloading doesn't get better with increasing n
	 */
	private static final int TOPMAXN = 65536;

	public static void findBoundaryPoints(ExecutionController controller,
			LinkedList<Benchmark> benchmarks) {
		HashMap<String, Integer> benchmarkResults = new HashMap<String, Integer>();
		int maxN = MAXN;
		try {
			FileWriter results = new FileWriter(new File(
					"/sdcard/benchmarkResults.txt"), true);

			while (benchmarkResults.size() < benchmarks.size() && maxN <= TOPMAXN) {
				for (Benchmark benchmark : benchmarks) {
					if (!benchmarkResults.containsKey(benchmark.getClass()
							.toString())) {
						int result = BenchmarkRunner.binSearch(controller,
								benchmark, 0, maxN);
						if (result != -1) {
							benchmarkResults.put(benchmark.getClass()
									.toString(), result);
							results.append("Boundary value for "
									+ benchmark.getClass().toString() + " - "
									+ result + "\n");
							results.flush();
						}
					}
				}

				maxN = maxN * 2;
			}

			results.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Search for the point when it pays to offload computations of a certain
	 * Benchmark instead of performing them locally (with the assumption that
	 * with input n increasing the relative price of offloading vs. local
	 * execution decreases)
	 * 
	 * @param o
	 *            the benchmark to run the search on
	 * @param low
	 *            the low end of the range of N values to try for the search
	 * @param high
	 *            the high end of the range
	 * @return the point when it starts to pay to offload or -1 if such point
	 *         not found within the range
	 */
	private static int binSearch(ExecutionController controller, Benchmark o,
			int low, int high) {
		int lo = low;
		int hi = high;
		boolean crashing = false;
		HashMap<Integer, Boolean> cache = new HashMap<Integer, Boolean>();
		while (lo < hi) {
			int mid = lo + ((hi - lo + 1) / 2);
			Log.d("PowerDroid-Benchmark", "Benchmarking "
					+ o.getClass().toString() + " with n - " + mid
					+ " (lo, hi) = " + lo + " " + hi);
			Long localExecTime = 0L;
			Long remoteExecTime = 0L;
			// Do not rerun unnecessary calculations
			if (cache.containsKey(mid)) {
				if (cache.get(mid) == true)
					hi = mid;
				else
					lo = mid + 1;
				continue;
			}
			int iterationsCompleted = 0;
			boolean cont = true;
			// With the same value of N, perform the comparison of execution
			// times for a few times to mitigate the effect of unexpected
			// fluctuations
			for (int i = 0; i < ITERATIONS && cont; i++, iterationsCompleted++) {
				try {
					o.main(mid);
				} catch (Exception e) {
					hi = mid;
					crashing = true;
				}

				localExecTime += controller.lastLocalLogRecord.threadCpuTime;
				remoteExecTime += controller.lastRemoteLogRecord.threadCpuTime;

				// Don't need multiple iterations of the difference of execution
				// time is in the order of magnitude
				if (controller.lastLocalLogRecord.threadCpuTime
						/ controller.lastRemoteLogRecord.threadCpuTime >= 10
						|| controller.lastRemoteLogRecord.threadCpuTime
						/ controller.lastLocalLogRecord.threadCpuTime >= 10)
					cont = false;

			}
			Log.d("PowerDroid-Benchmark", "local average time - "
					+ (localExecTime / iterationsCompleted)
					+ "remote average time - "
					+ (remoteExecTime / iterationsCompleted));
			if (localExecTime / iterationsCompleted < remoteExecTime
					/ iterationsCompleted) {
				lo = mid + 1;
				cache.put(mid, false);
			} else {
				hi = mid;
				cache.put(mid, true);
			}
		}
		if (lo < high) {
			Log.i("PowerDroid-Benchmark", "Benchmark "
					+ o.getClass().toString() + " boundary n - " + lo
					+ " (crashed - " + crashing + ")");
			return lo;
		} else {
			Log.i("PowerDroid-Benchmark", "Benchmark "
					+ o.getClass().toString() + " boundary not found");
			return -1;
		}
	}

	public static void generateLoad(ExecutionController controller,
			Benchmark o, int low, int high) {
		Random rand = new Random(0);
		File logFile = new File("/sdcard/benchmarkRunnerLog.csv");
		try {
			logFile.createNewFile(); // Try creating new, if doesn't exist
			FileWriter logFileWriter = new FileWriter(logFile, true);
			for (int i = 0; i < ITERATIONS; i++) {
				int n = rand.nextInt(high - low + 1) + low;
				o.main(n);
				if (controller.lastLogRecord != null)
					logFileWriter.append(controller.lastLogRecord.toString()
							+ ",n = " + n + "\n");
			}
			Log.i("PowerDroid-Benchmark", "Load generator "
					+ o.getClass().toString() + " finished");
			logFileWriter.close();
		} catch (IOException e) {
			// Don't care
		}
	}

	public static void findBoundary(ExecutionController controller, Benchmark o, int low, int high) {

		Log.i("Sokol-benchmarks", "Executing runnings for: " + o.getClass().toString());
		
		int lo = low;
		int hi = high;
		boolean found = false;
		
		int ITERATIONS = 10;

		File logFile = new File("/sdcard/benchmarkRunnerLogSokol.csv");
		try {
			logFile.createNewFile(); // Try creating new, if doesn't exist
			FileWriter logFileWriter = new FileWriter(logFile, true);

			while (lo < hi) {
				int mid = lo + (hi - lo) / 2;

				Log.i("Sokol-benchmarks", "midpoint = " + mid);
						
				long localExecTime = 0;
				long remoteExecTime = 0;

				controller.setUserChoice(ControlMessages.STATIC_LOCAL);
				for (int i = 0; i < ITERATIONS; i++) {
					o.main(mid);
					localExecTime += controller.lastLogRecord.execDuration;
				}
				localExecTime /= ITERATIONS;

				controller.setUserChoice(ControlMessages.STATIC_REMOTE);
				for (int i = 0; i < ITERATIONS; i++) {
					o.main(mid);
					remoteExecTime += controller.lastLogRecord.execDuration;
				}
				remoteExecTime /= ITERATIONS;

				Log.i("Sokol-benchmarks", "localExecTime=" + localExecTime + " remoteExecTime=" + remoteExecTime);
				
				if (localExecTime < remoteExecTime) {
					lo = mid;
				}
				else {
					found = true;
					logFileWriter.append(controller.lastLogRecord.toString() + ", n = " + mid + "\n");
					hi = mid;
				}
				
				try {
					Thread.sleep(200);
					
					if (mid == hi - 1)
						if (found) {
							break;
						}
						else
							hi *= 2;
					
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			logFileWriter.close();
			
		} catch (IOException e) {
			// Don't care
		}
	}
}
