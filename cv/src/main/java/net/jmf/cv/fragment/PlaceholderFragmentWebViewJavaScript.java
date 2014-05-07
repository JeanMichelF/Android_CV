package net.jmf.cv.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import java.util.ArrayList;
import java.util.List;

/**
 * Webview with Javascript
 * Created by Jean-Mi on 27/04/2014.
 */
public class PlaceholderFragmentWebViewJavaScript extends PlaceholderFragmentWebView {

    public static final String CLICKED_ITEMS = "clickedItems";
    public static final String CLICKED_STYLE_ITEMS = "clickedStyleItems";
    public List<String> listClikedItems;
    public List<String> listStyleClikedItems;

    /**
     *
     */
    public PlaceholderFragmentWebViewJavaScript() {
        listClikedItems = new ArrayList<>();
        listStyleClikedItems = new ArrayList<>();
        // With this, we have the pseudo click in Javascript fully operational and no need to save our clicks into onSaveInstanceState
        setRetainInstance(true);
        currentlyLoading = false;
    }

    /**
     * Returns a new instance of this fragment
     */
    public static PlaceholderFragmentWebViewJavaScript newInstance() {
        return new PlaceholderFragmentWebViewJavaScript();
    }

    /**
     * Load an URL for the webview
     */
    @Override
    @SuppressLint("SetJavaScriptEnabled")
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.d("DEBUG", "Création de la vue PlaceholderFragmentWebViewJavaScript " + url);
        super.webView.getSettings().setJavaScriptEnabled(true);

        super.webView.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                // BugFix for E/webcoreglue﹕ Should not happen: no rect-based-test nodes found
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    int temp_ScrollY = v.getScrollY();
                    v.scrollTo(v.getScrollX(), v.getScrollY() + 1);
                    v.scrollTo(v.getScrollX(), temp_ScrollY);
                }
                return false;
            }
        });

        // Add a bridge between Android and Javascript
        super.webView.addJavascriptInterface(new WebAppInterface(this), "CallToAnAndroidFunction");

        // for debugging, this will handle the console.log() in Javascript
        super.webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onConsoleMessage(ConsoleMessage cm) {
                Log.i("PlaceholderFragmentWebViewJavaScript", cm.message() + " #" + cm.lineNumber() + " --" + cm.sourceId());
                return true;
            }
        });

        super.onActivityCreated(savedInstanceState);

        webView.setWebViewClient(new MyBrowserJS());
    }

    /**
     * Getter
     *
     * @return List<String>
     */
    public List<String> getListClikedItems() {
        return listClikedItems;
    }

    /**
     * Getter
     *
     * @return List<String>
     */
    public List<String> getListStyleClikedItems() {
        return listStyleClikedItems;
    }

    /**
     * Handles connexions between Android and JavaScript
     */
    private class WebAppInterface {
        private PlaceholderFragmentWebViewJavaScript placeholderFragmentWebViewJavaScript;

        /**
         * Instantiate the interface and set the context
         */
        WebAppInterface(PlaceholderFragmentWebViewJavaScript ph) {
            placeholderFragmentWebViewJavaScript = ph;
        }

        /**
         * Save all clicks
         *
         * @param idDomElement id of DOM Element clicked
         * @param mustAdd      do we must save this click ? No if we're simulating clicks
         */
        @JavascriptInterface
        public void saveClick(String idDomElement, boolean mustAdd) {
            Log.d("PlaceholderFragmentWebViewJavaScript", idDomElement + " clicked");
            // If it exist then delete instead of add
            if (mustAdd) {
                if (!placeholderFragmentWebViewJavaScript.getListClikedItems().contains(idDomElement)) {
                    placeholderFragmentWebViewJavaScript.getListClikedItems().add(idDomElement);
                } else {
                    placeholderFragmentWebViewJavaScript.getListClikedItems().remove(idDomElement);
                }
            }
        }

        /**
         * Save all clicks and handle styles
         *
         * @param idDomElement id of DOM Element clicked
         * @param mustAdd      do we must save this click ? No if we're simulating clicks
         */
        @JavascriptInterface
        public void saveStyle(String idDomElement, boolean mustAdd) {
            Log.d("PlaceholderFragmentWebViewJavaScript", idDomElement + " style clicked and must Add : " + mustAdd);
            // If it exist then delete instead of add
            if (mustAdd) {
                if (!placeholderFragmentWebViewJavaScript.getListStyleClikedItems().contains(idDomElement)) {
                    placeholderFragmentWebViewJavaScript.getListStyleClikedItems().add(idDomElement);
                } else {
                    placeholderFragmentWebViewJavaScript.getListStyleClikedItems().remove(idDomElement);
                }
            }
        }
    }

    private class MyBrowserJS extends MyBrowser {

        /**
         * Make pseudo click when page is fully loaded
         *
         * @param view Webview
         * @param url  String
         */
        @Override
        public void onPageFinished(WebView view, String url) {
            Log.i("BROWSER JS", "Finish loading... " + url);
            // If there were some clicked items, let's reclick them as they were
            if (!listClikedItems.isEmpty()) {
                ArrayList<String> listClikedItemsTmp = new ArrayList<>();
                listClikedItemsTmp.addAll(listClikedItems);
                for (String item : listClikedItemsTmp) {
                    Log.i("onPageFinished", "CLICK ON SAVED : " + item);
                    // Refresh and redisplay everything which has been clicked
                    view.loadUrl("javascript:toggleVisible('" + item + "',false)");
                }
            }
            // Let's do this with style !
            if (!listStyleClikedItems.isEmpty()) {
                ArrayList<String> listStyleClikedItemsTmp = new ArrayList<>();
                listStyleClikedItemsTmp.addAll(listStyleClikedItems);
                for (String item : listStyleClikedItemsTmp) {
                    Log.i("onPageFinished", "STYLE CLICK ON SAVED : " + item);
                    // Refresh and redisplay everything which has been clicked
                    view.loadUrl("javascript:changeH2Style('" + item + "',false)");
                }
            }
            currentlyLoading = false;
        }
    }
}

