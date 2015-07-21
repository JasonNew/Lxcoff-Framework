package de.tlabs.thinkAir.virus;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.content.Context;
import android.util.Log;

import org.jason.lxcoff.lib.ControlMessages;
import org.jason.lxcoff.lib.ExecutionController;
import org.jason.lxcoff.lib.Remote;
import org.jason.lxcoff.lib.Remoteable;
import org.jason.lxcoff.lib.ZipHandler;

public class VirusScanning extends Remoteable {

	private static final long serialVersionUID = -1839651210541446342L;

	private static final String 			TAG = "VirusScanning";
	private String[]						signatureDB;
	private byte[]							zippedFolder;
//	private String							pathToFolder;
//	private String							pathToSignatures;
	private static final int 				SIGNATURE_SIZE = 1024;
	private transient ExecutionController	controller;
	private int								nrClones;

	public VirusScanning(Context ctx, ExecutionController controller, int nrClones) {
		
		this.controller = controller;
		this.nrClones = nrClones;
		
		initFolderToScan2();
		//		initFolderToScan(pathToFolder);
	}
	
	public int scanFolder() {
		int nrVirusesFound = 0;
		Method toExecute;
		Class<?>[] paramTypes = null;
		Object[] paramValues = null;

		try {
			toExecute = this.getClass().getDeclaredMethod("localScanFolder", paramTypes);
			nrVirusesFound = (Integer) controller.execute(toExecute, paramValues, this);
		} catch (SecurityException e) {
			// Should never get here
			e.printStackTrace();
			throw e;
		} catch (NoSuchMethodException e) {
			// Should never get here
			e.printStackTrace();
		} catch (Throwable e) {
			e.printStackTrace();
		}

		return nrVirusesFound;
	}


	private void extractZippedFiles() {
		//		ZipHandler.extractBytes(zippedFolder);

		try {
			File f = new File(ControlMessages.VIRUS_FOLDER_ZIP);
			FileOutputStream out = new FileOutputStream(f);
			out.write(zippedFolder);
			out.flush();
			out.close();

			ControlMessages.executeShellCommand(TAG, "busybox tar -xzf " + ControlMessages.VIRUS_FOLDER_ZIP +
					" -C /", false);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Remote
	public int localScanFolder() {
		
		Log.i(TAG, "Scan folder");
		
		Log.i(TAG, "Started signature initialization on folder: " + ControlMessages.VIRUS_DB_PATH);
		initSignatureDB(ControlMessages.VIRUS_DB_PATH);
		Log.i(TAG, "Finished signature initialization");
		
		int nrVirusesFound = 0;
		boolean isOffloaded = ControlMessages.checkIfOffloaded();
		int cloneId = -1;
		
		if ( isOffloaded ) {
			Log.i(TAG, "isOffloaded true");
			extractZippedFiles();
			
			cloneId	= ControlMessages.readCloneId();
			
		} else {
			Log.i(TAG, "isOffloaded false");
		}

		Log.i(TAG, "Scanning folder: " + ControlMessages.VIRUS_FOLDER_TO_SCAN);
		File 	folderToScan	= new File(ControlMessages.VIRUS_FOLDER_TO_SCAN);
		File[] 	filesToScan		= folderToScan.listFiles();
		
		int 	howManyFiles 	= (int) ( filesToScan.length / nrClones ); // Integer division, some files may be not considered
		int 	start			= (cloneId + 1) * howManyFiles;	// Since cloneId starts from -1 (the main clone)
		int		end				= start + howManyFiles;
		
		// If this is the clone with the highest id let him take care
		// of the files not considered due to the integer division. 
		if (cloneId == nrClones-2)
			end += filesToScan.length % nrClones;
		
		Log.i(TAG, "Nr signatures: " + signatureDB.length + ". Nr files to scan: " + howManyFiles);
		Log.i(TAG, "Checking files: " + start + "-" + end);
		
		for (int i = start; i < end; i ++ ) {
			Log.i(TAG, "Checking file: " + i);
			if (heavyCheckIfFileVirus(filesToScan[i])) {
//			if (checkIfFileVirus(filesToScan[i])) {
				Log.i(TAG, "Virus found");
				nrVirusesFound++;
			}
		}

		return nrVirusesFound;
	}
	
	/**
	 * When having more than one clone running the method there will be partial results
	 * which should be combined to get the total result.
	 * This will be done automatically by the main clone by calling
	 * this method.
	 * @param params Array of partial results.
	 * @return The total result.
	 */
	public int localScanFolderReduce(int[] params) {
		int nrViruses = 0;
		for (int i = 0; i < params.length; i++) {
			nrViruses += params[i];
		}
		return nrViruses;
	}

	/**
	 * Compare ONLY the initial bytes with the virus signatures.
	 * @param virus
	 * @return
	 */
	private boolean checkIfFileVirus(File virus) {
		MessageDigest md;

		try {
			md = MessageDigest.getInstance("SHA-1");
			char[] buffer = new char[SIGNATURE_SIZE];
//			Log.i(TAG, "Checking file " + virus.getName());
			FileReader currentFile = new FileReader(virus);
			int totalRead =	 0;
			int read = 0;
			while ( totalRead != SIGNATURE_SIZE ) {
				read = currentFile.read(buffer, totalRead, buffer.length - totalRead);
				totalRead += read;
			}
			currentFile.close();
			if (totalRead > 0) {
				String signature = byteArrayToHexString(md.digest(new String(buffer).getBytes()));
				return isInVirusDB(signature);
			}
			currentFile.close();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * Check all the subsequences of length SIGNATURE_SIZE
	 * for the virus signature.
	 * @param virus
	 * @return
	 */
	private boolean heavyCheckIfFileVirus(File virus) {
		MessageDigest md;

		try {
			md = MessageDigest.getInstance("SHA-1");

			int length = (int) virus.length();

			char[] buffer = new char[length];
//			Log.i(TAG, "Checking file " + virus.getName());
//			Log.i(TAG, "Length of file " + length);
			FileReader currentFile = new FileReader(virus);
			int totalRead =	 0;
			int read = 0;
			do {
				totalRead += read;
				read = currentFile.read(buffer, totalRead, length - totalRead);
			} while ( read > 0 );
			currentFile.close();
			if (totalRead > 0) {
				for (int i = 0; i < (length - SIGNATURE_SIZE); i++) {
//				for (int i = 0; i < 200; i++) {
					char[] tempBuff = new char[SIGNATURE_SIZE];
					System.arraycopy(buffer, i, tempBuff, 0, SIGNATURE_SIZE);
					String signature = new String(tempBuff);
					signature = byteArrayToHexString(md.digest(signature.getBytes()));
					if ( isInVirusDB(signature) )
						return true;
				}
			}
			currentFile.close();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}


	private boolean isInVirusDB(String signature) {
		for (int i = 0; i < signatureDB.length; i++) {
			if (signature.equals(signatureDB[i]))
				return true;
		}
		return false;
	}

	private void initSignatureDB(String pathToSignatures) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-1");
			File signatureFolder = new File(pathToSignatures);
			File[] demoViruses = signatureFolder.listFiles();

			signatureDB		= new String[demoViruses.length];
			char[] buffer 	= new char[SIGNATURE_SIZE];

			int i = 0;
			for (File virus : demoViruses) {

				FileReader signatureFile = new FileReader(virus);
				int totalRead =	 0;
				int read = 0;
				while ( totalRead != SIGNATURE_SIZE ) {
					read = signatureFile.read(buffer, totalRead, buffer.length - totalRead);
					totalRead += read;
				}
				if (totalRead > 0)
					signatureDB[i++] = byteArrayToHexString(md.digest(new String(buffer).getBytes()));
				signatureFile.close();
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			Log.e(TAG, "NoSuchAlgorithmException " + e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			Log.e(TAG, "Exception " + e.getMessage());
		}
	}

	private void initFolderToScan(ByteArrayOutputStream baos, String pathToFolder, int i) {
		try {
			long start = System.currentTimeMillis();

			zippedFolder = ZipHandler.zipFolder(baos, pathToFolder, i);

			Log.i(TAG, "Zip duration: " + (System.currentTimeMillis() - start));
			Log.i(TAG, "Zipped size of files: " + zippedFolder.length);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void initFolderToScan2() {
		FileInputStream in = null;
		
		try {
			
			Log.i(TAG, "Started folder compression");
			
			String zipFolderCmd = "busybox tar -czf " + 
						ControlMessages.VIRUS_FOLDER_ZIP + " " +
						ControlMessages.VIRUS_FOLDER_TO_SCAN;
			
			ControlMessages.executeShellCommand(TAG, zipFolderCmd, false);
			
			File f = new File(ControlMessages.VIRUS_FOLDER_ZIP);
			in = new FileInputStream(f);

			int length = (int) f.length();

			int read = 0;
			int totalRead = 0;
			zippedFolder = new byte[length];
			while (read != -1 && totalRead < length) {
				read = in.read(zippedFolder, totalRead, length - totalRead);
				totalRead += read;
				Log.i(TAG, "Read " + totalRead);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				in.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		Log.i(TAG, "Finished folder compression");
	}

	public static String byteArrayToHexString(byte[] b) {
		String result = "";
		for (int i=0; i < b.length; i++) {
			result +=
					Integer.toString( ( b[i] & 0xff ) + 0x100, 16).substring( 1 );
		}
		return result;
	}

	@Override
	public void copyState(Remoteable state) {

	}
	
	public void setNumberOfClones(int nrClones) {
		this.nrClones = nrClones;
	}
}
