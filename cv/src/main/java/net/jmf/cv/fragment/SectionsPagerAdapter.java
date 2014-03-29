package net.jmf.cv.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import net.jmf.cv.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by Jean-Mi on 24/03/2014.
 */
public class SectionsPagerAdapter extends FragmentPagerAdapter {
    private List<Fragment> fragments;
    private List<String> titles;

    public SectionsPagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        Locale l = Locale.getDefault();
        this.fragments = new ArrayList<Fragment>();
        this.titles    = new ArrayList<String>();
        addItem(null, context.getString(R.string.title_section1).toUpperCase(l));
        addItem(context.getString(R.string.url_section2),context.getString(R.string.title_section2).toUpperCase(l));
        addItem(context.getString(R.string.url_section3),context.getString(R.string.title_section3).toUpperCase(l));
        addItem(context.getString(R.string.url_section4),context.getString(R.string.title_section4).toUpperCase(l));
        addItem(context.getString(R.string.url_section5),context.getString(R.string.title_section5).toUpperCase(l));
    }

    public void addItem(String url, String title) {
        Fragment myFragment;
        Bundle args = new Bundle();
        if (null == url) {
            myFragment = PlaceholderFragmentHome.newInstance();
            args.putInt(PlaceholderFragmentHome.ARG_SECTION_NUMBER, 1);
        } else {
            myFragment = PlaceholderFragmentWebView.newInstance();
            args.putString(PlaceholderFragmentWebView.ARG_SECTION_URL, url);
        }
        myFragment.setArguments(args);
        this.fragments.add(myFragment);
        this.titles.add(title);
    }

    @Override
    public Fragment getItem(int position) {
        return this.fragments.get(position);
    }

    public CharSequence getPageTitle(int position) {
        return this.titles.get(position);
    }

    @Override
    public int getCount() {
        return this.fragments.size();
    }

/*
    @Override
    public Fragment getItem(int position) {
        // getItem is called to instantiate the fragment for the given page.
        // Return a PlaceholderFragmentHome or a PlaceholderFragmentWebView (defined as a static inner class below).
        if (position == 0) {
            return PlaceholderFragmentHome.newInstance(position + 1);
        }
        return PlaceholderFragmentWebView.newInstance(position + 1);
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
                return context.getString(R.string.title_section1).toUpperCase(l);
            case 1:
                return context.getString(R.string.title_section2).toUpperCase(l);
            case 2:
                return context.getString(R.string.title_section3).toUpperCase(l);
        }
        return null;
    }
    */
}
