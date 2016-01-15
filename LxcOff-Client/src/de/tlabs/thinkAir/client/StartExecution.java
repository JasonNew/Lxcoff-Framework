package de.tlabs.thinkAir.client;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;

import android.app.Activity;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.Settings.Secure;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Toast;
import de.tlabs.thinkAir.benchmarkBundle1.BenchmarkSet1;
import de.tlabs.thinkAir.benchmarkBundle2.BenchmarkSet2;
// import de.tlabs.thinkAir.eightQueens.EightQueens;
import de.tlabs.thinkAir.faceDetection.FaceDetection;

import org.jason.lxcoff.lib.Clone;
import org.jason.lxcoff.lib.Configuration;
import org.jason.lxcoff.lib.ControlMessages;
import org.jason.lxcoff.lib.ExecutionController;

import de.tlabs.thinkAir.queens.NQueens;
import de.tlabs.thinkAir.sudoku.Sudoku;
import de.tlabs.thinkAir.synthBenchmark.CalcIntensive;
import de.tlabs.thinkAir.synthBenchmark.JniTest;
import de.tlabs.thinkAir.virus.VirusScanning;

public class StartExecution extends Activity {

	private static final String TAG = "StartExecution";

	private Configuration		config;
	private ExecutionController executionController;
	private Socket dirServiceSocket = null;
	InputStream is					= null;
	OutputStream os					= null;
	ObjectOutputStream oos			= null;
	ObjectInputStream ois			= null;
	
	private static String logFileName = "/sdcard/GameRecord/nqueens.txt";
	private static FileWriter logFileWriter;
	
	public  static int		 	nrClones 				= 1;

	/** Called when the activity is first created. */ 
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		// I wanna use network in main thread, so ...
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
		
		ExecutionController.myId = Secure.getString(this.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);

		createNotOffloadedFile();

		try {
			getInfoFromDirService();

		} catch (FileNotFoundException e) {
			Log.e(TAG, "Could not read the config file: " + ControlMessages.PHONE_CONFIG_FILE);
			return ;
		} /*catch (UnknownHostException e) {
			Log.e(TAG, "Could not connect: " + e.getMessage());
		} */catch (IOException e) {
			Log.e(TAG, "IOException: " + e.getMessage());
		} catch (ClassNotFoundException e) {
			Log.e(TAG, "Could not find Clone class: " + e.getMessage());
			return;
		}
		// Create an execution controller
		executionController = new ExecutionController(
				this.dirServiceSocket,
				is, os, ois, oos,
				getPackageName(),
				getPackageManager(),
				this);
		
		this.startNewLog();
		
		Spinner nrClonesSpinner = (Spinner) findViewById(R.id.spinnerNrClones);
		nrClonesSpinner.setOnItemSelectedListener(new MyOnItemSelectedListener());
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		try {
			// Close the connection with the DirService
			dirServiceSocket.close();
		} catch (Exception e) {
			Log.w(TAG, "It seems we never connected to the DirectoryService.");
		}
		executionController.onDestroy();
	}

	/**
	 * Create an empty file on the phone in order to let the method know
	 * where is being executed (on the phone or on the clone).
	 */
	private void createNotOffloadedFile(){
		try {
			File f = new File(ControlMessages.FILE_NOT_OFFLOADED);
			f.createNewFile();
		} catch (FileNotFoundException e) {
			Log.e(TAG, e.getMessage());
		} catch (IOException e) {
			Log.e(TAG, e.getMessage());
		}
	}

	/**
	 * Read the config file to get the IP and port for DirectoryService.
	 * @throws IOException 
	 * @throws UnknownHostException 
	 * @throws ClassNotFoundException 
	 */
	private void getInfoFromDirService() throws UnknownHostException, IOException, ClassNotFoundException {
		config = new Configuration(ControlMessages.PHONE_CONFIG_FILE);
		config.parseConfigFile(null, null);
		
		try{
			this.dirServiceSocket  = new Socket(config.getDirServiceIp(), config.getDirServicePort());
			this.os = this.dirServiceSocket.getOutputStream();
			this.is = this.dirServiceSocket.getInputStream();

			os.write(ControlMessages.PHONE_CONNECTION);

			oos = new ObjectOutputStream(os);
			ois = new ObjectInputStream(is);

			// Send the name and id to DirService
			os.write(ControlMessages.PHONE_AUTHENTICATION);
			oos.writeObject(ExecutionController.myId);
			oos.flush();
			
		} 
		finally {
			
		}
	}

	public void onClickLoader1(View v) {
		DummyRemoteable remote = new DummyRemoteable(executionController);

		String result = remote.cpuLoader1();

		Toast.makeText(StartExecution.this, result, Toast.LENGTH_SHORT)
		.show();
	}

	public void onClickLoader2(View v) {
		DummyRemoteable remote = new DummyRemoteable(executionController);

		String result = remote.cpuLoader2();

		Toast.makeText(StartExecution.this, result, Toast.LENGTH_SHORT)
		.show();
	}

	public void onClickLoader3(View v) {
		DummyRemoteable remote = new DummyRemoteable(executionController);

		Long result = remote.cpuLoader3(1);

		Toast.makeText(StartExecution.this, Long.toString(result),
				Toast.LENGTH_SHORT).show();
	}

	public void onClickScenario1(View v) {
		CalcIntensive remote = new CalcIntensive(executionController);

		remote.scenario1();

		Toast.makeText(StartExecution.this, "scenario1 finished",
				Toast.LENGTH_SHORT).show();
	}

	public void onClickScenario2(View v) {
		CalcIntensive remote = new CalcIntensive(executionController);

		remote.scenario2();

		Toast.makeText(StartExecution.this, "scenario2 finished",
				Toast.LENGTH_SHORT).show();
	}

	public void onClickJni1(View v) {
		JniTest remote = new JniTest(executionController);

		String result = remote.jniCaller();

		Toast.makeText(StartExecution.this, result, Toast.LENGTH_SHORT)
		.show();
	}

	public void onClickBenchmark1(View v) {
		BenchmarkSet1 benchmark = new BenchmarkSet1(executionController);

		String result = benchmark.runAllBenchmarks();

		Toast.makeText(StartExecution.this, result, Toast.LENGTH_SHORT)
		.show();
	}

	public void onClickBenchmark2(View v) {
		BenchmarkSet2 benchmark = new BenchmarkSet2(executionController);

		String result = benchmark.runAllBenchmarks();

		Toast.makeText(StartExecution.this, result, Toast.LENGTH_SHORT)
		.show();
	}

	public void onClickBenchmarksAll(View v) {
		BenchmarkSet2 benchmark = new BenchmarkSet2(executionController);

		String result = benchmark.runAllBenchmarks();

		Toast.makeText(StartExecution.this, result, Toast.LENGTH_SHORT)
		.show();

		BenchmarkSet2 benchmark2 = new BenchmarkSet2(executionController);

		result = benchmark2.runAllBenchmarks();

		Toast.makeText(StartExecution.this, result, Toast.LENGTH_SHORT)
		.show();
	}

	public void onClickSudoku(View v) {

		Sudoku sudoku = new Sudoku(executionController);

		boolean result = sudoku.hasSolution();

		if(result)
			Toast.makeText(StartExecution.this, "Sudoku solved",	Toast.LENGTH_SHORT)
			.show();
		else
			Toast.makeText(StartExecution.this, "Sudoku NOT solved",	Toast.LENGTH_SHORT)
			.show();
	}

	public void onClickFaceDetection(View v) {

		Spinner nrPhotosSpinner = (Spinner) findViewById(R.id.spinnerNrPhotos);
		int nrPhotos = Integer.parseInt( (String) nrPhotosSpinner.getSelectedItem() );
		FaceDetection faceDetection = new FaceDetection(executionController, nrClones);
		int result = faceDetection.detectFaces(ControlMessages.FACE_PICTURE_TEST, nrPhotos);

		Log.d(TAG, "Nr duplicated photos: " + result);
	}

	public void onClickVirusScanning(View v) {

		VirusScanning virusScanner = new VirusScanning( getApplicationContext(), executionController, nrClones );

		int result = virusScanner.scanFolder();
		Log.d(TAG, "Number of viruses found: " + result);
	}

	public void onClickQueenSolver(View v) {

		Spinner nrQueensSpinner = (Spinner) findViewById(R.id.spinnerNrQueens);
		int nrQueens = Integer.parseInt( (String) nrQueensSpinner.getSelectedItem() );

		long stime = System.nanoTime();
		NQueens puzzle = new NQueens(executionController, nrClones);

		Log.i(TAG, "Nr Queens: " + nrQueens);
		
		int[] nqueens = {8, 7, 6, 8, 5, 6, 7, 7, 8, 7, 5, 4, 6, 6, 7, 8, 8, 7};
		int result = 0;
		for(int i=0; i<18; i++){
			result = puzzle.solveNQueens(nqueens[i]);
			try {
				Thread.currentThread().sleep(2500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		long dura = System.nanoTime() - stime;

		Log.i(TAG, "EightQueens solved, solutions: " + result);
/*		if (logFileWriter != null) {
			try {
				logFileWriter.append(dura/1000000 + "\n");
				logFileWriter.flush();

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}*/
		Toast.makeText(StartExecution.this, "EightQueens solved, solutions: " + result + ". Cost " + dura/1000000 + " ms.",	Toast.LENGTH_SHORT)
		.show();
	}

	public void onRadioExecLocationChecked(View radioButton) {

		boolean checked = ((RadioButton) radioButton).isChecked();

		switch(radioButton.getId()) {

		case R.id.radio_local:
			if (checked) executionController.setUserChoice(ControlMessages.STATIC_LOCAL);
			break;

		case R.id.radio_remote:
			if (checked) executionController.setUserChoice(ControlMessages.STATIC_REMOTE);
			break;

		case R.id.radio_exec_time:
			if (checked) executionController.setUserChoice(ControlMessages.USER_CARES_ONLY_TIME);
			break;

		case R.id.radio_energy:
			if (checked) executionController.setUserChoice(ControlMessages.USER_CARES_ONLY_ENERGY);
			break;

		case R.id.radio_exec_time_energy:
			if (checked) executionController.setUserChoice(ControlMessages.USER_CARES_TIME_ENERGY);
			break;
		}
	}

	private class MyOnItemSelectedListener implements OnItemSelectedListener {

		public void onItemSelected(AdapterView<?> parent, View view, int pos,
				long id) {

			nrClones = Integer.parseInt( (String) parent.getItemAtPosition(pos) );
			Log.i(TAG, "Number of clones: " + nrClones);

			executionController.setNrClones(nrClones);
		}

		public void onNothingSelected(AdapterView<?> arg0) {
			Log.i(TAG, "Nothing selected on clones spinner");
		}
	}
	
	public void startNewLog(){
		if (logFileWriter != null){
			try {
				logFileWriter.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		try {
			File logFile = new File(logFileName);
			logFile.createNewFile(); // Try creating new, if doesn't exist
			logFileWriter = new FileWriter(logFile, false);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
