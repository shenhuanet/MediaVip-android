package com.shenhua.mediavip;

import java.io.IOException;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.webkit.WebView;
import android.widget.TextView;

public class GetSource {
	private Context mContext;
	private WebView mWebView;
	private TextView mTextView;
	private String code;
	private final String HTML_SOURCEFILE_NAME = "html.txt";

	public GetSource(Context context, WebView webView, TextView textView) {
		this.mContext = context;
		this.mWebView = webView;
		this.mTextView = textView;
	}

	public void getHtmlSource(final int a) {
		mTextView.setText("正在初始化...\n获取数据中...");
		new Thread() {
			@Override
			public void run() {
				try {
					code = http_get(Line.getLine(a));
				} catch (Exception e) {
					e.printStackTrace();
				}
				handler.sendEmptyMessage(0);
			}
		}.start();

	}

	public Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (msg.what < 0) {
				mTextView.setText("正在初始化...\n获取数据中...\n数据获取失败！");
			} else {
				mTextView.setText("正在初始化...\n获取数据中...\n数据获取成功！");
				Utils htmlfileout = new Utils(mContext);
				htmlfileout.writeFileData(HTML_SOURCEFILE_NAME, code);
				try {
					// code = ChangeSource(code);
					mTextView.setText("正在初始化...\n获取数据中...\n数据获取成功！\n正在转换数据...");
					Utils fileout = new Utils(mContext);
					fileout.writeFileData("ChangeSource.txt", code);
					mTextView
							.setText("正在初始化...\n获取数据中...\n数据获取成功！\n正在转换数据...\n数据转换成功！");
					final String mimeType = "text/html";
					final String encoding = "UTF-8";
					Utils file = new Utils(mContext);
					String s = file.readFile("ChangeSource.txt");
//					Log.e("ChangeSource", s);
					mWebView.loadDataWithBaseURL("", s, mimeType, encoding, "");
					mTextView
							.setText("正在初始化...\n获取数据中...\n数据获取成功！\n正在转换数据...\n数据转换成功！\n正在解析数据...");
				} catch (Exception e) {
					mTextView
							.setText("正在初始化...\n获取数据中...\n数据获取成功！\n正在转换数据...\n数据转换失败！");
				}
			}
		}
	};

	private String http_get(String url) {
		final int RETRY_TIME = 5;
		HttpClient httpClient = null;
		HttpGet httpGet = null;
		String responseBody = "";
		int time = 0;
		do {
			try {
				httpClient = getHttpClient();
				httpGet = new HttpGet(url);
				httpGet.setHeader("Content-Type",
						"application/x-www-form-urlencoded; charset=utf-8");
				HttpResponse response = httpClient.execute(httpGet);
				if (response.getStatusLine().getStatusCode() == 200) {
					byte[] bResult = EntityUtils.toByteArray(response
							.getEntity());
					if (bResult != null) {
						responseBody = new String(bResult, "GB2312");
					}
				}
				break;
			} catch (IOException e) {
				time++;
				if (time < RETRY_TIME) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e1) {
					}
					continue;
				}
				e.printStackTrace();
			} finally {
				httpClient = null;
			}
		} while (time < RETRY_TIME);
		return responseBody;
	}

	@SuppressWarnings("unused")
	private String ChangeSource(String sorce) {
		String pat1 = null, pat2 = null;
		String jsa = "<script type=\"text/javascript\">", jsb = "</script>";
		Log.e("ChangeSource", "begin");
		// List<String> pat2 = null;
		String change = "<head><script type=\"text/javascript\" src=\"http://cbjs.baidu.com/js/m.js\"></script>\n<script type=\"text/javascript\">\n";
		Pattern p1 = Pattern.compile("<script .*?\\n(.*?)\\n</script>");
		Matcher m1 = p1.matcher(sorce);
		while (m1.find()) {
			MatchResult mr = m1.toMatchResult();
			pat1 = mr.group();

			// Log.e("groupCount", pat1);
		}

		// Pattern p2 = Pattern.compile(jsa + "(.*?)" + jsb);
		// Matcher m2 = p2.matcher(sorce);
		// for (int j = 0; j < m2.groupCount(); j++) {
		// MatchResult mr = m2.toMatchResult();
		// pat2 = (mr.group(1));
		// Log.e("groupCount", "2");
		// }

		change = change + pat1 + "\n</script></head>\n<body>\n" + jsa + pat2
				+ jsb + "\n</body>";
//		Log.e("ssssss", change);
		return change;
	}

	private HttpClient getHttpClient() {
		HttpParams httpParams = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParams, 8000);
		HttpConnectionParams.setSoTimeout(httpParams, 40000);
		return new DefaultHttpClient(httpParams);
	}

}
