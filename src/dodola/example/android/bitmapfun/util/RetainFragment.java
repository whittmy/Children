package dodola.example.android.bitmapfun.util;

//ok
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

public class RetainFragment extends Fragment {
	private static final String TAG = "RetainFragment";
	private Object mObject;

	public static RetainFragment findOrCreateRetainFragment(FragmentManager paramFragmentManager) {
		RetainFragment localRetainFragment = (RetainFragment) paramFragmentManager.findFragmentByTag("RetainFragment");
		if (localRetainFragment == null) {
			localRetainFragment = new RetainFragment();
			paramFragmentManager.beginTransaction().add(localRetainFragment, "RetainFragment").commit();
		}
		return localRetainFragment;
	}

	public Object getObject() {
		return this.mObject;
	}

	public void onCreate(Bundle paramBundle) {
		super.onCreate(paramBundle);
		setRetainInstance(true);
	}

	public void setObject(Object paramObject) {
		this.mObject = paramObject;
	}
}
