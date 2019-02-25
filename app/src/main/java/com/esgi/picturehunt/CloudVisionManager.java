package com.esgi.picturehunt;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

public class CloudVisionManager extends AsyncTask<String,Void,String> {

    private Context context;
    private String result;


    public CloudVisionManager(Context context) {
        this.context = context;
    }

    private static final String GOOGLE_VISION_API_URL = "https://vision.googleapis.com/v1/images:annotate?key=AIzaSyD3ueDi3hBoLyBmund500KFIsGSZrmX0no ";
    private static  String body = "{\"requests\":[{\"image\":{\"source\":{\"imageUri\":\"<URI>\"}},\"features\":[{\"type\":\"LABEL_DETECTION\",\"maxResults\":10}]}]}";
    @Override
    protected String doInBackground(String... strings) {
        String output = null;

        body = body.replace("<URI>", strings[0]);
        HttpURLConnection urlConnection = null;
        BufferedWriter httpRequestBodyWriter = null;
        URL url = null;
        try {
            url = new URL(GOOGLE_VISION_API_URL);
            urlConnection = (HttpURLConnection)url.openConnection();
            urlConnection.setConnectTimeout(15000);
            urlConnection.setReadTimeout(15000);
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Content-Type", "application/json");
            urlConnection.setDoOutput(true);

            OutputStream os = urlConnection.getOutputStream();
            OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");
            osw.write(body);
            osw.flush();
            osw.close();
            os.close();  //don't
            urlConnection.connect();
            InputStream is = urlConnection.getInputStream();

            BufferedInputStream bis = new BufferedInputStream(is);
            BufferedReader br = new BufferedReader(new InputStreamReader(bis));
            String line = null;
            StringBuffer sb = new StringBuffer();
            while((line =br.readLine()) != null){
                sb.append(line);

            }
            output = sb.toString();
            br.close();

            is.close();
            urlConnection.disconnect();


            urlConnection.disconnect();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            Toast.makeText(context, "Une erreur est survenue", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }finally {

        }

        result = output;
        return  output;
    }

    @Override
    protected void onPostExecute(String s) {
        Toast.makeText(context, s, Toast.LENGTH_SHORT).show();
    }

    public String getResult() {
        return result;
    }
}