package me.madhukiran.bookza;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class MainActivity extends Activity {

	String url = "http://www.bookza.org/s?q=";
	ProgressDialog mProgressDialog;
	EditText editView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		editView = (EditText) findViewById(R.id.edit_message);

		/** For search key in keyboard **/
		editView.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId,
					android.view.KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_SEARCH) {
					String message = editView.getText().toString();
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
		ArrayList<book> books = new ArrayList<book>();

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
		protected Void doInBackground(Void... params) {
			try {
				// Connect to the web site
				Document document = Jsoup.connect(url).get();

				Element results = document.getElementById("searchResultBox");
				// output = results.toString();

				Elements links = results.getElementsByClass("resItemBox");

				for (Element link : links) {
					book temp = new book();

					// getting name
					Elements sublinks = link.getElementsByTag("h3");
					temp.setName(sublinks.get(0).text());

					Element lhref = sublinks.get(0).parent();
					temp.setLink("http://bookza.org/" + lhref.attr("href"));

					// getting dlink and type
					Elements dwlinks = link.getElementsByClass("ddownload");
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
					Elements lanlinks = link.getElementsByAttributeValue(
							"itemprop", "inLanguage");

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

					books.add(temp);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			mProgressDialog.dismiss();
		}
	}
}
