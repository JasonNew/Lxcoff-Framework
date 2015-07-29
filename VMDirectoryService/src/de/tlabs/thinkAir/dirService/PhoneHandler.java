package de.tlabs.thinkAir.dirService;

import java.io.BufferedOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
	
	private String					phoneID;
	static 	String 					appName;						// the app name sent by the phone	
	static	String 					apkFilePath;					// the path where the apk is installed
	static 	String 					objToExecute = null;	// the object to be executed sent by the phone
	static 	String 					methodName;						// the method to be executed
	static 	Class<?>[] 				pTypes;							// the types of the parameters passed to the method
	static 	Object[] 				pValues;						// the values of the parameteres to be passed to the method
	
	private Container				worker_container = null;
	private Clone					worker_clone = null;
	
	private final int 				BUFFER = 8192;
	
	private DBHelper				dbh;

	public PhoneHandler(Socket clientSocket, InputStream is, OutputStream os, DBHelper dbh) {
		this.clientSocket 	= clientSocket;
		this.is				= is;
		this.os				= os;
		this.dbh			= dbh;
	}

	@Override
	public void run() {

		System.out.println("Waiting for commands from the phone...");

		int command = 0;

		try{
			ois = new ObjectInputStream(is);
			oos = new ObjectOutputStream(os);

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
					apkFilePath = ControlMessages.DIRSERVICE_APK_DIR + appName + ".apk";
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
					
					if(this.worker_clone == null || this.worker_clone.getStatus()!= CloneState.AUTHENTICATED){
						Clone c = null;
						
						c = findAvailableClone();
	
						if (c != null) {
							System.out.println("Found an authenticated/starting clone.");
						}
						else {
							//Should not go in here for now.
							System.err.println("There is no available clone running, should start one.");
							c = startNewClone();
							if (c == null) {
								System.err.println("Could not find an available clone, neither started, nor stopped.");
								break;
							}
							c.prepareClone();
						}
						c.setStatus(CloneState.ASSIGNED_TO_PHONE);
						
						//receive the object from phone-client ois and repost the request to container
						this.worker_clone = c;
					}else{
						
						this.worker_clone.setStatus(CloneState.ASSIGNED_TO_PHONE);
					}
					
					HashMap<String, String> result = (HashMap<String, String>) receiveAndRepost(ois, this.worker_clone);
					
					releaseClone(this.worker_clone);
					
/*					//just for testing on windows with virtualbox
					waitForCloneToAuthenticate();
					
					this.worker_container = null;

					
					System.out.println("Start to receive and repost");
					Object result = receiveAndRepost(ois, this.worker_container);*/

					try {
						// Send back over the socket connection
						System.out.println("Send result back");
						this.oos.writeObject(result.get("retType"));
						this.oos.writeObject(result.get("retVal"));
						// Clear ObjectOutputCache - Java caching unsuitable
						// in this case
						this.oos.flush();
						this.oos.reset();

						System.out.println("Result successfully sent");
					} catch (IOException e) {
						System.out.println("Connection failed when sending result back");
						e.printStackTrace();
						return;
					}

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
			
			clone.conos.write(ControlMessages.PHONE_COMPUTATION_REQUEST);
			
			clone.conoos.writeObject(appName);
			
//			this.conoos.reset();
			clone.conoos.writeObject(className);
			
			clone.conoos.writeObject(objToExecute);

			// Send the method to be executed
			// Log.d(TAG, "Write Method - " + m.getName());
			clone.conoos.writeObject(methodName);

			// Log.d(TAG, "Write method parameter types");
			//this.conoos.writeObject(pTypes);
			clone.conoos.writeObject(pType);

			// Log.d(TAG, "Write method parameter values");
			//this.conoos.writeObject(pValues);
			clone.conoos.writeObject(pValuestr);
			clone.conoos.flush();
			
			//waiting to retrieve result from container
			System.out.println("Reading result from container");
			
			HashMap<String ,String> result = new HashMap<String ,String>(); 
			String retType = (String) clone.conois.readObject();
			result.put("retType", retType);
			String response = (String) clone.conois.readObject();
			result.put("retVal", response);
			
			return result;
		

		} catch (Exception e) {
			// catch and return any exception since we do not know how to handle
			// them on the server side
			e.printStackTrace();
			//return new ResultContainer(null, e, null);
			return null;
		}

	}
	
	/*private boolean waitForContainerToAuthenticate(Container container) {
		Long stime = System.nanoTime();
		while(true){
			try{
				this.conSocket  = new Socket();
				System.out.println("Connecting worker container ...");
				conSocket.connect(new InetSocketAddress(container.getIp(), container.getPortForDir()), 0);
				
				Long dura = (System.nanoTime()-stime);
				System.out.println("Connected worker container in " + dura/1000000 + "ms");
				
				this.worker_container.setStatus(ContainerState.ASSIGNED_TO_PHONE);
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

	private Container findAvailableContainer() {
		try{
			dbh.dbUpdate("lock tables lxc write"); 
			String sql = "select * from lxc where status in (2,1,0) order by status desc limit 1 ";
			ResultSet rs = dbh.dbSelect(sql);
			
			if(rs.next()){
				String name = rs.getString("name");
				String ip = rs.getString("ip");
				int status = rs.getInt("status");
				int mem = rs.getInt("mem");
				String cpuset = rs.getString("cpuset");
				int cpushare = rs.getInt("cpushare");
				//only update status in database so that nobody will choose this lxc anymore, 
				//while the container object has old status for prepareContainer deciding what operation to do
				dbh.dbUpdate("update lxc set status = " + ContainerState.ASSIGNING + " where name = '" + name + "'"); 
				dbh.dbUpdate("unlock tables");
				
				if(ip == null){
					ip = getAvailableIp();
					dbh.dbUpdate("update lxc set ip = '" + ip + "' where name = '" + name + "'");
				}
				
				Container con = new Container(name, ip, status, mem, cpuset, cpushare);
				return con;
			}else{
				dbh.dbUpdate("unlock tables");
				Container con = startNewContainer();
				return con;
				
			}
		} catch (SQLException e) {
            e.printStackTrace();
        } 
		return null;
		
	}*/
	
	/**
	 * Start the first available clone scanning the amazon clones first and virtualbox clones second.
	 * @return
	 */
	/*private Container startNewContainer() {
		try{
			//find available ip for new container
			String name = UUID.randomUUID().toString();
			String ip = getAvailableIp();
			if(ip == null)
				return null;
			
			Container con = new Container(name, ip, ContainerState.CREATING);
			
			String sql = "insert into lxc (name, ip, status) values ('" + name + "','" + ip + "', " + ContainerState.CREATING +")";

			boolean rs = dbh.dbUpdate(sql);
			
			if(rs){
				return con;
			}
			
		} catch (Exception e) {
            e.printStackTrace();
        } 
		
		return null;

	}
	
	private String getAvailableIp(){
		String ip = null;
		try{
			//find available ip for container
			dbh.dbUpdate("lock tables ip write"); 
			ResultSet rs = dbh.dbSelect("select ip from ip where inuse=0 limit 1");
			if(rs.next()){
				ip = rs.getString("ip");
			}
			
			if(ip != null){
				dbh.dbUpdate("update ip set inuse=1 where ip='" + ip + "'");
			}
			
			dbh.dbUpdate("unlock tables");
			return ip;
		} catch (SQLException e) {
            e.printStackTrace();
        } 
		
		return null;
		
	}*/
	
	private void releaseClone(Clone clone){
		
		clone.setStatus(CloneState.AUTHENTICATED);
		
	}
	
	private synchronized Clone findAvailableClone() {
		Clone bestc = null;

		for (Clone c : DirectoryService.amazonClones)
			if (c.getStatus() == CloneState.AUTHENTICATED)
				return c;
		
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

		for (Clone c : DirectoryService.amazonClones) {
			if (c.getStatus() != CloneState.ASSIGNED_TO_PHONE) {
				c.prepareClone();
				return c;
			}
		}
		for (Clone c : DirectoryService.vbClones) {
			if (c.getStatus() == CloneState.STOPPED || c.getStatus() == CloneState.PAUSED) {
				c.prepareClone();
				return c;
			}
		}
		return null;
	}
	

}
