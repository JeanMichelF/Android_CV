package net.jmf.cv.fragment;

/**
 * Used to handle in the viewpager onPause and onResume
 * Created by Jean-Mi on 07/05/2014.
 */
public interface FragmentLifecycle {
    public void onPauseFragment();
    public void onResumeFragment();
}
