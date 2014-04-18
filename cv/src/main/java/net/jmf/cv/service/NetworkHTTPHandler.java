package net.jmf.cv.service;

import org.apache.http.client.methods.HttpUriRequest;

/**
 * Abstract class to implement in order to make an http call
 * Created by Jean-Mi on 18/04/2014.
 */
public abstract class NetworkHttpHandler {
    public abstract HttpUriRequest getHttpRequestMethod();

    public abstract void onResponse(String result);

    public abstract void onCancel(String result);

    public void execute() {
        new NetworkHttpMethod(this).execute();
    }
}
