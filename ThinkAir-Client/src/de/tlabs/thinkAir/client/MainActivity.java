package de.tlabs.thinkAir.client;

import de.tlabs.thinkAir.lib.ControlMessages;
import de.tlabs.thinkAir.lib.ExecutionController;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.provider.Settings.Secure;

public class MainActivity extends Activity {
	
	public static boolean START_AS_NEW_PHONE = true;
	private static final String TAG = "MainActivity";

	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
	}
	
	public void startConnect(View v) {
		START_AS_NEW_PHONE = false;
		Intent intent = new Intent(v.getContext(), StartExecution.class);
		ExecutionController.myId = Secure.getString(v.getContext().getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);

		startActivity(intent);
	}

}