package children.lemoon.player.duplay.vermgr;

import android.content.Context;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ZipUtils {
	private static ZipUtils jdField_a_of_type_ComBaiduCyberplayerUtilsZipUtils;

	class a extends ZipInputStream {
		public a(InputStream paramInputStream) {
			super(paramInputStream);
		}

		public long skip(long value) throws IOException {
			if (value < 0L) {
				throw new IllegalArgumentException();
			}
			long l1 = 0L;
			byte[] arrayOfByte = new byte[(int) Math.min(value, 2048L)];
			while (l1 != value) {
				long l2 = value - l1;
				int i = read(arrayOfByte, 0, (int) (arrayOfByte.length > l2 ? l2 : arrayOfByte.length));
				if (i <= 0) {
					return l1;
				}
				l1 += i;
			}
			return l1;
		}
	}

	public static ZipUtils getInstance() {
		if (jdField_a_of_type_ComBaiduCyberplayerUtilsZipUtils == null) {
			jdField_a_of_type_ComBaiduCyberplayerUtilsZipUtils = new ZipUtils();
		}
		return jdField_a_of_type_ComBaiduCyberplayerUtilsZipUtils;
	}

	private ZipEntry jdField_a_of_type_JavaUtilZipZipEntry = null;

	private void a(ZipInputStream paramZipInputStream) {
		try {
			this.jdField_a_of_type_JavaUtilZipZipEntry = paramZipInputStream.getNextEntry();
			while ((this.jdField_a_of_type_JavaUtilZipZipEntry != null) && (this.jdField_a_of_type_JavaUtilZipZipEntry.isDirectory())) {
				this.jdField_a_of_type_JavaUtilZipZipEntry = paramZipInputStream.getNextEntry();
			}
		} catch (IOException localIOException) {
			throw new RuntimeException("could not get next zip entry", localIOException);
		} catch (RuntimeException localRuntimeException) {
		} finally {
			if (this.jdField_a_of_type_JavaUtilZipZipEntry == null) {
				b(paramZipInputStream);
			}
		}
	}

	private void b(ZipInputStream paramZipInputStream) {
		try {
			if (paramZipInputStream != null) {
				paramZipInputStream.close();
			}
		} catch (IOException localIOException) {
		}
	}

	public void unZip(Context context, String zipFile, String targetDir) throws IOException, Exception {
		int i = 4096;
		try {
			FileInputStream localFileInputStream = new FileInputStream(zipFile);
			a locala = new a(new BufferedInputStream(localFileInputStream));
			a(locala);
			while (this.jdField_a_of_type_JavaUtilZipZipEntry != null) {
				byte[] arrayOfByte = new byte[4096];
				String str = this.jdField_a_of_type_JavaUtilZipZipEntry.getName();

				File localFile1 = new File(targetDir + str);
				boolean bool = context.deleteFile(str);

				File localFile2 = new File(localFile1.getParent());
				if (!localFile2.exists()) {
					localFile2.mkdirs();
				}
				FileOutputStream localFileOutputStream = new FileOutputStream(localFile1);
				BufferedOutputStream localBufferedOutputStream = new BufferedOutputStream(localFileOutputStream, 4096);
				int j = locala.read(arrayOfByte, 0, 4096);
				while (j > 0) {
					localBufferedOutputStream.write(arrayOfByte, 0, j);
					j = locala.read(arrayOfByte, 0, 4096);
				}
				a(locala);
				localBufferedOutputStream.close();
			}
			locala.close();
		} catch (IOException localIOException1) {
			throw new IOException(localIOException1.toString());
		} catch (Exception localException1) {
			throw new Exception(localException1.toString());
		}
	}
}
