package me.madhukiran.bookza;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.impl.client.DefaultHttpClient;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.app.Activity;
import android.app.DownloadManager;
import android.app.DownloadManager.Request;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

public class MainActivity extends Activity {

	String servicestring = Context.DOWNLOAD_SERVICE;
	DownloadManager downloadmanager;
	String url = "http://www.bookza.org/s?q=";
	ProgressDialog mProgressDialog;
	EditText editView;
	ListView list;
	CustomAdapter adapter;
	public MainActivity CustomListView = null;
	ArrayList<book> books = new ArrayList<book>();
	String LocationHeader;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		CustomListView = this;
		this.getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		editView = (EditText) findViewById(R.id.edit_message);
		/** For search key in keyboard **/
		editView.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId,
					android.view.KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_SEARCH) {
					String message = editView.getText().toString();
					books.clear();
					url = "http://www.bookza.org/s?q=";
					try {
						url = url + URLEncoder.encode(message, "UTF-8")
								+ "&t=0";
					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					Log.d("Url", url);
					new getData().execute();
				}
				return false;
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	private class getData extends AsyncTask<Void, Void, Void> {
		// String output = "";

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			mProgressDialog = new ProgressDialog(MainActivity.this);
			mProgressDialog.setTitle("Connecting to server");
			mProgressDialog.setMessage("Loading...");
			mProgressDialog.setIndeterminate(false);
			mProgressDialog.show();
		}

		@Override
		protected Void doInBackground(Void... params) {
			try {
				// Connect to the web site
				Document document = Jsoup.connect(url).get();

				Element results = document.getElementById("searchResultBox");
				// output = results.toString();
				if (results != null) {
					Elements links = results.getElementsByClass("resItemBox");
					if (!links.isEmpty()) {
						for (Element link : links) {
							book temp = new book();

							// getting name
							Elements sublinks = link.getElementsByTag("h3");
							temp.setName(sublinks.get(0).text());

							Element lhref = sublinks.get(0).parent();
							temp.setLink("http://bookza.org/"
									+ lhref.attr("href"));

							// getting dlink and type
							Elements dwlinks = link
									.getElementsByClass("ddownload");
							if (!dwlinks.isEmpty()) {
								temp.setDlink(dwlinks.get(0).attr("href"));
								String temptype = dwlinks.get(0).text();
								int start = temptype.indexOf("(") + 1;
								int end = temptype.indexOf(")");
								temp.setType(temptype.substring(start, end));
							} else {
								temp.setDlink(null);
							}

							// getting language and size
							Elements lanlinks = link
									.getElementsByAttributeValue("itemprop",
											"inLanguage");

							if (!lanlinks.isEmpty()) {
								String language = lanlinks.get(0).text()
										.replace("en", "English")
										.replace("ru", "Russian");
								temp.setLang(language);
								System.out.println(language);

								// getting size
								Element sizelink = lanlinks.get(0).parent();
								String tempsize = sizelink.text();
								int j = tempsize.indexOf(",");
								String subtempsize = tempsize.substring(0, j);
								temp.setSize(subtempsize);
								System.out.println(subtempsize);
							} else {
								temp.setLang("Unknown");
								temp.setSize("Unknown");
							}

							// getting authors
							Elements authors = link.getElementsByTag("i");
							if (!authors.isEmpty()) {
								temp.setAuthors(authors.get(0).text());
							} else {
								temp.setAuthors("Unknown");
							}
							if (temp.dlink != null) {
								books.add(temp);
							}
						}
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			Resources res = getResources();
			list = (ListView) findViewById(R.id.list); // List defined in XML (
														// See Below )

			/**************** Create Custom Adapter *********/
			adapter = new CustomAdapter(CustomListView, books, res);
			list.addHeaderView(new View(CustomListView));
			list.addFooterView(new View(CustomListView));
			list.setAdapter(adapter);
			mProgressDialog.dismiss();
		}
	}

	public void onItemClick(final int mPosition) {
		book tempValues = (book) books.get(mPosition);
		// Toast.makeText(CustomListView,tempValues.getDlink()+"\n"+tempValues.getLink(),
		// Toast.LENGTH_LONG).show();
		File direct = new File(Environment.getExternalStorageDirectory()
				+ "/BookZa");

		if (!direct.exists()) {
			direct.mkdirs();
		}
		// new getUrl().execute(tempValues);
		downloadmanager = (DownloadManager) getSystemService(servicestring);
		Uri uri = Uri.parse(tempValues.dlink);
		DownloadManager.Request request = new Request(uri);
		request.addRequestHeader("Accept", "*/*");
		request.addRequestHeader("Accept-Encoding", "gzip,deflate,sdch");
		request.addRequestHeader("Accept-Language",
				"en-US,en;q=0.8,te;q=0.6,en-GB;q=0.4,hi;q=0.2");
		request.addRequestHeader("Referer", url);
		request.addRequestHeader(
				"User-Agent",
				"Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.154 Safari/537.36");
		request.setDestinationInExternalPublicDir("/BookZa",
				tempValues.getName() + "." + tempValues.getType());
		final Long downloadId = downloadmanager.enqueue(request);

		// final ProgressBar mProgressBar = (ProgressBar)
		// findViewById(R.id.progressBar1);

		new Thread(new Runnable() {

			@Override
			public void run() {

				boolean downloading = true;

				while (downloading) {

					DownloadManager.Query q = new DownloadManager.Query();
					q.setFilterById(downloadId);

					Cursor cursor = downloadmanager.query(q);
					cursor.moveToFirst();
					int bytes_downloaded = cursor.getInt(cursor
							.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
					int bytes_total = cursor.getInt(cursor
							.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));

					if (cursor.getInt(cursor
							.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_SUCCESSFUL) {
						downloading = false;
					}

					final int dl_progress = (int) (((double) bytes_downloaded / (double) bytes_total) * 100);

					runOnUiThread(new Runnable() {

						@Override
						public void run() {

							// mProgressBar.setProgress((int) dl_progress);
							books.get(mPosition).setProgress(dl_progress);
							adapter.notifyDataSetChanged();
						}
					});

					// Log.d(Constants.MAIN_VIEW_ACTIVITY,
					// statusMessage(cursor));
					cursor.close();
				}

			}
		}).start();

	}

	private class getUrl extends AsyncTask<book, Void, Void> {
		// String output = "";

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			mProgressDialog = new ProgressDialog(MainActivity.this);
			mProgressDialog.setTitle("Android Basic JSoup Tutorial");
			mProgressDialog.setMessage("Loading...");
			mProgressDialog.setIndeterminate(false);
			mProgressDialog.show();
		}

		@Override
		protected Void doInBackground(book... params) {
			book tempValues = params[0];
			try {
				HttpClient client = new DefaultHttpClient();
				client.getParams().setParameter("http.protocol.version",
						HttpVersion.HTTP_1_0);
				client.getParams().setParameter(ClientPNames.HANDLE_REDIRECTS,
						false);
				String getURL = tempValues.getDlink();
				HttpGet get = new HttpGet(getURL);

				get.setHeader("Accept", "*/*");
				get.setHeader("Accept-Encoding", "gzip,deflate,sdch");
				get.setHeader("Accept-Language",
						"en-US,en;q=0.8,te;q=0.6,en-GB;q=0.4,hi;q=0.2");
				get.setHeader("Referer", url);
				get.setHeader(
						"User-Agent",
						"Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.154 Safari/537.36");

				HttpResponse responseGet = client.execute(get);
				HttpEntity resEntityGet = responseGet.getEntity();

				Header[] headers = responseGet.getAllHeaders();
				for (Header header : headers) {
					System.out.println("Key : " + header.getName()
							+ " ,Value : " + header.getValue());
				}
				LocationHeader = responseGet.getFirstHeader("location")
						.getValue();
				//
				// if (resEntityGet != null) {
				// //do something with the response
				// String LocationHeader =
				// responseGet.getFirstHeader("location").getValue();
				// Log.i("GET ",EntityUtils.toString(resEntityGet));
				// Toast.makeText(CustomListView,LocationHeader,
				// Toast.LENGTH_LONG).show();
				// }
				// else {
				// Toast.makeText(CustomListView,"apparently nothing happened",
				// Toast.LENGTH_LONG).show();
				// }
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;

		}

		@Override
		protected void onPostExecute(Void result) {
			Resources res = getResources();
			list = (ListView) findViewById(R.id.list); // List defined in XML (
														// See Below )

			/**************** Create Custom Adapter *********/
			adapter = new CustomAdapter(CustomListView, books, res);
			list.setAdapter(adapter);
			mProgressDialog.dismiss();
		}
	}

}
