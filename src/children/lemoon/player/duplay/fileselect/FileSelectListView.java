package children.lemoon.player.duplay.fileselect;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;

import children.lemoon.R;

public class FileSelectListView extends ListView implements android.widget.AdapterView.OnItemClickListener {
	private Context mContext;
	private String[] mSuffixs;
	private FileSelectedCallBackBundle mFileSelectedCallBack;
	private FileFilter mFileFilter;
	private FileSelectAdapter mAdapter;
	ArrayList<FileItem> mFileList;

	public FileSelectListView(Context context) {
		super(context);
	}

	public FileSelectListView(Context context, FileFilter fileFilter, FileSelectedCallBackBundle callback) {
		super(context);
		mContext = context;
		mFileFilter = fileFilter;
		mFileSelectedCallBack = callback;
		mAdapter = new FileSelectAdapter(mContext, null);
		// initFileFilter(mSuffixs);
		refreshList(Environment.getExternalStorageDirectory());
		this.setOnItemClickListener(this);
		this.setAdapter(mAdapter);

	}

	private void refreshList(File file) {
		mFileList = getFirstList(file, mFileFilter);
		mAdapter.setList(mFileList);
	}

	private void initFileFilter(FileFilter fileFilter) {
		mFileFilter = fileFilter;
	}

	private ArrayList<FileItem> getFirstList(File currentDir, FileFilter fileFilter) {
		File[] list = currentDir.listFiles(fileFilter);
		ArrayList<FileItem> fileList = new ArrayList<FileItem>();
		String parentPath = currentDir.getParent();
		if (TextUtils.isEmpty(parentPath)) {
			parentPath = currentDir.getAbsolutePath();
		}
		FileItem parent = new FileItem("返回上级", parentPath);
		parent.setFileType(FileItem.DIR_TYPE);
		fileList.add(parent);
		if (list != null && list.length != 0) {
			for (File file : list) {
				FileItem item = new FileItem(file.getName(), file.getAbsolutePath());
				if (file.isDirectory()) {
					item.setFileType(FileItem.DIR_TYPE);
				} else {
					item.setFileType(FileItem.FILE_TYPE);
				}
				fileList.add(item);
			}
		}
		return fileList;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		FileItem item = mFileList.get(position);
		if (item.getFileType() == FileItem.DIR_TYPE) {
			refreshList(new File(item.getFileUri()));
		} else {
			Bundle bundle = new Bundle();
			bundle.putString("name", item.getFileName());
			bundle.putString("uri", item.getFileUri());
			mFileSelectedCallBack.onFileSelected(bundle);
		}
	}

	private class FileSelectAdapter extends BaseAdapter {
		private final int[] mFileIcon = { R.drawable._4ui_duplayer_icon_list_folder, R.drawable._4ui_duplayer_icon_list_unknown };
		private ArrayList<FileItem> mFileList;
		private Context mContext;
		private LayoutInflater mInflater;

		public FileSelectAdapter(Context context, ArrayList<FileItem> fileList) {
			mContext = context;
			mFileList = fileList;
			mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		public void setList(ArrayList<FileItem> fileList) {
			mFileList = fileList;
			this.notifyDataSetChanged();
		}

		@Override
		public int getCount() {
			if (mFileList == null) {
				return 0;
			}
			return mFileList.size();
		}

		@Override
		public Object getItem(int position) {
			if (mFileList == null) {
				return null;
			}
			return mFileList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout._4ui_duplayer_file_list_item, null);
				holder = new ViewHolder();
				holder.icon = (ImageView) convertView.findViewById(R.id.file_icon);
				holder.fileName = (TextView) convertView.findViewById(R.id.file_name);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			FileItem item = mFileList.get(position);
			holder.icon.setImageResource(mFileIcon[item.getFileType()]);
			holder.fileName.setText(item.getFileName());
			return convertView;
		}
	}

	static class ViewHolder {
		ImageView icon;
		TextView fileName;
	}
}
