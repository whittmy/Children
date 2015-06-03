package children.lemoon.reqbased;

import children.lemoon.reqbased.entry.ControlcurrentThread;
import children.lemoon.reqbased.entry.ResHeadAndBody;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;

public class BaseReqActivity extends Activity implements ControlcurrentThread {
	protected Handler bHandler = new Handler() {
		public void handleMessage(Message msg) {
			if (msg.arg2 == 99) {
				onPostHandle(msg.what, msg.obj, true, 0, null, null);
				return;
			}
			if (msg.obj == null) {
				onPostHandle(msg.what, null, false, msg.arg1, null, null);
				return;
			}

			ResHeadAndBody rslt = (ResHeadAndBody) msg.obj;
			int i = rslt.getHeader().getRetStatus();
			if (i == 0) {
				onPostHandle(msg.what, rslt.getBody(), true, 0, null, null);
			} else {
				onPostHandle(msg.what, rslt, false, i, null, null);
			}
			return;
		}
	};

	protected void onPostHandle(int requestType, Object data, boolean status, int paramInt2, Object paramObject2, Object paramObject3) {
	};

	@Override
	public void getControlcurrentThread(AsyncTask paramAsyncTask) {
		// TODO Auto-generated method stub

	}
}
