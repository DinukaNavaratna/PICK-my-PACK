package com.dinukanavaratna.pickmypack;

import android.os.AsyncTask;
import android.util.Log;

import com.dinukanavaratna.pickmypack.dataStoring.localDB;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class insertData {

    String ServerURL = "" ;
    private List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

    public insertData(List<NameValuePair> nameValuePairs1){
        this.nameValuePairs = nameValuePairs1;
        this.ServerURL = "http://infotechdesigners.com/PICK%20my%20PACK/insert_update.php?code=159753";
        InsertData();
    }

    public void InsertData(){

        class SendPostReqAsyncTask extends AsyncTask<String, Void, String> {
            @Override
            protected String doInBackground(String... params) {
                try {

// ---------------------HTTP---------------------------------
                    HttpClient httpClient = new DefaultHttpClient();
                    HttpPost httpPost = new HttpPost(ServerURL);
// Url Encoding the POST parameters
                    try {
                        httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                    }
                    catch (UnsupportedEncodingException e) {
                        // writing error to Log
                        e.printStackTrace();
                    }
                    try {
                        HttpResponse response = httpClient.execute(httpPost);

                        // writing response to log
                        Log.d("Http Response:", response.toString());

                    } catch (ClientProtocolException e) {
                        // writing exception to log
                        e.printStackTrace();

                    } catch (IOException e) {
                        // writing exception to log
                        e.printStackTrace();
                    }
// ---------------------HTTP---------------------------------


// ---------------------HTTPS---------------------------------

//                    HostnameVerifier hostnameVerifier = SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;
//                    DefaultHttpClient client = new DefaultHttpClient();
//
//                    Log.i("Insert", "1");
//
//                    SchemeRegistry registry = new SchemeRegistry();
//                    SSLSocketFactory socketFactory = SSLSocketFactory.getSocketFactory();
//                    socketFactory.setHostnameVerifier((X509HostnameVerifier) hostnameVerifier);
//                    registry.register(new Scheme("https", socketFactory, 443));
//                    SingleClientConnManager mgr = new SingleClientConnManager(client.getParams(), registry);
//                    DefaultHttpClient httpClient = new DefaultHttpClient(mgr, client.getParams());
//
//                    Log.i("Insert", "2");
//
//                    // Set verifier
//
//                    HttpsURLConnection.setDefaultHostnameVerifier(hostnameVerifier);
//                    Log.i("Insert", "2.1");
//                    HttpPost httpPost = new HttpPost(ServerURL);
//                    Log.i("Insert", "2.2");
//                    httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
//                    Log.i("Insert", "2.3");
//                    HttpResponse httpResponse = httpClient.execute(httpPost);
//                    Log.i("Insert", "2.4");
//                    HttpEntity httpEntity = httpResponse.getEntity();
//                    Log.i("Insert", "2.5");
//                    Log.i("Insert Response", httpEntity.toString());
//                    Log.i("Insert", "3");
//
//                } catch (UnsupportedEncodingException e) {
//                    e.printStackTrace();
//                    Log.d("Ex1", e.toString());
//                } catch (ClientProtocolException e) {
//                    Log.d("Ex2", e.toString());

// ---------------------HTTPS---------------------------------
                } catch (Exception e) {
                    Log.d("Ex3", e.toString());
                }
                Log.i("Insert", "4");

                return "Data Inserted Successfully";

            }

            @Override
            protected void onPostExecute(String result) {

                Log.i("Insert", "5");

                super.onPostExecute(result);

                final String[] tables = {"shopping_group", "shopping_pins"};
                com.dinukanavaratna.pickmypack.dataStoring.localDB localdb = new localDB();
                localdb.getAllData(tables);

                Log.i("Insert", "6");

            }
        }

        SendPostReqAsyncTask sendPostReqAsyncTask = new SendPostReqAsyncTask();

        sendPostReqAsyncTask.execute();

        Log.i("Insert", "7");

    }
}
