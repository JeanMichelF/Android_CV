package net.jmf.cv.fragment;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.style.UnderlineSpan;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import net.jmf.cv.MyCVActivity;
import net.jmf.cv.R;
import net.jmf.cv.service.NetworkHttpHandler;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * Home Fragment
 * Created by Jean-Mi on 26/03/2014.
 */
public class PlaceholderFragmentHome extends Fragment implements FragmentLifecycle {

    private static final int YEAR_OF_BIRTH = 1979;
    private static final int MONTH_OF_BIRTH = Calendar.APRIL;
    private static final int DAY_OF_BIRTH = 16;
    private static final String TEXTVIEW_AGE_VALUE = "age";
    private final String CACHE_FILE_NAME = "homedatas.json";
    private Context context;
    private TextView textViewAge;
    private TextView textViewSituationMaritale;
    private TextView textViewPermis;
    private TextView textViewCity;
    private TextView textViewPresentation;
    private TextView textViewGithub;
    private TextView textViewEmail;
    private TextView textViewSituationProfessionnelle;
    private TextView textViewSituationCarriere;
    private TextView textViewPresentationTitle;
    private TextView textViewLinkTitle;
    private TextView textViewSituationProfessionnelleTitle;
    private Bundle savePause;
    private boolean canHandleEmailIntent = false;

    public PlaceholderFragmentHome() {
        setRetainInstance(true);
    }

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static PlaceholderFragmentHome newInstance() {
        return new PlaceholderFragmentHome();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_accueil, container, false);
        assert rootView != null;
        textViewAge = (TextView) rootView.findViewById(R.id.age);
        textViewSituationMaritale = (TextView) rootView.findViewById(R.id.situation_maritale);
        textViewCity =  (TextView) rootView.findViewById(R.id.city);
        textViewPresentation = (TextView) rootView.findViewById(R.id.presentation);
        textViewGithub = (TextView) rootView.findViewById(R.id.github);
        textViewEmail = (TextView) rootView.findViewById(R.id.email);
        textViewSituationProfessionnelle = (TextView) rootView.findViewById(R.id.situation_professionnelle);
        textViewSituationCarriere = (TextView) rootView.findViewById(R.id.situation_carriere);
        textViewPermis = (TextView) rootView.findViewById(R.id.permis);
        textViewPresentationTitle = (TextView) rootView.findViewById(R.id.presentation_title);
        textViewLinkTitle = (TextView) rootView.findViewById(R.id.surleweb_title);
        textViewSituationProfessionnelleTitle = (TextView) rootView.findViewById(R.id.situation_professionnelle_title);
        MyCVActivity.d("DEBUG_MODE", "Création de la vue home");
        return rootView;
    }

    /**
     * Load from bundle if it exists, otherwise go check online/in cache
     *
     * @param savedInstanceState Bundle
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        context = getActivity();

        //We must style everytime : Android does not handle this well (but text in TextView, yes)

        //Capitalize all letters for titles in Android previous to ICS : make the app consistent with Webviews
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            List<TextView> titlesView = Arrays.asList(textViewPresentationTitle, textViewLinkTitle, textViewSituationProfessionnelleTitle);
            for (TextView textView : titlesView) {
                textView.setText(textView.getText().toString().toUpperCase());
            }
        }

        // Disabling link in Android when no Intent handler is Available : make the app crash when no email app is created
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                "mailto", textViewEmail.getText().toString(), null));
        canHandleEmailIntent = MyCVActivity.isAvailable(context, emailIntent);
        if (canHandleEmailIntent) {
            textViewEmail.setAutoLinkMask(Linkify.EMAIL_ADDRESSES);
            textViewEmail.setText(R.string.home_email);
        } else {
            textViewEmail.setTextColor(textViewEmail.getLinkTextColors().getDefaultColor());
            //textViewEmail.setPaintFlags(Paint.UNDERLINE_TEXT_FLAG);
            textViewEmail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(context, R.string.error_email_app_absent, Toast.LENGTH_LONG).show();
                }
            });
            SpannableString spanString = new SpannableString(getResources().getString(R.string.home_email));
            spanString.setSpan(new UnderlineSpan(), 0, spanString.length(), 0);
            textViewEmail.setText(spanString);
        }


        // URL for Github clickable
        textViewGithub.setText(Html.fromHtml(getResources().getString(R.string.home_github)));
        textViewGithub.setMovementMethod(LinkMovementMethod.getInstance());

        // If we run this for the first time, let's try to get refresh datas
        if (null == savedInstanceState && null == savePause) {
            MyCVActivity.d("HOME FRAGMENT", "Full initialization");
            textViewAge.setText(Integer.toString(getMyAge()) + " " + context.getString(R.string.home_age));
            // Overriding if possible datas from Strings.xml with some on network or in cache
            loadDataPresentation();
        } else {
            MyCVActivity.d("HOME FRAGMENT", "No full initialization : ");
            if (null != savedInstanceState) {
                MyCVActivity.d("HOME FRAGMENT", " savedInstanceState " + savedInstanceState.isEmpty());
                // Load Age
                if (null != savedInstanceState.getCharSequence(TEXTVIEW_AGE_VALUE)) {
                    textViewAge.setText(savedInstanceState.getCharSequence(TEXTVIEW_AGE_VALUE));
                }
            }
            if (null != savePause) {
                MyCVActivity.d("HOME FRAGMENT", " savePause " + savePause.isEmpty());
                // Load Age
                if (null != savePause.getCharSequence(TEXTVIEW_AGE_VALUE)) {
                    textViewAge.setText(savePause.getCharSequence(TEXTVIEW_AGE_VALUE));
                }
            }
            // Overriding if possible datas from Strings.xml with some on cache
            // Usefull when rotating before completion of web call : result of webcall is in cache
            loadFromCache();
        }
        super.onActivityCreated(savedInstanceState);
    }

    /**
     * Save datas from TextViews : Android handles this natively, but not quite good with Age
     *
     * @param outState Bundle
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putCharSequence(TEXTVIEW_AGE_VALUE, textViewAge.getText());
    }

    /**
     * Calculate my age in years
     *
     * @return int My age
     */
    private int getMyAge() {

        GregorianCalendar cal = new GregorianCalendar();
        int y, m, d, a;

        y = cal.get(Calendar.YEAR);
        m = cal.get(Calendar.MONTH);
        d = cal.get(Calendar.DAY_OF_MONTH);
        cal.set(YEAR_OF_BIRTH, MONTH_OF_BIRTH, DAY_OF_BIRTH);
        a = y - cal.get(Calendar.YEAR);
        if ((m < cal.get(Calendar.MONTH))
                || ((m == cal.get(Calendar.MONTH)) && (d < cal
                .get(Calendar.DAY_OF_MONTH)))) {
            --a;
        }

        return a;
    }

    /**
     * Load data from an URL
     */
    private void loadFromWeb() {
        MyCVActivity.d("loadFromWeb", "Load from Web");
        new NetworkHttpHandler() {

            @Override
            public HttpUriRequest getHttpRequestMethod() {
                return new HttpGet(getResources().getString(R.string.url_section1));
            }

            @Override
            public void onResponse(String result) {
                // Create a JSON object from the request response
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    MyCVActivity.d("loadFromWeb", "Load from Web result " + result);
                    saveIntoCache(jsonObject);
                    handleDataPresentation(jsonObject);
                } catch (JSONException e) {
                    MyCVActivity.i("loadFromWeb", "Could not convert into JSONObject " + e.getMessage());
                    loadFromCache();
                }
            }

            @Override
            public void onCancel(String result) {
                loadFromCache();
            }
        }.execute();
    }


    /**
     * Load data from cache
     */
    private void loadFromCache() {
        MyCVActivity.d("loadFromCache", "Load from Cache");
        new AsyncLoadFromCache().execute();
    }

    private class AsyncLoadFromCache extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... params) {
            return getFileContent();
        }

        @Override
        protected void onPostExecute(String text) {
            super.onPostExecute(text);
            JSONObject jsonObject = null;
            try {
                jsonObject = new JSONObject(text);
            } catch (JSONException e) {
                MyCVActivity.e("loadFromCache", "Error loading file " + e.getMessage(), e);
            }
            handleDataPresentation(jsonObject);
        }

        private String getFileContent() {
            /** Reading contents of the temporary file, if already exists */
            FileReader fReader = null;
            String strLine;
            StringBuilder text = new StringBuilder();
            try {
                File tempFile = new File(context.getCacheDir().getPath() + "/" + CACHE_FILE_NAME);
                fReader = new FileReader(tempFile);
                BufferedReader bReader = new BufferedReader(fReader);
                while ((strLine = bReader.readLine()) != null) {
                    text.append(strLine);
                }
            } catch (IOException | NullPointerException e) {
                MyCVActivity.e("loadFromCache", "Error loading file " + e.getMessage(), e);
            } finally {
                try {
                    if (fReader != null) {
                        fReader.close();
                    }
                } catch (IOException io) {
                    MyCVActivity.e("loadFromCache", "Error closing file " + io.getMessage(), io);
                }
            }
            return text.toString();
        }
    }


    /**
     * Save data into cache
     */
    private void saveIntoCache(Object object) {
        new AsyncSaveIntoCache().execute(object);
    }

    private class AsyncSaveIntoCache extends AsyncTask<Object, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Object... objects) {
            return saveFileContent(objects);
        }

        private boolean saveFileContent(Object... objects) {
            Boolean retour = false;
            /** Create a tempfile and save it into app cache */
            FileWriter writer = null;
            try {
                File tempFile = new File(context.getCacheDir().getPath() + "/" + CACHE_FILE_NAME);
                writer = new FileWriter(tempFile);
                Object object = objects[0];
                writer.write(object.toString());
                retour = true;
            } catch (IOException | NullPointerException e) {
                MyCVActivity.i("saveIntoCache", "Error saving file " + e.getMessage());
            } finally {
                try {
                    if (writer != null) {
                        writer.close();
                    }
                } catch (IOException io) {
                    MyCVActivity.e("saveIntoCache", "Error closing file " + io.getMessage(), io);
                }
            }
            return retour;
        }
    }

    /**
     * Handle loading of datas through network then cache
     * In fact, I could have added the URL to load into site.manifest and let WebView handle this through ViewPager setOffscreenPageLimit
     * but hey, it's a learning/experimental project !
     */
    private void loadDataPresentation() {
        loadFromWeb();
    }

    /**
     * Update textViews with data provided by network or cache
     */
    private void handleDataPresentation(JSONObject datas) {
        if (null != datas) {
            try {
                textViewSituationMaritale.setText(datas.getString("situationMaritale"));
            } catch (JSONException e) {
                MyCVActivity.i("handleDataPresentation", "Could not update situationMaritale " + e.getMessage());
            }
            try {
                textViewPresentation.setText(datas.getString("presentation"));
            } catch (JSONException e) {
                MyCVActivity.i("handleDataPresentation", "Could not update presentation " + e.getMessage());
            }
            /* Not yet
            try {
                textViewGithub.setText(datas.getString("urlGithub"));
            } catch (JSONException e) {
                MyCVActivity.i("handleDataPresentation","Could not update urlGithub " + e.getMessage());
            }*/
            try {
                if (canHandleEmailIntent) {
                    textViewEmail.setText(datas.getString("email"));
                } else {
                    SpannableString spanString = new SpannableString(getResources().getString(R.string.home_email));
                    spanString.setSpan(new UnderlineSpan(), 0, spanString.length(), 0);
                    textViewEmail.setText(spanString);
                }
            } catch (JSONException e) {
                MyCVActivity.i("handleDataPresentation", "Could not update email " + e.getMessage());
            }
            try {
                textViewSituationProfessionnelle.setText(datas.getString("situationProfessionnelle"));
            } catch (JSONException e) {
                MyCVActivity.i("handleDataPresentation", "Could not update situationProfessionnelle " + e.getMessage());
            }
            try {
                textViewSituationCarriere.setText(datas.getString("situationCarriere"));
            } catch (JSONException e) {
                MyCVActivity.i("handleDataPresentation", "Could not update situationCarriere " + e.getMessage());
            }
            try {
                textViewPermis.setText(datas.getString("permis"));
            } catch (JSONException e) {
                MyCVActivity.i("handleDataPresentation", "Could not update permis " + e.getMessage());
            }
            /* Not yet
            try {
                textViewCity.setText(datas.getString("ville"));
            } catch (JSONException e) {
                MyCVActivity.i("handleDataPresentation","Could not update ville " + e.getMessage());
            }*/
        }
    }

    /**
     * Make a save of a bundle like onSaveInstanceState in order to have one and only one call to web
     */
    @Override
    public void onPauseFragment() {
        MyCVActivity.d("HOME FRAGMENT", "onPauseFragment");
        savePause = new Bundle();
        savePause.putCharSequence(TEXTVIEW_AGE_VALUE, textViewAge.getText());
    }

    /**
     * Since onSaveInstanceState is not very well called with Fragment, we're using a call to onPauseFragment
     * to save a bundle (strange, but setRetainInstance does not fully work as expected : fragment is recreated)
     */
    @Override
    public void onPause() {
        onPauseFragment();
        super.onPause();
    }

    /**
     * Could be use when home is redisplayed (not usefull yet)
     */
    @Override
    public void onResumeFragment() {

    }
}
