package net.jmf.cv.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import net.jmf.cv.R;

/**
 * Created by Jean-Mi on 26/03/2014.
 */
public class PlaceholderFragmentWebView extends Fragment {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    protected static final String ARG_SECTION_URL = "section_number";

    private String url;

    private Context context;
    private WebView webView;
    private boolean mustBeRefresh;

    /**
     * Returns a new instance of this fragment
     */
    public static PlaceholderFragmentWebView newInstance() {
        return new PlaceholderFragmentWebView();
    }

    /**
     *
     */
    public PlaceholderFragmentWebView() {}


    /**
     * Create the webview
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_webview, container, false);
        assert view != null;
        Log.d("DEBUG", "Création de la vue " + url);
        webView = (WebView) view.findViewById(R.id.webView);
        return view;
    }

    /**
     * Load an URL for the webview
     */
    @Override
    @SuppressLint("SetJavaScriptEnabled")
    public void onActivityCreated(Bundle savedInstanceState) {
        context = getActivity();
        url = getArguments().getString(ARG_SECTION_URL);
        /*if (savedInstanceState != null) {
            webView.restoreState(savedInstanceState);
        } else {*/
            webView.setWebViewClient(new MyBrowser());
            webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
            webView.getSettings().setJavaScriptEnabled(true);
            webView.getSettings().setSupportZoom(false);
            webView.getSettings().setLoadWithOverviewMode(true);
            webView.getSettings().setUseWideViewPort(true);
            // Cache gestion
            // This next one is crazy. It's the DEFAULT location for your app's cache
            // But it didn't work for me without this line.
            try {
                String appCachePath = getActivity().getCacheDir().getAbsolutePath();
                webView.getSettings().setAppCachePath(appCachePath);
                Log.d("BROWSER", "App cache path : " + appCachePath);
            } catch (NullPointerException e) {
                Log.d("BROWSER", "Error setting app cache : " + e.getMessage());
            }
            webView.getSettings().setDomStorageEnabled(true);
            webView.getSettings().setAllowFileAccess(true);
            webView.getSettings().setAppCacheEnabled(true);
            webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
            webView.getSettings().setLoadsImagesAutomatically(true);
            webView.loadUrl(url);
        //}
        super.onActivityCreated(savedInstanceState);
    }

    // todo gérer la rotation : sur jobs.php, avoir les mêmes choses d'ouvertes : apparemment impossible
    /**
     * Try to refresh the webview if it was displaying "No network found" because of
     * network unreachable and nothing in cache
     */
    @Override
    public void onResume() {
        super.onResume();
        Log.d("BROWSER", "Dans OnResume");
        if (mustBeRefresh) {
            Log.d("BROWSER", "On reload");
            webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
            webView.loadUrl(url);
        }
    }

    /**
     * Handle webview state when rotating (for example)
     * Not usefull since site is fully loaded in cache
     * @param outState
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (null != webView) {
            webView.saveState(outState);
        }
        super.onSaveInstanceState(outState);
        // Sauvegarde des données du contexte utilisateur
        //outState.putInt("curChoice", mCurCheckPosition);
    }

    /**
     * Handle links into a new browser and try to load from cache if network is not reachable
     */
    private class MyBrowser extends WebViewClient {

        /**
         * Handle links into a new browser activity
         */
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                context.startActivity(intent);
            } catch (NullPointerException e) {
                Log.e("BROWSER", "Error accessing browser external to WebView : " + e.getMessage());
            }
            return true;
        }

        /**
         * Try to load from cache if network is not reachable
         * @param view
         * @param errorCode
         * @param description
         * @param failingUrl
         */
        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            Log.e("BROWSER", failingUrl + " " + errorCode + " " + description);
            // Seems that there isn't a network around there
            if (view.getSettings().getCacheMode() == WebSettings.LOAD_NO_CACHE) {
                Log.e("BROWSER", "No network available for " + failingUrl + " " + errorCode + " " + description);
                // Using LOAD_CACHE_ELSE_NETWORK makes a retry via network (this things can be capricious)
                view.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
                view.loadUrl(failingUrl);
                mustBeRefresh = false;
            // Seems that there isn't a network around there and no cache neither !
            } else if (view.getSettings().getCacheMode() == WebSettings.LOAD_CACHE_ELSE_NETWORK) {
                Log.e("BROWSER", "No network and no cache available " + failingUrl + " " + errorCode + " " + description);
                view.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
                view.loadData("<h1>Un probl&egrave;me est survenu</h1>.<h2>Avez-vous une connection &agrave; internet active ?</h2>", "text/html", "utf-8");
                // No cache for the error message
                view.clearCache(true);
                mustBeRefresh = true;
            }
        }

        /**
         * Usefull in debug only
         * @param view
         * @param url
         * @param favicon
         */
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            Log.d("BROWSER", "Loading... " + url);
            super.onPageStarted(view, url, favicon);
        }

        /**
         * Usefull in debug only
         * @param view
         * @param url
         */
        @Override
        public void onPageFinished(WebView view, String url) {
            Log.d("BROWSER", "Finish loading... " + url);
            super.onPageFinished(view, url);
        }
    }

}