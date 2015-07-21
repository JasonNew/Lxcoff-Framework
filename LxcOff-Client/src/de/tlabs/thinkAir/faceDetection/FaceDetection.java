package de.tlabs.thinkAir.faceDetection;

import java.lang.reflect.Method;
import java.nio.ByteBuffer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.media.FaceDetector;
import android.util.Log;
import org.jason.lxcoff.lib.ControlMessages;
import org.jason.lxcoff.lib.ExecutionController;
import org.jason.lxcoff.lib.Remote;
import org.jason.lxcoff.lib.Remoteable;

public class FaceDetection extends Remoteable {

	private static final String	TAG = "FaceDetection";
	private static final long 	serialVersionUID = 3576725998229711688L;

	private static final int				NUM_FACES	= 64; // max is 64
	private static final boolean			DEBUG		= true;

	transient private Bitmap 				sourceImage;
	transient private ExecutionController 	controller;
	private int								nrClones;

	private byte[] 	bytesar;
	private int 	widthImage;
	private int 	heightImage;
	private int 	rowBytes;
	private int 	dstCapacity;

	public FaceDetection(ExecutionController controller, int nrClones) {
		this.controller = controller;
		this.nrClones	= nrClones;
	}

	public FaceDetection(ExecutionController controller) {
		this.controller = controller;
	}

	/**
	 * Compare an image against a bunch of photos and check if this photo is a duplicate
	 * of one of the photos in the folder.
	 * 
	 * @param imageToCheck The path of the image to be compared with the other photos 
	 * @param numPhotos	The number of photos to be considered for comparison
	 * @return
	 */
	public int detectFaces(String imageToCheck, int numPhotos)
	{
		// Uncomment this in case you want to send the picture to the server
		try {
			getImage(imageToCheck);
		} catch (NullPointerException e) {
			Log.e(TAG, "Error while reading the image");
			return 0;
		}

		Method		toExecute;
		Class<?>[]	paramTypes 	= {String.class, int.class};
		Object[]	paramValues = {imageToCheck, numPhotos};

		int result = -1;
		try {
			toExecute = this.getClass().getDeclaredMethod("localDetectFaces", paramTypes);
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

	/**
	 * Read the image and put it in a byteArray for off-loading
	 * since bitmaps are not serializable.
	 * @param imageToCheck
	 */
	private void getImage(String imageToCheck) throws NullPointerException
	{
		BitmapFactory.Options bfo = new BitmapFactory.Options();
		bfo.inPreferredConfig = Bitmap.Config.RGB_565;

		sourceImage = BitmapFactory.decodeFile(imageToCheck, bfo);

		rowBytes = sourceImage.getRowBytes();
		heightImage = sourceImage.getHeight();
		widthImage = sourceImage.getWidth();

		int bmSize = sourceImage.getRowBytes() * sourceImage.getHeight();

		ByteBuffer dst = ByteBuffer.allocate(bmSize);

		dstCapacity = dst.capacity();

		dst.position(0);

		sourceImage.copyPixelsToBuffer(dst);

		bytesar = new byte[bmSize];

		Log.d(TAG, "Size of the image to send: " + dstCapacity);

		dst.position(0);
		dst.get(bytesar);

		sourceImage.recycle();
	}

	@Remote
	public int localDetectFaces(String imageToCheck, int numPhotos)
	{
		boolean isOffloaded = ControlMessages.checkIfOffloaded();

		FaceDetector.Face getAllFaces[] = new FaceDetector.Face[NUM_FACES];
		FaceDetector arrayFaces;

		Bitmap cloneImage = null;
		if (isOffloaded) {
			ByteBuffer dst = ByteBuffer.allocate(dstCapacity);
			dst.position(0);
			dst.put(bytesar);

			dst.position(0);
			cloneImage = Bitmap.createBitmap(widthImage, heightImage, Bitmap.Config.RGB_565);
			cloneImage.copyPixelsFromBuffer(dst);
		}
		else {
			BitmapFactory.Options bfo = new BitmapFactory.Options();
			bfo.inPreferredConfig = Bitmap.Config.RGB_565;
			cloneImage = BitmapFactory.decodeFile(imageToCheck, bfo);
		}

		int picWidth = cloneImage.getWidth();
		int picHeight = cloneImage.getHeight();

		Log.d(TAG, "File exists. Width=" + picWidth + " Height=" + picHeight);

		arrayFaces = new FaceDetector( picWidth, picHeight, NUM_FACES );
		int numberOfFacesFound = arrayFaces.findFaces(cloneImage, getAllFaces);

		if(numPhotos == 1) {
			return numberOfFacesFound;
		}
		else
		{
			int nrDuplicates = findDuplicates(numPhotos, numberOfFacesFound, getAllFaces, isOffloaded); 
			Log.d(TAG, "Finished Face Detection. Duplicates found: " + nrDuplicates);
			return nrDuplicates;
		}	
	}

	/**
	 * When having more than one clone running the method there will be partial results
	 * which should be combined to get the total result.
	 * This will be done automatically by the main clone by calling
	 * this method.
	 * @param params Array of partial results.
	 * @return The total result.
	 */
	public int localDetectFacesReduce(int[] params) {
		int nrDuplicates = 0;
		for (int i = 0; i < params.length; i++) {
			nrDuplicates += params[i];
		}
		return nrDuplicates;
	}

	private int findDuplicates(int numPhotos, int numberOfFacesFound,
			FaceDetector.Face[] allFaces, boolean isOffloaded)
	{
		int nrDuplicates 	= 0;

		int cloneId 		= ControlMessages.readCloneId();
		int howManyPhotos 	= (int) (numPhotos / nrClones);		// Integer division, we may loose some photos.
		int start 			= (cloneId + 1) * howManyPhotos;
		int end 			= start + howManyPhotos;

		// If this is the clone with the highest id let him take care
		// of the photos not considered due to the integer division. 
		if (cloneId == nrClones-2)
			end += numPhotos % nrClones;

		String pathToFolder = (isOffloaded == true) ? ControlMessages.FACE_PICTURE_FOLDER_CLONE : 
			ControlMessages.FACE_PICTURE_FOLDER;
		Log.i(TAG, "Checking photos: " + start + "-" + end + " in folder: " + pathToFolder);

		for(int name = start; name < end; name++)
		{
			int facesDuplicate 			= 0;
			FaceDetector.Face getFace 	= null;
			PointF eyesMidPts[] 		= new PointF[NUM_FACES];
			float  eyesDistance[] 		= new float[NUM_FACES];

			FaceDetector.Face getFaceDuplicate = null;
			FaceDetector.Face getAllFacesDuplicate[] = new FaceDetector.Face[NUM_FACES];
			FaceDetector arrayFacesDuplicate;
			PointF eyesMidPtsDuplicate[] 	= new PointF[NUM_FACES];
			float  eyesDistanceDuplicate[] 	= new float[NUM_FACES];

			BitmapFactory.Options bfo = new BitmapFactory.Options();
			bfo.inPreferredConfig = Bitmap.Config.RGB_565;

			Bitmap picDuplicate = BitmapFactory.decodeFile(pathToFolder + name + ".jpg", bfo);

			if (picDuplicate == null) {
				Log.d(TAG, "File doesn't exist: " + name + ".jpg");
				continue;
			}

			int picWidth 	= picDuplicate.getWidth();
			int picHeight 	= picDuplicate.getHeight();
			Log.d(TAG, "Processing file: " + name + ".jpg" + 
					" Width=" + picWidth + " Height=" + picHeight);

			arrayFacesDuplicate = new FaceDetector( picWidth, picHeight, NUM_FACES );
			int numberOfFacesDuplicate = arrayFacesDuplicate.findFaces(picDuplicate, getAllFacesDuplicate);

			// Compare the number of faces found: if equal proceed and check if the pictures are equal
			if(numberOfFacesDuplicate == numberOfFacesFound)
			{
				Log.d("Face", "Maybe found duplicate, number of faces (" + numberOfFacesFound + ") is equal.");
				for (int i = 0; i < numberOfFacesFound; i++)
				{
					// Compare the faces of the test photo with the faces found in the current photo
					getFace = allFaces[i];
					for (int j = 0; j < numberOfFacesDuplicate; j++) {
						getFaceDuplicate = getAllFacesDuplicate[j];
						try 
						{
							// Get the eyes metrics: eyes distance and the middle point
							PointF eyesMP = new PointF();
							getFace.getMidPoint(eyesMP);
							eyesDistance[i] = getFace.eyesDistance();
							eyesMidPts[i] = eyesMP;

							PointF eyesMPDuplicate = new PointF();
							getFaceDuplicate.getMidPoint(eyesMPDuplicate);
							eyesDistanceDuplicate[i] = getFaceDuplicate.eyesDistance();
							eyesMidPtsDuplicate[i] = eyesMPDuplicate;

							// If the eyes distance and the middle point are equal then the face maybe is duplicate
							if(eyesDistanceDuplicate[i] == eyesDistance[i] && 
									eyesMidPts[i].x == eyesMidPtsDuplicate[i].x &&
									eyesMidPts[i].y == eyesMidPtsDuplicate[i].y) {

								facesDuplicate++;
							}
						}
						catch (Exception e)
						{
							if (DEBUG) Log.e("Face", i + " is null");
						}
					}
				}
			}

			picDuplicate.recycle();
			eyesMidPts = null;
			eyesDistance = null;
			getAllFacesDuplicate = null;
			eyesMidPtsDuplicate = null;
			eyesDistanceDuplicate = null;
			arrayFacesDuplicate = null;

			// If all the faces found were duplicated in the current photo
			// then we can say that the photo is a duplicate with high probability
			if(facesDuplicate == numberOfFacesFound) {
				nrDuplicates++;
				Log.i(TAG, "Number of duplications so far: " + nrDuplicates);
			}
		}

		return nrDuplicates;
	}

	@Override
	public void copyState(Remoteable state) {

	}

	public void setNumberOfClones(int nrClones) {
		this.nrClones = nrClones;
	}

}
