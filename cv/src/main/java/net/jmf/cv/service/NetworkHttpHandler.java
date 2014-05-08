package net.jmf.cv.service;

import org.apache.http.client.methods.HttpUriRequest;

/**
 * Abstract class to implement in order to make an http call
 * Created by Jean-Mi on 18/04/2014.
 */
public abstract class NetworkHttpHandler {
    /**
     * Get or Post...
     *
     * @return HttpUriRequest
     */
    public abstract HttpUriRequest getHttpRequestMethod();

    /**
     * When succeeded
     *
     * @param result String
     */
    public abstract void onResponse(String result);

    /**
     * When cancelled
     *
     * @param result String
     */
    public abstract void onCancel(String result);

    /**
     * Make the call
     */
    public void execute() {
        new NetworkHttpMethod(this).execute();
    }
}
