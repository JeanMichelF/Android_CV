package net.jmf.cv.service;

import android.os.AsyncTask;
import android.util.Log;

import net.jmf.cv.MyCVActivity;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

/**
 * Generic Class handling HTTP Methods and returning a String
 * Created by Jean-Mi on 18/04/2014.
 */
public class NetworkHttpMethod extends AsyncTask<String, Void, String> {
    private NetworkHttpHandler networkHttpHandler;

    public NetworkHttpMethod(NetworkHttpHandler networkHttpHandler) {
        this.networkHttpHandler = networkHttpHandler;
    }

    /**
     * Make the call
     *
     * @param arg0 Args
     * @return String
     */
    @Override
    protected String doInBackground(String... arg0) {
        String result = "";
        try {
            //Create the HTTP request
            HttpParams httpParameters = new BasicHttpParams();

            //Setup timeouts
            HttpConnectionParams.setConnectionTimeout(httpParameters, 1000);
            HttpConnectionParams.setSoTimeout(httpParameters, 1000);

            HttpClient httpClient = new DefaultHttpClient(httpParameters);
            HttpResponse response = httpClient.execute(networkHttpHandler.getHttpRequestMethod());
            HttpEntity entity = response.getEntity();

            result = EntityUtils.toString(entity);

        } catch (Exception e) {
            MyCVActivity.e("NetworkHTTPGet", "Error: ", e);
        }
        return result;
    }

    /**
     * When fully loaded
     *
     * @param result String
     */
    @Override
    protected void onPostExecute(String result) {
        networkHttpHandler.onResponse(result);
    }

    /**
     * When disabled
     *
     * @param result String
     */
    @Override
    protected void onCancelled(String result) {
        networkHttpHandler.onCancel(result);
    }
}
