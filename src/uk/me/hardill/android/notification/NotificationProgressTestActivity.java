package uk.me.hardill.android.notification;

import java.io.File;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

public class NotificationProgressTestActivity extends Activity {
	/** Called when the activity is first created. */
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
	}
	
	public void button(View view) {
		Intent intent = new Intent(this, UploadService.class);
		Uri uri = Uri.fromFile(new File("/sdcard/MyTracks/tcx/PPP recy.tcx"));
		Log.i("FOO", uri.toString());
		
		intent.setData(uri);
		startService(intent);
	}
}	