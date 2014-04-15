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

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Created by Jean-Mi on 26/03/2014.
 */
public class PlaceholderFragmentHome extends Fragment {

    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    protected static final String ARG_SECTION_NUMBER = "section_number";
    private static final int YEAR_OF_BIRTH = 1979;
    private static final int MONTH_OF_BIRTH = Calendar.APRIL;
    private static final int DAY_OF_BIRTH = 16;
    private Context context;
    private TextView textViewAge;
    private TextView textViewSituationMaritale;
    private TextView textViewPermis;
    private TextView textViewPresentation;
    private TextView textViewGithub;
    private TextView textViewEmail;
    private TextView textViewSituationProfessionnelle;
    private TextView textViewSituationCarriere;

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
        Log.d("DEBUG", "Création de la vue " + Integer.toString(getArguments().getInt(ARG_SECTION_NUMBER)));
        /*webView = (WebView) rootView.findViewById(R.id.presentation);
        String text = "<html><body>"
        + "<p align=\"justify\">"
        + getString(R.string.home_presentation)
        + "</p> "
        + "</body></html>";
        webView.loadData(text, "text/html", "utf-8");*/
        return rootView;
    }

    /**
     * @param savedInstanceState Bundle
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        context = getActivity();
        if (savedInstanceState != null) {
            //edtMessage.setText(savedInstanceState.getString(EdtStorageKey));
        }
        textViewAge.setText(Integer.toString(getMyAge()) + " " + context.getString(R.string.home_age));

        // Disabling link in Android previous to ICS : make the app crash when no email app is created
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
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
        super.onActivityCreated(savedInstanceState);
    }

    /**
     * @param outState Bundle
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Sauvegarde des données du contexte utilisateur
        //outState.putInt("curChoice", mCurCheckPosition);
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
}
