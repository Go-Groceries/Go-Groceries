package com.prasunpersonal.gogroceries_customer.Activities;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.prasunpersonal.gogroceries_customer.R;
import com.prasunpersonal.gogroceries_customer.databinding.ActivityPdfViewBinding;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class PDFViewActivity extends AppCompatActivity {
    private ActivityPdfViewBinding binding;
    String pdfUrl, pdfName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPdfViewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        pdfUrl = getIntent().getStringExtra("PDF_URL");
        pdfName = getIntent().getStringExtra("PDF_NAME");

        binding.pdfViewToolbar.setTitle(pdfName);
        setSupportActionBar(binding.pdfViewToolbar);
        binding.pdfViewToolbar.setNavigationOnClickListener(v -> finish());

        new RetrievePDFOnline(this).execute(pdfUrl);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_pdfview, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.download) {
            File file = new File(String.format("%s/%s/%s", Environment.getExternalStorageDirectory(), getString(R.string.app_name),  pdfName));
            DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(pdfUrl));
            request.setTitle(pdfName);
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setDestinationUri(Uri.fromFile(file));
            long reference = manager.enqueue(request);

            registerReceiver(new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (reference == intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)) {
                        Toast.makeText(context, pdfName + " downloaded successfully.", Toast.LENGTH_SHORT).show();
                    }
                }
            }, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        }
        return super.onOptionsItemSelected(item);
    }

    private static class RetrievePDFOnline extends AsyncTask<String, Void, InputStream> {
        private final WeakReference<PDFViewActivity> activityWeakReference;

        public RetrievePDFOnline(PDFViewActivity activity) {
            super();
            activityWeakReference = new WeakReference<>(activity);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            PDFViewActivity activity = activityWeakReference.get();
            activity.binding.pdfView.enableAntialiasing(true);
            activity.binding.loadingProgress.setVisibility(View.VISIBLE);
        }

        @Override
        protected InputStream doInBackground(String... strings) {
            InputStream inputStream = null;
            try {
                URL url = new URL(strings[0]);
                HttpURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
                Log.d("TAG", "doInBackground: "+urlConnection.getContentLength());
                if (urlConnection.getResponseCode() == 200) {
                    inputStream = new BufferedInputStream(urlConnection.getInputStream());
                }
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
            return inputStream;
        }

        @Override
        protected void onPostExecute(InputStream inputStream) {
            PDFViewActivity activity = activityWeakReference.get();
            activity.binding.pdfView.fromStream(inputStream).load();
            activity.binding.loadingProgress.setVisibility(View.GONE);
        }
    }

}