package bulman.daniel.bleattractions;

import android.annotation.SuppressLint;
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

        protected htmlDownloader(String pURL, Context pContext) {
            mURL = pURL;
            mApplicationContext = pContext;
            String[] segments = pURL.split("/");
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
                Toast.makeText(mApplicationContext, "File " + mFileName + " has been written!", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
