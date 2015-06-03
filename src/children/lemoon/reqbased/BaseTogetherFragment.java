package children.lemoon.reqbased;

import children.lemoon.reqbased.entry.ControlcurrentThread;
import children.lemoon.reqbased.entry.ResHeadAndBody;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;

public abstract class BaseTogetherFragment extends Fragment implements ControlcurrentThread {
	public String getFragName() {
		return "";
	};

	/**
	 * 所有继承BackHandledFragment的子类都将在这个方法中实现物理Back键按下后的逻辑
	 * FragmentActivity捕捉到物理返回键点击事件后会首先询问Fragment是否消费该事件
	 * 如果没有Fragment消息时FragmentActivity自己才会消费该事件
	 */
	public abstract boolean onBackPressed();

	protected Handler bHandler = new Handler() {
		public void handleMessage(Message paramAnonymousMessage) {
			if (paramAnonymousMessage.arg2 == 99) {
				onPostHandle(paramAnonymousMessage.what, paramAnonymousMessage.obj, true, 0, null, null);
				return;
			}
			if (paramAnonymousMessage.obj != null) {
				ResHeadAndBody localResHeadAndBody = (ResHeadAndBody) paramAnonymousMessage.obj;
				int i = localResHeadAndBody.getHeader().getRetStatus();
				onPostHandle(paramAnonymousMessage.what, localResHeadAndBody, false, i, null, null);
				return;
			}
			onPostHandle(paramAnonymousMessage.what, null, false, paramAnonymousMessage.arg1, null, null);
		}
	};
	public AsyncTask currentTask;

	public void getControlcurrentThread(AsyncTask paramAsyncTask) {
		currentTask = paramAsyncTask;
	}

	public void onCreate(Bundle paramBundle) {
		super.onCreate(paramBundle);
		setRetainInstance(true);

	}

	@Override
	public void onStart() {
		// TODO Auto-generated method stub
		super.onStart();

	}

	protected void onPostHandle(int paramInt1, Object paramObject1, boolean paramBoolean, int paramInt2, Object paramObject2, Object paramObject3) {
	}
}