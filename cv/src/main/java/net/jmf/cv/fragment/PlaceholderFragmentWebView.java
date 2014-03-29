package net.jmf.cv.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

    //todo check settings usefull for the webview
    @Override
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
        //webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        //webView.getSettings().setPluginState(WebSettings.PluginState.ON);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(false);
        webView.getSettings().setSupportMultipleWindows(false);
        webView.getSettings().setSupportZoom(false);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.loadUrl(url);
        return view;
    }
    private class MyBrowser extends WebViewClient {
        // Les URLs sont pour le navigateur, pas pour la WebView
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (view != null) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                view.getContext().startActivity(intent);
            }
            return true;
        }
        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            Log.e("BROWSER", errorCode + " " + description);
        }
    }

}
