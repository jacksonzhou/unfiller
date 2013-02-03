package edu.cmu.pocketsphinx.demo;

import java.util.Date;
import java.util.concurrent.Semaphore;

import android.os.Bundle;
import android.os.Vibrator;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

public class Listening extends Activity implements OnClickListener, RecognitionListener {
	static {
		System.loadLibrary("pocketsphinx_jni");
	}
	/**
	 * Recognizer task, which runs in a worker thread.
	 */
	RecognizerTask rec;
	/**
	 * Thread in which the recognizer task runs.
	 */
	Thread rec_thread;
	/**
	 * Time at which current recognition started.
	 */
	Date start_date;
	/**
	 * Number of seconds of speech.
	 */
	float speech_dur;
	/**
	 * Are we listening?
	 */
	boolean listening;
	/**
	 * Progress dialog for final recognition.
	 */
	ProgressDialog rec_dialog;
	/**
	 * Performance counter view.
	 */
	TextView performance_text;
	/**
	 * Editable text view.
	 */
	EditText edit_text;
	
	private final int WINDOW_SIZE = 20;
	private int startIndex = 0;
	
	long lastTime = 0;
	
	Vibrator v;

	
	/**
	 * Respond to touch events on the Speak button.
	 * 
	 * This allows the Speak button to function as a "push and hold" button, by
	 * triggering the start of recognition when it is first pushed, and the end
	 * of recognition when it is released.
	 * 
	 * @param v
	 *            View on which this event is called
	 * @param event
	 *            Event that was triggered.
	 */
	/*
	public boolean onTouch(View v, MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			start_date = new Date();
			this.listening = true;
			this.rec.start();
			break;
		case MotionEvent.ACTION_UP:
			Date end_date = new Date();
			long nmsec = end_date.getTime() - start_date.getTime();
			this.speech_dur = (float)nmsec / 1000;
			if (this.listening) {
				Log.d(getClass().getName(), "Showing Dialog");
				this.rec_dialog = ProgressDialog.show(PocketSphinxAndroidDemo.this, "", "Recognizing speech...", true);
				this.rec_dialog.setCancelable(false);
				this.listening = false;
			}
			this.rec.stop();
			break;
		default:
			;
		}
		 Let the button handle its own state 
		return false;
	}*/

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.listening);
		
		this.rec = new RecognizerTask();
		this.rec_thread = new Thread(this.rec);
		this.listening = true;

		this.rec.setRecognitionListener(this);
		this.rec_thread.start();
		
		//start record
		this.listening = true;
		this.rec.start();
		
		v = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
	}
	
	/*
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		this.rec = new RecognizerTask();
		this.rec_thread = new Thread(this.rec);
		this.listening = false;
		Button b1 = (Button) findViewById(R.id.Button01);
		Button b2 = (Button) findViewById(R.id.Button02);
		
		//b1.setOnClickListener(this);
		
		
		//b.setOnTouchListener(this);
		//this.performance_text = (TextView) findViewById(R.id.PerformanceText);
		//this.edit_text = (EditText) findViewById(R.id.EditText01);
		this.rec.setRecognitionListener(this);
		this.rec_thread.start();
	}*/

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.listening, menu);
		return true;
	}

	@Override
	public void onPartialResults(Bundle b) {
		/*
		if(System.currentTimeMillis() - lastTime < 500){
			//Log.e("skip", "skip");
			return;
		}
		lastTime = System.currentTimeMillis();*/
		// TODO Auto-generated method stub

		/*TextView b2 = (TextView) findViewById(R.id.listeningText);
		b2.setText(b.getString("hyp"));*/
		String rawResult = b.getString("hyp");
		
		int windowStartIndex = Math.max(startIndex, rawResult.length() - WINDOW_SIZE);
		
		
		String result = (windowStartIndex > rawResult.length()) ? rawResult.substring(rawResult.length() - WINDOW_SIZE) : rawResult.substring(windowStartIndex);
		
		Log.e("sphinx", result);
		
		
		if(hasBadWord(result)){
			//v.vibrate(300);

			this.startIndex = rawResult.length();
			
			FlashThread ft = new FlashThread(this);
			ft.start();
			
			/*this.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					LinearLayout button = (LinearLayout) findViewById(R.id.blah);
					Log.e("tag", "I should turn cyan now");
					button.setBackgroundColor(Color.CYAN);
					Vibrator vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
					vib.vibrate(300);
				}
				
			});
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			this.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					LinearLayout button = (LinearLayout) findViewById(R.id.blah);
					Log.e("tag", "I should turn back to black now");
					button.setBackgroundColor(Color.BLACK);
				}
				
			});*/
		}
	}
	
	private void resetListener(){
		stopListening();
		startListening();
	}
	
	private void stopListening(){
		this.listening = false;
		this.rec.stop();
	}
	
	private void startListening(){
		this.rec = new RecognizerTask();
		this.rec_thread = new Thread(this.rec);
		this.listening = true;

		this.rec.setRecognitionListener(this);
		this.rec_thread.start();
		
		//start record
		this.listening = true;
		this.rec.start();
	}

	@Override
	public void onResults(Bundle b) {
		// TODO Auto-generated method stub
		Log.e("sphinx", "onResults");
		
	}

	@Override
	public void onError(int err) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onClick(DialogInterface arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}
	
	public void stopListening(View v){
		Log.d(getClass().getName(), "stopped recording");
		
		this.listening = false;
		this.rec.stop();
		
		Intent intent = new Intent(this, PocketSphinxAndroidDemo.class);
		startActivity(intent);
	}
	
	/* if a "bad word" (tbd) is in this string, return true; else false */
	private boolean hasBadWord(String s){
		if(s.contains(" like")){
			return true;
		}
		return false;
	}

}

class FlashThread extends Thread{
	Activity activity;
	public FlashThread(Activity activity){
		this.activity = activity;
	}
	public void run(){
		activity.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				ImageButton button = (ImageButton) activity.findViewById(R.id.Button03);
				button.setBackgroundColor(Color.CYAN);
			}
			
		});
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		activity.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				ImageButton button = (ImageButton) activity.findViewById(R.id.Button03);
				button.setBackgroundColor(Color.BLACK);
			}
			
		});
	}
}