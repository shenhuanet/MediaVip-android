package com.shenhua.mediavip;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class Utils {
	private Context mContext;

	public Utils(Context context) {
		this.mContext = context;
	}

	public boolean isNetworkAvailable() {

		ConnectivityManager connectivityManager = (ConnectivityManager) mContext
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectivityManager == null) {
			return false;
		} else {
			NetworkInfo[] networkInfo = connectivityManager.getAllNetworkInfo();
			if (networkInfo != null && networkInfo.length > 0) {
				for (int i = 0; i < networkInfo.length; i++) {
					if (networkInfo[i].getState() == NetworkInfo.State.CONNECTED) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public void writeFileData(String fileName, String message) {
		try {
			FileOutputStream fout = mContext.openFileOutput(fileName,
					Context.MODE_PRIVATE);
			byte[] bytes = message.getBytes();
			fout.write(bytes);
			fout.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String readFile(String fileName) {
		String res = "";
		try {
			FileInputStream fin = mContext.openFileInput(fileName);
			int length = fin.available();
			byte[] buffer = new byte[length];
			fin.read(buffer);
			res = EncodingUtils.getString(buffer, "UTF-8");
			fin.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return res;
	}

	public String readAgree() {
		try {
			InputStream in = mContext.getAssets().open("agreement.txt");
			int size = in.available();
			byte[] buffer = new byte[size];
			in.read(buffer);
			in.close();
			String str = new String(buffer);
			return str;
		} catch (Exception e) {

			return null;
		}
	}
}
