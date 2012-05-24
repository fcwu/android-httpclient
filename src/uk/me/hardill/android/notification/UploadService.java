package uk.me.hardill.android.notification;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.DefaultHttpClient;

import uk.me.hardill.upload.CountingInputStreamEntity;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.widget.RemoteViews;

public class UploadService extends IntentService{
	
	private NotificationManager notificationManager;
	private Notification notification;

	public UploadService(String name) {
		super(name);
	}
	
	public UploadService(){
		super("UploadService");
	}


	@Override
	protected void onHandleIntent(Intent intent) {
		notificationManager = (NotificationManager) getApplicationContext().getSystemService(getApplicationContext().NOTIFICATION_SERVICE);
		Uri uri  = intent.getData();
		Log.i("FOO", uri.toString());
		Thread t = new Thread(new BackgroundThread(this, uri));
		t.start();
	}

	private class BackgroundThread implements Runnable, CountingInputStreamEntity.UploadListener {
		
		Context context;
		Uri uri;
		int lastPercent = 0;
		
		public BackgroundThread(Context context, Uri uri) {
			this.context = context;
			this.uri = uri;
		}
		
		@Override
		public void run() {
			
			Intent notificationIntent = new Intent();
			PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);


			notification = new Notification(R.drawable.icon,
					"Uploading file", System.currentTimeMillis());
			notification.flags = notification.flags
					| Notification.FLAG_ONGOING_EVENT;
			notification.contentView = new RemoteViews(getApplicationContext()
					.getPackageName(), R.layout.upload_progress_bar);
			notification.contentIntent = contentIntent;
			notification.contentView.setProgressBar(R.id.progressBar1, 100,0, false);
			
			notificationManager.notify(1, notification);
			
			Log.i("FOO", "Notification started");
			
			final HttpResponse resp;
			final HttpClient httpClient = new DefaultHttpClient();
			
			HttpPut put = new HttpPut("http://10.0.2.2:8080/FileUpload/FileConsumer");
			
			try {
				ParcelFileDescriptor fileDescriptor = context.getContentResolver().openFileDescriptor(uri, "r");
				InputStream in = context.getContentResolver().openInputStream(uri);
				CountingInputStreamEntity entity = new CountingInputStreamEntity(in, fileDescriptor.getStatSize());
				entity.setUploadListener(this);
				entity.setContentType("application/gpx+xml");
				put.setEntity(entity);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				Log.i("FOO", "About to call httpClient.execute");
				resp = httpClient.execute(put);
				if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
					notification.setLatestEventInfo(context, "Uploading Workout", "All Done", contentIntent);
					notification.flags |= Notification.FLAG_AUTO_CANCEL;
					notificationManager.notify(1, notification);
					Log.i("FOO", "All done");
				} else {
					Log.i("FOO", "Screw up with http - " + resp.getStatusLine().getStatusCode());
				}
				resp.getEntity().consumeContent();
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		@Override
		public void onChange(int percent) {
			if(percent > lastPercent) {
				notification.contentView.setProgressBar(R.id.progressBar1, 100, percent, false);
				notificationManager.notify(1, notification);
				lastPercent = percent;
			}
		}
		
	}

}
