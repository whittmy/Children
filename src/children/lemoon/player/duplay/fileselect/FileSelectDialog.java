package children.lemoon.player.duplay.fileselect;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;

import java.io.FileFilter;

public class FileSelectDialog {
	private final static String TAG = "FileSelectDialog";
	private final static String S_PARENT_FOLDER = "..";
	private final static String S_CURRENT_FOLDER = ".";

	private FileSelectDialog() {
		// 不允许
	}

	public static Dialog createDialog(Context context, String title, FileFilter fileFilter, FileSelectedCallBackBundle callback) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setView(new FileSelectListView(context, fileFilter, callback));
		Dialog dialog = builder.create();

		dialog.setTitle(title);
		return dialog;
	}
}
