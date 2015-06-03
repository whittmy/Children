package children.lemoon;

import android.app.Application;
import android.content.Context;

public class MyApp extends Application {
	public static Context mContext;
	private static MyApp instance;

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();

		instance = this;
		mContext = getApplicationContext();
	}

	public static MyApp getInstance() {
		return instance;
	}
}
