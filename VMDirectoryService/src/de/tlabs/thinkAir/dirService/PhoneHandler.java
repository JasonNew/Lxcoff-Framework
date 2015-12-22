package de.tlabs.thinkAir.dirService;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.NoRouteToHostException;
import java.net.Socket;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.UUID;
import java.util.Base64;

import org.jason.lxcoff.lib.Clone;
import org.jason.lxcoff.lib.Configuration;
import org.jason.lxcoff.lib.ControlMessages;
import org.jason.lxcoff.lib.ResultContainer;
import org.jason.lxcoff.lib.Clone.CloneState;
import org.jason.lxcoff.lib.profilers.NetworkProfiler;

public class PhoneHandler implements Runnable {

	//phone-client connect socket
	private Socket					clientSocket;
	private InputStream				is;
	private OutputStream			os;
	private ObjectOutputStream 		oos;
	private ObjectInputStream 		ois;
	
	//vm connect socket
	private Socket 					conSocket;
	private InputStream				conis;
	private OutputStream			conos;
	private ObjectOutputStream		conoos;
	private ObjectInputStream		conois;
	
	private String					phoneID;
	static 	String 					appName;						// the app name sent by the phone	
	static	String 					apkFilePath;					// the path where the apk is installed
	static 	String 					objToExecute = null;	// the object to be executed sent by the phone
	static 	String 					methodName;						// the method to be executed
	static 	Class<?>[] 				pTypes;							// the types of the parameters passed to the method
	static 	Object[] 				pValues;						// the values of the parameteres to be passed to the method
	
	private Clone					worker_clone = null;
	
	private final int 				BUFFER = 8192;
	
	private DBHelper				dbh;
	private static String			logFileName = null;
	private static FileWriter 		logFileWriter = null;
	
	private String 					RequestLog = null;

	public PhoneHandler(Socket clientSocket, InputStream is, OutputStream os, DBHelper dbh) {
		this.clientSocket 	= clientSocket;
		this.is				= is;
		this.os				= os;
		this.dbh			= dbh;
		this.logFileName = "/root/cloneroot/dirservice/ExecRecord/execrecord.txt"; 
		File needlog = new File("/root/cloneroot/dirservice/ExecRecord/needlog");
		if(needlog.exists()){
			try {
				File logFile = new File(logFileName);
				logFile.createNewFile(); // Try creating new, if doesn't exist
				logFileWriter = new FileWriter(logFile, true);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public void run() {

		System.out.println("Waiting for commands from the phone...");
		
		int command = 0;

		try{
			ois = new ObjectInputStream(is);
			oos = new ObjectOutputStream(os);
			long startTime , dura;

			while (command != -1) {

				command = is.read();
				System.out.println("Command: " + command);
				
				switch(command) {

				case ControlMessages.PHONE_AUTHENTICATION:

					// Read the ID of the requesting phone
					this.phoneID = (String)ois.readObject();
					System.out.println(this.phoneID);
					break;
					
				case ControlMessages.PING:
					System.out.println("Reply to PING");
					os.write(ControlMessages.PONG);
					break;
					
				case ControlMessages.APK_REGISTER:
					appName = (String) ois.readObject();
					apkFilePath = "/root/cloneroot/off-app/" + appName + ".apk";
					if (apkPresent(apkFilePath)) {
						System.out.println("APK present " + appName);
						os.write(ControlMessages.APK_PRESENT);
					} else {
						System.out.println("request APK " + appName);
						os.write(ControlMessages.APK_REQUEST);
						// Receive the apk file from the client
						receiveApk(ois, apkFilePath);
						System.out.println("received APK");
					}
					/*File dexFile = new File(apkFilePath);
					libraries = addLibraries(dexFile);
					objIn.addDex(dexFile);*/

					break;
					
				case ControlMessages.PHONE_COMPUTATION_REQUEST:	
					System.out.println("Execute request");
					
					boolean connected = false;
					
					startTime = System.nanoTime();
					if(this.worker_clone == null || this.worker_clone.getStatus()!= CloneState.AUTHENTICATED){
						do{
							this.worker_clone = findAvailableClone();
		
							if (this.worker_clone != null) {
								System.out.println("Found an authenticated/starting clone.");
								connected = waitForCloneToAuthenticate(this.worker_clone);
							}
							else {
								System.err.println("There is no available clone running, should start one.");
								this.worker_clone = startNewClone();
								
								if (this.worker_clone == null) {
									System.err.println("Could not find an available clone, neither started, nor stopped.");
									break;
								}else{
									//while starting the clone,we should wait for clone to connect
									connected = waitForCloneToAuthenticate(this.worker_clone);
								}
							}
						}while(!connected);
					}else{
						do{
							connected = waitForCloneToAuthenticate(this.worker_clone);
						}while(!connected);
					}
					dura = System.nanoTime() - startTime;
					
					//资源准备
					this.RequestLog += dura/1000000 + " ";
					
					startTime = System.nanoTime();
					HashMap<String, String> result = (HashMap<String, String>) receiveAndRepost(ois, this.worker_clone);
					
					releaseClone(this.worker_clone);
					try {
						// Send back over the socket connection
						System.out.println("Send result back");
						this.oos.writeObject(result.get("retType"));
						this.oos.writeObject(result.get("retVal"));
						// Clear ObjectOutputCache - Java caching unsuitable
						// in this case
						this.oos.flush();
						//this.oos.reset();

						System.out.println("Result successfully sent");
					} catch (IOException e) {
						System.out.println("Connection failed when sending result back");
						e.printStackTrace();
						return;
					}
					
					dura = System.nanoTime()-startTime;
					//请求执行 + apk传输
					this.RequestLog += dura/1000000;
					this.traceLog(this.RequestLog);
					this.RequestLog = "";

					break;
					
				case ControlMessages.SEND_FILE_FIRST:
					startTime = System.nanoTime();
					System.out.println("The offloading need to send file first");
					String filePath = (String) ois.readObject();
					String fileName = filePath.substring(filePath.lastIndexOf("/")+1);
					filePath = "/root/cloneroot/off-file/" + fileName;
					if (apkPresent(filePath)) {
						System.out.println("File present " + filePath);
						os.write(ControlMessages.FILE_PRESENT);
					} else {
						System.out.println("request File " + filePath);
						os.write(ControlMessages.SEND_FILE_REQUEST);
						// Receive the apk file from the client
						receiveApk(ois, filePath);
					}
					
					//We need to push this into the phone now.
					System.out.println("Trying to push the file to the VM phone.");
					String out = executeCommand("adb push " + filePath + " /system/off-app/off-file/" );
					dura = System.nanoTime() - startTime;
					
					//send file时间
					this.RequestLog += dura / 1000000 + " ";
					break;
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e){
			e.printStackTrace();
		} finally {
			try {
				oos.close();
				ois.close();
				clientSocket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private boolean apkPresent(String filename) {
		// return false;
		// TODO: more sophisticated checking for existence
		File apkFile = new File(filename);
		return apkFile.exists();
	}
	
	/**
	 * Method to retrieve an apk of an application that needs to be executed
	 * 
	 * @param objIn
	 *            Object input stream to simplify retrieval of data
	 * @return the file where the apk package is stored
	 * @throws IOException
	 *             throw up an exception thrown if socket fails
	 */
	private File receiveApk(ObjectInputStream objIn, String apkFilePath)
			throws IOException {
		// Receiving the apk file
		// Get the length of the file receiving
		int apkLen = objIn.readInt();
		System.out.println("Read apk len - " + apkLen);

		// Get the apk file
		byte[] tempArray = new byte[apkLen];
		System.out.println("Read apk");
		objIn.readFully(tempArray);

		// Write it to the filesystem
		File dexFile = new File(apkFilePath);
		FileOutputStream fout = new FileOutputStream(dexFile);

		BufferedOutputStream bout = new BufferedOutputStream(fout, BUFFER);
		bout.write(tempArray);
		bout.close();
		
		

		return dexFile;
	}
	
	private void sendApk(String apkName, ObjectOutputStream objOut)
			throws IOException {
		File apkFile = new File(apkName);
		FileInputStream fin = new FileInputStream(apkFile);
		BufferedInputStream bis = new BufferedInputStream(fin);
		byte[] tempArray = new byte[(int) apkFile.length()];
		bis.read(tempArray, 0, tempArray.length);
		// Send file length first
		System.out.println("Sending apk length - " + tempArray.length);
		objOut.writeInt(tempArray.length);
		// Send the file
		System.out.println("Sending apk");
		objOut.write(tempArray);
		objOut.flush();
		bis.close();
	}
	
	/**
	 * Reads in the object to execute an operation on, name of the method to be
	 * executed and repost it
	 */
	private Object receiveAndRepost(ObjectInputStream objIn,
			Clone clone) {
		// Read the object in for execution
		System.out.println("Read Object");
		try {

			// Get the object
			String className = (String) objIn.readObject();
			
			objToExecute = (String) objIn.readObject();

			System.out.println("Read Method");
			// Read the name of the method to be executed
			methodName = (String) objIn.readObject();
			
			Object tempTypes = objIn.readObject();
			String[] pType = (String[]) tempTypes;
			
			String pValuestr = (String) objIn.readObject();

			System.out.println("Repost Method " + methodName);
			
			long starttime = System.nanoTime();
			
			this.conos.write(ControlMessages.PHONE_COMPUTATION_REQUEST);
			
			this.conoos.writeObject(appName);
			
			//see if the runtime needs apk. In container case, this will always be no need.
			int needApk = this.conis.read();
			if(needApk == ControlMessages.APK_REQUEST){
				sendApk(apkFilePath, this.conoos);
			}
			
//			this.conoos.reset();
			this.conoos.writeObject(className);
			
			this.conoos.writeObject(objToExecute);

			// Send the method to be executed
			// Log.d(TAG, "Write Method - " + m.getName());
			this.conoos.writeObject(methodName);

			// Log.d(TAG, "Write method parameter types");
			//this.conoos.writeObject(pTypes);
			this.conoos.writeObject(pType);

			// Log.d(TAG, "Write method parameter values");
			//this.conoos.writeObject(pValues);
			this.conoos.writeObject(pValuestr);
			this.conoos.flush();
			
			//waiting to retrieve result from container
			System.out.println("Reading result from container");
			
			HashMap<String ,String> result = new HashMap<String ,String>(); 
			String retType = (String) this.conois.readObject();
			result.put("retType", retType);
			String response = (String) this.conois.readObject();
			result.put("retVal", response);
			
			long dura = System.nanoTime() - starttime;
			this.RequestLog += " " + dura /1000000; 
			
			return result;
		

		} catch (Exception e) {
			// catch and return any exception since we do not know how to handle
			// them on the server side
			e.printStackTrace();
			//return new ResultContainer(null, e, null);
			return null;
		}

	}
	
	private boolean waitForCloneToAuthenticate(Clone c) {
		Long stime = System.nanoTime();
		while(true){
			try{
				this.conSocket  = new Socket();
				System.out.println("Connecting worker clone ...");
				conSocket.connect(new InetSocketAddress(c.getIp(), c.getPortForDir()), 0);
				
				Long dura = (System.nanoTime()-stime);
				System.out.println("Connected worker clone in " + dura/1000000 + "ms");
				
				this.worker_clone.setStatus(CloneState.ASSIGNED_TO_PHONE);
				this.conis = this.conSocket.getInputStream();
				this.conos = this.conSocket.getOutputStream();
				this.conois = new ObjectInputStream(this.conis);
				this.conoos = new ObjectOutputStream(this.conos);
				
				return true;
			}catch(ConnectException e){
				try {
					Thread.sleep(100);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}catch(NoRouteToHostException e){
				try {
					Thread.sleep(100);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}catch(IOException e){
				e.printStackTrace();
				try {
					this.conSocket.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				return false;
			}
		}
		
	}

	private void releaseClone(Clone clone){
		try{
			this.conoos.close();
			this.conois.close();
			this.conos.close();
			this.conis.close();
		}catch(IOException e){
			e.printStackTrace();
		}
		
		clone.setStatus(CloneState.AUTHENTICATED);
		
	}
	
	private synchronized Clone findAvailableClone() {
		Clone bestc = null;

/*		for (Clone c : DirectoryService.amazonClones)
			if (c.getStatus() == CloneState.AUTHENTICATED)
				return c;*/
		
		for (Clone c : DirectoryService.vbClones){
			if (c.getStatus() == CloneState.AUTHENTICATED)
				return c;
		}
		
		return null;
	}
	
	/**
	 * Start the first available clone scanning the amazon clones first and virtualbox clones second.
	 * @return
	 */
	private synchronized Clone startNewClone() {

/*		for (Clone c : DirectoryService.amazonClones) {
			if (c.getStatus() != CloneState.ASSIGNED_TO_PHONE) {
				c.prepareClone();
				return c;
			}
		}*/
		for (Clone c : DirectoryService.vbClones) {
			if (c.getStatus() == CloneState.STOPPED || c.getStatus() == CloneState.PAUSED) {
				c.prepareClone();
				return c;
			}
		}
		return null;
	}
	
	private void traceLog(String log){
		if (logFileWriter != null) {
			try {
				logFileWriter.append(log + "\n");
				logFileWriter.flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private String executeCommand(String command) {
		try {
			Process p = Runtime.getRuntime().exec(command);
			// you can pass the system command or a script to exec command.
			BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));

			BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));

			// read the output from the command
			StringBuilder sb = new StringBuilder();
			String s = "";
			while ((s = stdInput.readLine()) != null) {
//				System.out.println("Std OUT: "+s);
				sb.append(s);
			}

			while ((s = stdError.readLine()) != null) {
				System.out.println("Std ERROR : "+s);
			}

			return sb.toString();

		} catch (IOException e) {

			e.printStackTrace();
		}
		return null;
	}
	

}
