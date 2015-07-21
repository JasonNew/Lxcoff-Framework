package de.tlabs.thinkAir.benchmarkBundle1;

import java.util.LinkedList;

import org.jason.lxcoff.lib.ExecutionController;

import de.tlabs.thinkAir.client.BenchmarkRunner;

public class BenchmarkSet1 {
	private transient ExecutionController controller;

	public BenchmarkSet1(ExecutionController controller) {
		this.controller = controller;
	}

	public String runAllBenchmarks() {
		LinkedList<Benchmark> benchmarks = new LinkedList<Benchmark>();

		Fibo fib = new Fibo(controller);
//		benchmarks.add(fib);
//		BenchmarkRunner.generateLoad(controller, fib, 10, 25);
		BenchmarkRunner.findBoundary(controller, fib, 10, 40);

		Hash hash = new Hash(controller);
//		benchmarks.add(hash);
//		BenchmarkRunner.generateLoad(controller, hash, 500, 1200);
		BenchmarkRunner.findBoundary(controller, hash, 5000, 10000);

//		Hash2 hash2 = new Hash2(controller);
//		// benchmarks.add(hash2);
//		BenchmarkRunner.generateLoad(controller, hash2, 3, 5);

		Heapsort heapsort = new Heapsort(controller);
//		benchmarks.add(heapsort);
//		BenchmarkRunner.generateLoad(controller, heapsort, 475, 775);
		BenchmarkRunner.findBoundary(controller, heapsort, 40000, 60000);

//		Matrix matrix = new Matrix(controller);
//		// benchmarks.add(matrix);
//		BenchmarkRunner.generateLoad(controller, matrix, 1, 3);

		Methcall methcall = new Methcall(controller);
//		benchmarks.add(methcall);
//		BenchmarkRunner.generateLoad(controller, methcall, 2000, 10000);
		BenchmarkRunner.findBoundary(controller, methcall, 2000000, 5000000);

		Nestedloop nested = new Nestedloop(controller);
//		// benchmarks.add(nested);
//		BenchmarkRunner.generateLoad(controller, nested, 6, 8);
		BenchmarkRunner.findBoundary(controller, nested, 6, 25);

//		Objinst obj = new Objinst(controller);
//		benchmarks.add(obj);
//		BenchmarkRunner.generateLoad(controller, obj, 2500, 4000);
//		BenchmarkRunner.findBoundary(controller, obj, 25000, 35000);

//		RandomGen random = new RandomGen(controller);
//		// benchmarks.add(random);
//		BenchmarkRunner.generateLoad(controller, random, 2900, 4500);
//
//		Sieve sieve = new Sieve(controller);
//		// benchmarks.add(sieve);
//		BenchmarkRunner.generateLoad(controller, sieve, 1, 3);
//
//		Strcat str = new Strcat(controller);
//		// benchmarks.add(str);
//		BenchmarkRunner.generateLoad(controller, str, 1800, 3000);
//
//		HeapsortData heapsortdata = new HeapsortData(controller);
//		// benchmarks.add(heapsortdata);
//		BenchmarkRunner.generateLoad(controller, heapsortdata, 2000, 4000);
//
//		SieveData sievedata = new SieveData(controller);
//		// benchmarks.add(sievedata);
//		BenchmarkRunner.generateLoad(controller, sievedata, 200, 550);

//		StrcatData strcatdata = new StrcatData(controller);
//		// benchmarks.add(strcatdata);
//		BenchmarkRunner.generateLoad(controller, strcatdata, 10000, 20000);

//		BenchmarkRunner.findBoundaryPoints(controller, benchmarks);

		return "Benchmarks finished";
	}

}
