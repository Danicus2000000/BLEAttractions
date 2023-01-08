package bulman.daniel.bleattractions;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.os.AsyncTask;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
    @SuppressLint("StaticFieldLeak")
    public class htmlDownloader extends AsyncTask<Void,Void,String> {//gets filenames
        private final String mURL;
        private final String mFileName;
        private final Context mApplicationContext;
        private final ProgressDialog mDialog;
        protected htmlDownloader(String pURL, Context pContext, ProgressDialog pDialog) {
            mURL = pURL;
            mApplicationContext = pContext;
            String[] segments = pURL.split("/");
            mDialog=pDialog;
            if (segments[segments.length - 1].contains(".css")) {
                mFileName = segments[segments.length - 1];
            } else {
                mFileName = segments[segments.length - 1] + ".html";
            }
        }

        @Override
        protected String doInBackground(Void... voids) {//downloads html
            URL url = null;
            try {
                url = new URL(mURL);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            BufferedReader in = null;
            try {
                in = new BufferedReader(new InputStreamReader(url.openStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }
            String input = null;
            StringBuilder stringBuilder = new StringBuilder();
            while (true) {
                try {
                    if ((input = in.readLine()) == null) break;
                } catch (IOException e) {
                    e.printStackTrace();
                }
                stringBuilder.append(input);
            }
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return stringBuilder.toString();
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            saveHTML(result);
        }

        private void saveHTML(String contents)//saves html file locally
        {
            ContextWrapper contextWrapper = new ContextWrapper(mApplicationContext);
            try {
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(contextWrapper.openFileOutput(mFileName, Context.MODE_PRIVATE));
                outputStreamWriter.write(contents);
                outputStreamWriter.flush();
                outputStreamWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if(mDialog!=null) {//if there is a progress dialog close it
                mDialog.dismiss();
                Toast.makeText(contextWrapper,"Download Complete!",Toast.LENGTH_SHORT).show();
            }
        }
    }
