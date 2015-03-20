package tv.matchstick.jnitest;

import tv.matchstick.Flint;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity implements Flint.Callback {
	private static final String LOG_TAG = "Flint";

	public Button startBt;
	public Button stopBt;
	public Button statusBt;

	private Flint flint;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		startBt = (Button) findViewById(R.id.start);
		stopBt = (Button) findViewById(R.id.stop);
		statusBt = (Button) findViewById(R.id.getstatus);

		flint = new Flint(this);
		flint.setCallback(this);

		startBt.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (flint != null && !flint.isRunning()) {
					new Thread(new Runnable() {
						public void run() {
							flint.start();
							int e = flint.getErrorCode();
							Log.d(LOG_TAG, "Flint start error:" + e);
						}
					}).start();
				}
			}
		});

		stopBt.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (flint != null && flint.isRunning()) {
					flint.stop();
				}
			}
		});

		statusBt.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (flint != null) {
					Toast.makeText(
							getApplicationContext(),
							"FlintDaemon is "
									+ (flint.isRunning() ? "running"
											: "not running"),
							Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(getApplicationContext(),
							"FlintDaemon is not running", Toast.LENGTH_SHORT)
							.show();
				}
			}
		});
	}

	protected void onDestroy() {
		super.onDestroy();
		if (flint != null && flint.isRunning()) {
			flint.stop();
		}
		flint = null;
	}

	@Override
	public void onWebAppStart(String appInfo) {
		// TODO Auto-generated method stub
		Log.d(LOG_TAG, "start web app: " + appInfo);
	}

	@Override
	public void onNativeAppStart(String appInfo) {
		// TODO Auto-generated method stub
		Log.d(LOG_TAG, "start native app: " + appInfo);
	}

	@Override
	public void onWebAppStop(String appInfo) {
		// TODO Auto-generated method stub
		Log.d(LOG_TAG, "stop web app: " + appInfo);
	}

	@Override
	public void onNativeAppStop(String appInfo) {
		// TODO Auto-generated method stub
		Log.d(LOG_TAG, "stop native app: " + appInfo);
	}
}
