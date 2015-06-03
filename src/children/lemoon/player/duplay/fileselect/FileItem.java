package children.lemoon.player.duplay.fileselect;

public class FileItem {
	public final static int DIR_TYPE = 0;
	public final static int FILE_TYPE = 1;

	private int mFileType;
	private String mFileName;
	private String mFileUri;

	public FileItem(String fileName, String fileUri) {
		mFileName = fileName;
		mFileUri = fileUri;
	}

	public int getFileType() {
		return mFileType;
	}

	public void setFileType(int type) {
		mFileType = type;
	}

	public String getFileName() {
		return mFileName;
	}

	public String getFileUri() {
		return mFileUri;
	}
}
