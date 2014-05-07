package net.jmf.cv.fragment;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
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
    private TextView textViewPresentation;
    private TextView textViewGithub;
    private TextView textViewEmail;
    private TextView textViewSituationProfessionnelle;
    private TextView textViewSituationCarriere;
    private TextView textViewPresentationTitle;
    private TextView textViewLinkTitle;
    private TextView textViewSituationProfessionnelleTitle;

    public PlaceholderFragmentHome() {
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
        textViewPresentation = (TextView) rootView.findViewById(R.id.presentation);
        textViewGithub = (TextView) rootView.findViewById(R.id.github);
        textViewEmail = (TextView) rootView.findViewById(R.id.email);
        textViewSituationProfessionnelle = (TextView) rootView.findViewById(R.id.situation_professionnelle);
        textViewSituationCarriere = (TextView) rootView.findViewById(R.id.situation_carriere);
        textViewPermis = (TextView) rootView.findViewById(R.id.permis);
        textViewPresentationTitle = (TextView) rootView.findViewById(R.id.presentation_title);
        textViewLinkTitle = (TextView) rootView.findViewById(R.id.surleweb_title);
        textViewSituationProfessionnelleTitle = (TextView) rootView.findViewById(R.id.situation_professionnelle_title);
        Log.d("DEBUG", "Création de la vue home");
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

        // Disabling link in Android previous to ICS : make the app crash when no email app is created
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            textViewEmail.setTextColor(textViewEmail.getLinkTextColors().getDefaultColor());
            //textViewEmail.setPaintFlags(Paint.UNDERLINE_TEXT_FLAG);
            textViewEmail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                            "mailto", textViewEmail.getText().toString(), null));
                    if (MyCVActivity.isAvailable(context, emailIntent)) {
                        startActivity(Intent.createChooser(emailIntent, "Send email..."));
                    } else {
                        Toast.makeText(context, R.string.error_email_app_absent, Toast.LENGTH_LONG).show();
                    }
                }
            });
        } else {
            textViewEmail.setAutoLinkMask(Linkify.EMAIL_ADDRESSES);
        }
        textViewEmail.setText(R.string.home_email);
        // URL for Github clickable
        textViewGithub.setText(Html.fromHtml(getResources().getString(R.string.home_github)));
        textViewGithub.setMovementMethod(LinkMovementMethod.getInstance());

        // If we run this for the first time, let's try to get refresh datas
        if (savedInstanceState == null) {
            Log.d("HOME FRAGMENT", "Full initialization");
            textViewAge.setText(Integer.toString(getMyAge()) + " " + context.getString(R.string.home_age));
            // Overriding if possible datas from Strings.xml with some on network or in cache
            loadDataPresentation();
        } else {
            // Load Age
            if (null != savedInstanceState.getCharSequence(TEXTVIEW_AGE_VALUE)) {
                textViewAge.setText(savedInstanceState.getCharSequence(TEXTVIEW_AGE_VALUE));
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
        Log.d("loadFromWeb", "Load from Web");
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
                    Log.d("loadFromWeb", "Load from Web result " + result);
                    saveIntoCache(jsonObject);
                    handleDataPresentation(jsonObject);
                } catch (JSONException e) {
                    Log.e("loadFromWeb", "Could not convert into JSONObject " + e.getMessage());
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
        Log.d("loadFromCache", "Load from Cache");
        /** Reading contents of the temporary file, if already exists */
        try {
            File tempFile = new File(context.getCacheDir().getPath() + "/" + CACHE_FILE_NAME);
            FileReader fReader = new FileReader(tempFile);
            BufferedReader bReader = new BufferedReader(fReader);
            StringBuilder text = new StringBuilder();
            String strLine;
            while ((strLine = bReader.readLine()) != null) {
                text.append(strLine);
            }
            JSONObject jsonObject = new JSONObject(text.toString());
            handleDataPresentation(jsonObject);
        } catch (IOException | NullPointerException | JSONException e) {
            Log.d("loadFromCache", "Error loading file " + e.getMessage());
        }
    }

    /**
     * Save data into cache
     */
    private void saveIntoCache(Object object) {
        /** Create a tempfile and save it into app cache */
        try {
            File tempFile = new File(context.getCacheDir().getPath() + "/" + CACHE_FILE_NAME);
            FileWriter writer = new FileWriter(tempFile);
            writer.write(object.toString());
            writer.close();
        } catch (IOException | NullPointerException e) {
            Log.d("saveIntoCache", "Error saving file " + e.getMessage());
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
                Log.e("handleDataPresentation", "Could not update situationMaritale " + e.getMessage());
            }
            try {
                textViewPresentation.setText(datas.getString("presentation"));
            } catch (JSONException e) {
                Log.e("handleDataPresentation", "Could not update presentation " + e.getMessage());
            }
            /* Not yet
            try {
                textViewGithub.setText(datas.getString("urlGithub"));
            } catch (JSONException e) {
                Log.e("handleDataPresentation","Could not update urlGithub " + e.getMessage());
            }*/
            try {
                textViewEmail.setText(datas.getString("email"));
            } catch (JSONException e) {
                Log.e("handleDataPresentation", "Could not update email " + e.getMessage());
            }
            try {
                textViewSituationProfessionnelle.setText(datas.getString("situationProfessionnelle"));
            } catch (JSONException e) {
                Log.e("handleDataPresentation", "Could not update situationProfessionnelle " + e.getMessage());
            }
            try {
                textViewSituationCarriere.setText(datas.getString("situationCarriere"));
            } catch (JSONException e) {
                Log.e("handleDataPresentation", "Could not update situationCarriere " + e.getMessage());
            }
            try {
                textViewPermis.setText(datas.getString("permis"));
            } catch (JSONException e) {
                Log.e("handleDataPresentation", "Could not update permis " + e.getMessage());
            }
            /* Not yet
            try {
                textViewVille.setText(datas.getString("ville"));
            } catch (JSONException e) {
                Log.e("handleDataPresentation","Could not update ville " + e.getMessage());
            }*/
        }
    }

    @Override
    public void onPauseFragment() {
        
    }

    @Override
    public void onResumeFragment() {

    }
}
