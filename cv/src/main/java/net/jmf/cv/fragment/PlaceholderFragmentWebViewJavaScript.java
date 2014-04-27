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
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Webview with JS
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
        setRetainInstance(true);
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

        // Get the list of saved items clicked before
        if (savedInstanceState != null) {
            listClikedItems = savedInstanceState.getStringArrayList(CLICKED_ITEMS);
            Log.i("onActivityCreated", "Récupération de la liste getListClikedItems " + listClikedItems.size());
            listStyleClikedItems = savedInstanceState.getStringArrayList(CLICKED_STYLE_ITEMS);
            Log.i("onActivityCreated", "Récupération de la liste listStyleClikedItems " + listStyleClikedItems.size());
        }

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

        super.webView.addJavascriptInterface(new WebAppInterface(this), "CallToAnAndroidFunction");

        // for debugging, this will handle the console.log() in javascript
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
     * Handle webview state when rotating (for example)
     * Only usefull for clicks in JS since site is fully loaded in cache
     *
     * @param outState Bundle
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Save all the clicked items
        Log.i("onSaveInstanceState", "Sauvegarde de la liste getListClikedItems " + getListClikedItems().size());
        outState.putStringArrayList(CLICKED_ITEMS, (ArrayList<String>) getListClikedItems());
        Log.i("onSaveInstanceState", "Sauvegarde de la liste getListClikedItems " + getListStyleClikedItems().size());
        outState.putStringArrayList(CLICKED_STYLE_ITEMS, (ArrayList<String>) getListStyleClikedItems());
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
         */
        @JavascriptInterface
        public void saveClick(String idDomElement) {
            Log.i("PlaceholderFragmentWebViewJavaScript", idDomElement + " clicked");
            // If it exist then delete instead of add
            if (!placeholderFragmentWebViewJavaScript.getListClikedItems().contains(idDomElement)) {
                placeholderFragmentWebViewJavaScript.getListClikedItems().add(idDomElement);
            } else {
                placeholderFragmentWebViewJavaScript.getListClikedItems().remove(idDomElement);
            }
        }

        /**
         * Save all clicks
         */
        @JavascriptInterface
        public void saveStyle(String idDomElement) {
            Log.i("PlaceholderFragmentWebViewJavaScript", idDomElement + " style clicked");
            // If it exist then delete instead of add
            if (!placeholderFragmentWebViewJavaScript.getListStyleClikedItems().contains(idDomElement)) {
                placeholderFragmentWebViewJavaScript.getListStyleClikedItems().add(idDomElement);
            } else {
                placeholderFragmentWebViewJavaScript.getListStyleClikedItems().remove(idDomElement);
            }
        }
    }

    private class MyBrowserJS extends MyBrowser {

        /**
         * Usefull in debug only
         *
         * @param view Webview
         * @param url  String
         */
        @Override
        public void onPageFinished(WebView view, String url) {
            Log.i("BROWSER JS", "Finish loading... " + url);

            // If there were some clicked items, let's reclick them as they were
            if (!listClikedItems.isEmpty()) {
                try {
                    // Do not execute 2 times sameid, so flush everything which is 2 times in the list
                    Set<String> set = new HashSet<>();
                    set.addAll(listClikedItems);
                    listClikedItems = new ArrayList<>(set);
                    for (String item : listClikedItems) {
                        Log.i("onPageFinished", "CLICK ON SAVED : " + item);
                        // Refresh and redisplay everything which has been clicked
                        view.loadUrl("javascript:toggleVisible('" + item + "')");
                    }
                } catch (ConcurrentModificationException e) {
                    Log.e("onPageFinished", "Ooops...");
                }
            }
            // Let's do this with style !
            if (!listStyleClikedItems.isEmpty()) {
                try {
                    // Do not execute 2 times sameid, so flush everything which is 2 times in the list
                    Set<String> set = new HashSet<>();
                    set.addAll(listStyleClikedItems);
                    listStyleClikedItems = new ArrayList<>(set);
                    for (String item : listStyleClikedItems) {
                        Log.i("onPageFinished", "STYLE CLICK ON SAVED : " + item);
                        // Refresh and redisplay everything which has been clicked
                        view.loadUrl("javascript:changeH2Style('" + item + "')");
                    }
                } catch (ConcurrentModificationException e) {
                    Log.e("onPageFinished", "Ooops...");
                }
            }
        }
    }
}

