package de.tlabs.thinkAir.benchmarkBundle2;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import de.tlabs.thinkAir.client.BenchmarkRunner;
import de.tlabs.thinkAir.lib.ExecutionController;

public class BenchmarkSet2 {
	private transient ExecutionController controller;

	public BenchmarkSet2(ExecutionController controller) {
		this.controller = controller;
	}

	public String runAllBenchmarks() {

		try {
			FileWriter results = new FileWriter(new File(
					"/sdcard/benchmarkResults.txt"), true);

			binarytrees btrees = new binarytrees(controller);
			// results.append("Boundary value for "
			// + btrees.getClass().toString() + " - "
			// + binSearch(btrees, 1, 3) + "\n");
			// results.flush();
			BenchmarkRunner.generateLoad(controller, btrees, 1, 3);

			knucleotide knucleo = new knucleotide(controller);
			// results.append("Boundary value for "
			// + knucleo.getClass().toString() + " - "
			// + binSearch(knucleo, 1, 3) + "\n");
			// results.flush();
			BenchmarkRunner.generateLoad(controller, knucleo, 1, 3);

			mandelbrot mbrot = new mandelbrot(controller);
			// results.append("Boundary value for "
			// + mbrot.getClass().toString() + " - "
			// + binSearch(mbrot, 350, 400) + "\n");
			// results.flush();
			BenchmarkRunner.generateLoad(controller, mbrot, 150, 300);

			nbody nb = new nbody(controller);
			// results.append("Boundary value for "
			// + nb.getClass().toString() + " - "
			// + binSearch(nb, 240, 270) + "\n");
			// results.flush();
			BenchmarkRunner.generateLoad(controller, nb, 240, 270);

			// REgex offloading doesn't pay
			regexdna regex = new regexdna(controller);
			// results.append("Boundary value for " +
			// regex.getClass().toString()
			// + " - " + binSearch(regex, 1000, 50000) + "\n");
			// results.flush();
			BenchmarkRunner.generateLoad(controller, regex, 1, 1000);

			spectralnorm spectral = new spectralnorm(controller);
			// results.append("Boundary value for "
			// + spectral.getClass().toString() + " - "
			// + binSearch(spectral, 8, 12) + "\n");
			// results.flush();
			BenchmarkRunner.generateLoad(controller, spectral, 8, 12);

			results.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return "Benchmarks finished";
	}

	
}
