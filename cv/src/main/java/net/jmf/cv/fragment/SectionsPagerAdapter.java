package net.jmf.cv.fragment;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import net.jmf.cv.MainActivity;
import net.jmf.cv.R;

import java.util.Locale;

/**
 * Created by Jean-Mi on 24/03/2014.
 */
public class SectionsPagerAdapter extends FragmentPagerAdapter {
    public SectionsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        // getItem is called to instantiate the fragment for the given page.
        // Return a PlaceholderFragment (defined as a static inner class below).
        return MainActivity.PlaceholderFragment.newInstance(position + 1);
    }

    @Override
    public int getCount() {
        // Show 3 total pages.
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        Locale l = Locale.getDefault();
        switch (position) {
            case 0:
                return getItem(position).getString(R.string.title_section1).toUpperCase(l);
            case 1:
                return getItem(position).getString(R.string.title_section2).toUpperCase(l);
            case 2:
                return getItem(position).getString(R.string.title_section3).toUpperCase(l);
        }
        return null;
        //return MainActivity.PlaceholderFragment.getTitle(position);
    }
}
