package de.tlabs.thinkAir.synthBenchmark;

import java.lang.reflect.Method;

import org.jason.lxcoff.lib.ExecutionController;
import org.jason.lxcoff.lib.Remoteable;

public class JniTest extends Remoteable {

    /**
     * 
     */
    private static final long serialVersionUID = 7407706990063388777L;

    private transient ExecutionController controller;

    public int temp = 0;

    public JniTest(ExecutionController controller) {
        this.controller = controller;
    }

    /*
     * A native method that is implemented by the 'hello-jni' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();

    /*
     * this is used to load the 'hello-jni' library on application startup. The
     * library has already been unpacked into
     * /data/data/de.tlabs.mobileCloud.client.StartExecution/lib/libhello-jni.so
     * at installation time by the package manager.
     */
    static {

        try {
            System.load("/data/data/de.tlabs.mobileCloud.server/files/libhello-jni.so");
        } catch (UnsatisfiedLinkError e) {
            // Could not load it from server files, try client
            System.load("/data/data/de.tlabs.mobileCloud.client/lib/libhello-jni.so");
        }

    }

    public String jniCaller() {
        Method toExecute;
        String result = null;
        try {
            toExecute = this.getClass().getDeclaredMethod("localjniCaller", (Class[]) null);
            result = (String) controller.execute(toExecute, this);
            //Log.d("PowerDroid-Client", "Value of temp - " + temp);
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

    public String localjniCaller() {
        return stringFromJNI();
    }

    @Override
    public void copyState(Remoteable arg0) {
        // TODO Auto-generated method stub

    }

}
