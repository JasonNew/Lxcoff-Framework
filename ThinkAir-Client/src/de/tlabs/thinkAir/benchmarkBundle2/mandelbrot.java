/* The Computer Language Benchmarks Game
http://shootout.alioth.debian.org/

contributed by Stefan Krause
slightly modified by Chad Whipkey
parallelized by Colin D Bennett 2008-10-04
reduce synchronization cost by The Anh Tran
avoid shared memory access + steady state toggle by Tamas Cserveny 2010-01-08
 */

package de.tlabs.thinkAir.benchmarkBundle2;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;

import de.tlabs.thinkAir.benchmarkBundle1.Benchmark;
import de.tlabs.thinkAir.lib.ExecutionController;
import de.tlabs.thinkAir.lib.Remote;
import de.tlabs.thinkAir.lib.Remoteable;

/**
 * Each program should plot the Mandelbrot set [-1.5-i,0.5+i] on an N-by-N
 * bitmap. Write output byte-by-byte in portable bitmap format.
 * 
 * Modified for the PowerDroid framework by Andrius Aucinas
 * 
 * @author Andrius
 * 
 */
public class mandelbrot extends Remoteable implements Benchmark{

	/**
	 * 
	 */
	private static final long serialVersionUID = 7861890086600553120L;
	
	private transient ExecutionController controller;

	public mandelbrot(ExecutionController controller) {
		this.controller = controller;
	}
	

	public void main(int n) {
		int N = n;
	
		System.out.format("P4\n%d %d\n", N, N);

		System.out.print(mandelbrot(N));
	}
	
	@Remote
	private byte[] localmandelbrot(int N) throws IOException{
		int processors = Runtime.getRuntime().availableProcessors();
		WorkerThread[] pool = new WorkerThread[processors];

		int job = (int) Math.floor(((double) N / (double) processors));

		for (int i = 0; i < pool.length; i++) {
			pool[i] = new WorkerThread(N, i * job, Math.min((i + 1) * job, N));
			pool[i].start();
		}
		
		ByteArrayOutputStream output = new ByteArrayOutputStream();

		for (WorkerThread t : pool) {
			try {
				t.join();

				t.output(output);

			} catch (InterruptedException e) {
				e.printStackTrace();
				break;
			}
		}
		return output.toByteArray();
	}

	private static class WorkerThread extends Thread {

		int[] bytes_per_line;
		byte[][] output_data;
		int from;
		int to;
		int delta;
		int N;
		double inverse_N;
		int width_bytes;

		public WorkerThread(int N, int from, int to) {
			this.from = from;
			this.to = to;
			this.N = N;
			inverse_N = 2.0 / N;

			delta = to - from;

			bytes_per_line = new int[delta];
			output_data = new byte[delta][N / 8 + 1];
		}

		public void output(OutputStream ostream) throws IOException {
			for (int i = 0; i < delta; i++) {
				ostream.write(output_data[i], 0, bytes_per_line[i]);
			}
		}

		@Override
		public void run() {
			int y = from;
			int d = 0;
			while ((y = from++) < to) {
				byte[] pdata = output_data[d];

				int bit_num = 0;
				int byte_count = 0;
				int byte_accumulate = 0;

				double Civ = (double) y * inverse_N - 1.0;

				for (int x = 0; x < N; x++) {
					double Crv = (double) x * inverse_N - 1.5;

					double Zrv = Crv;
					double Ziv = Civ;

					double Trv = Crv * Crv;
					double Tiv = Civ * Civ;

					int i = 49;
					do {
						Ziv = (Zrv * Ziv) + (Zrv * Ziv) + Civ;
						Zrv = Trv - Tiv + Crv;

						Trv = Zrv * Zrv;
						Tiv = Ziv * Ziv;
					} while (((Trv + Tiv) <= 4.0) && (--i > 0));

					byte_accumulate <<= 1;
					if (i == 0) {
						byte_accumulate++;
					}

					if (++bit_num == 8) {
						pdata[byte_count++] = (byte) byte_accumulate;
						bit_num = byte_accumulate = 0;
					}
				}

				if (bit_num != 0) {
					byte_accumulate <<= (8 - (N & 7));
					pdata[byte_count++] = (byte) byte_accumulate;
				}

				bytes_per_line[d++] = byte_count;
			}
		}
	}

	@Override
	public void copyState(Remoteable arg0) {
		// TODO Auto-generated method stub
		
	}
	
	private byte[] mandelbrot (int N) {
	       Method toExecute;
	       Class<?>[] paramTypes = {int.class};
	       Object[] paramValues = { N};
	       byte[] result = null;
	       try {
	           toExecute = this.getClass().getDeclaredMethod("localmandelbrot", paramTypes);
	           result = (byte[]) controller.execute(toExecute, paramValues, this);
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