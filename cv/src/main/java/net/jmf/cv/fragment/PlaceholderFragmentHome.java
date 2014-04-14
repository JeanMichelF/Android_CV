package net.jmf.cv.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import net.jmf.cv.R;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Created by Jean-Mi on 26/03/2014.
 */
public class PlaceholderFragmentHome extends Fragment {

    private static final int YEAR_OF_BIRTH = 1979;
    private static final int MONTH_OF_BIRTH = Calendar.APRIL;
    private static final int DAY_OF_BIRTH = 16;

    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    protected static final String ARG_SECTION_NUMBER = "section_number";

    private Context context;
    private TextView textView;
    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static PlaceholderFragmentHome newInstance() {
        return new PlaceholderFragmentHome();
    }

    public PlaceholderFragmentHome() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_accueil, container, false);
        assert rootView != null;
        textView = (TextView) rootView.findViewById(R.id.section_label);
        Log.d("DEBUG", "Création de la vue " + Integer.toString(getArguments().getInt(ARG_SECTION_NUMBER)));
        return rootView;
    }

    /**
     *
     * @param savedInstanceState    Bundle
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        context = getActivity();
        if (savedInstanceState != null) {
            //edtMessage.setText(savedInstanceState.getString(EdtStorageKey));
        }
        textView.setText(Integer.toString(getMyAge()) + " " + context.getString(R.string.home_age));
        super.onActivityCreated(savedInstanceState);
    }

    /**
     *
     * @param outState  Bundle
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Sauvegarde des données du contexte utilisateur
        //outState.putInt("curChoice", mCurCheckPosition);
    }

    /**
     * Calculate my age in years
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
