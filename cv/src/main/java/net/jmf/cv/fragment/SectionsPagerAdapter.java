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
 * Section Pager for swipables fragments
 * Created by Jean-Mi on 24/03/2014.
 */
public class SectionsPagerAdapter extends FragmentPagerAdapter {
    private final List<Fragment> fragments;
    private final List<String> titles;

    public SectionsPagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        Locale l = Locale.getDefault();
        this.fragments = new ArrayList<>();
        this.titles = new ArrayList<>();
        addItem(null, context.getString(R.string.title_section1).toUpperCase(l), false);
        addItem(context.getString(R.string.url_section2), context.getString(R.string.title_section2).toUpperCase(l), true);
        addItem(context.getString(R.string.url_section3), context.getString(R.string.title_section3).toUpperCase(l), false);
        addItem(context.getString(R.string.url_section4), context.getString(R.string.title_section4).toUpperCase(l), false);
        addItem(context.getString(R.string.url_section5), context.getString(R.string.title_section5).toUpperCase(l), false);
    }

    void addItem(String url, String title, boolean withJS) {
        Fragment myFragment;
        Bundle args = new Bundle();
        if (null == url) {
            myFragment = PlaceholderFragmentHome.newInstance();
        } else {
            if (withJS) {
                myFragment = PlaceholderFragmentWebViewJavaScript.newInstance();
                args.putString(PlaceholderFragmentWebView.ARG_SECTION_URL, url);
            } else {
                myFragment = PlaceholderFragmentWebView.newInstance();
                args.putString(PlaceholderFragmentWebView.ARG_SECTION_URL, url);
            }
        }
        myFragment.setArguments(args);
        this.fragments.add(myFragment);
        this.titles.add(title);
    }

    @Override
    public Fragment getItem(int position) {
        return this.fragments.get(position);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return this.titles.get(position);
    }

    @Override
    public int getCount() {
        return this.fragments.size();
    }
}
