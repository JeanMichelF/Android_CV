package net.jmf.cv.fragment;

import android.annotation.SuppressLint;
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

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static PlaceholderFragmentWebView newInstance() {
        return new PlaceholderFragmentWebView();
    }

    public PlaceholderFragmentWebView() {}

    // todo gérer la rotation : sur jobs.php, avoir les mêmes choses d'ouvertes
    // todo gérer le retour sur une page qui est ko (quand le setOffscreenPageLimit = 5)
    @Override
    @SuppressLint("SetJavaScriptEnabled")
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_webview, container, false);
        assert view != null;
        url = getArguments().getString(ARG_SECTION_URL);
        //TextView textView = (TextView) rootView.findViewById(R.id.section_label);
        //textView.setText(Integer.toString(getArguments().getInt(ARG_SECTION_NUMBER)));
        Log.d("DEBUG", "Création de la vue " + url);
        WebView webView = (WebView) view.findViewById(R.id.webView);
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
            Log.e("BROWSER", "App cache path : " + appCachePath);
        } catch (NullPointerException e) {
            Log.e("BROWSER", "Error setting app cache : " + e.getMessage());
        }
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setAppCacheEnabled(true);
        webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        webView.getSettings().setLoadsImagesAutomatically(true);
        webView.loadUrl(url);
        return view;
    }
    private class MyBrowser extends WebViewClient {
        // Les URLs sont pour le navigateur, pas pour la WebView
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                view.getContext().startActivity(intent);
            } catch (NullPointerException e) {
                Log.e("BROWSER", "Error accessing browser external to WebView : " + e.getMessage());
            }
            return true;
        }

        // todo gérer le cache
        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            Log.e("BROWSER", failingUrl + " " + errorCode + " " + description);
            // Seems that there isn't a network around there
            if (view.getSettings().getCacheMode() == WebSettings.LOAD_NO_CACHE) {
                Log.e("BROWSER", "No network available for " + failingUrl + " " + errorCode + " " + description);
                // Using LOAD_CACHE_ELSE_NETWORK makes a retry via network (this things can be capricious)
                view.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
                view.loadUrl(failingUrl);
            // Seems that there isn't a network around there and no cache neither !
            } else if (view.getSettings().getCacheMode() == WebSettings.LOAD_CACHE_ELSE_NETWORK) {
                Log.e("BROWSER", "No network and no cache available " + failingUrl + " " + errorCode + " " + description);
                view.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
                view.loadData("<h1>Un probl&egrave;me est survenu</h1>.<h2>Avez-vous une connection &agrave; internet active ?</h2>", "text/html", "utf-8");
                // No cache for the error message
                view.clearCache(true);
            }
        }
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            Log.e("BROWSER", "Loading... " + url);
            super.onPageStarted(view, url, favicon);
        }
        @Override
        public void onPageFinished(WebView view, String url) {
            Log.e("BROWSER", "Finish loading... " + url);
            super.onPageFinished(view, url);
        }
    }

}
