package net.jmf.cv;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import net.jmf.cv.fragment.CVDialogFragment;
import net.jmf.cv.fragment.FragmentLifecycle;
import net.jmf.cv.fragment.SectionsPagerAdapter;
import net.jmf.cv.view.SlidingTabLayout;

import java.util.ArrayList;
import java.util.List;

public class MyCVActivity extends ActionBarActivity implements ActionBar.TabListener {

    /**
     * Usefull for displaying logs only in Debug
     */
    public static final boolean SHOW_LOG = BuildConfig.DEBUG;

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * A custom {@link ViewPager} title strip which looks much like Tabs present in Android v4.0 and
     * above, but is designed to give continuous feedback to the user when scrolling.
     */
    SlidingTabLayout mSlidingTabLayout;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    /**
     * Check if a Context is Available before sending it
     *
     * @param ctx    Context
     * @param intent Intent
     * @return bool
     */
    public static boolean isAvailable(Context ctx, Intent intent) {
        final PackageManager mgr = ctx.getPackageManager();
        List<ResolveInfo> list = mgr.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }

    /**
     * Change blue color to another color when hitting top or bottom of a scrollview
     * http://evendanan.net/android/branding/2013/12/09/branding-edge-effect/
     *
     * @param context    Context
     * @param brandColor Color
     */
    public static void brandGlowEffect(Context context, int brandColor) {
        try {
            //glow
            int glowDrawableId = context.getResources().getIdentifier("overscroll_glow", "drawable", "android");
            Drawable androidGlow = context.getResources().getDrawable(glowDrawableId);
            androidGlow.setColorFilter(brandColor, PorterDuff.Mode.SRC_IN);
            //edge
            int edgeDrawableId = context.getResources().getIdentifier("overscroll_edge", "drawable", "android");
            Drawable androidEdge = context.getResources().getDrawable(edgeDrawableId);
            androidEdge.setColorFilter(brandColor, PorterDuff.Mode.SRC_IN);
        } catch (Exception e) {
            MyCVActivity.d("GlowEffect", "Seems that this device doesn't like this effect !");
        }
    }

    /**
     * @param savedInstanceState Bundle
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Seems that the hacky way to change the color doesn't work well with Kitkat : might be because of virtual device...
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            MyCVActivity.brandGlowEffect(getBaseContext(), getResources().getColor(R.color.cvtheme_color));
        }

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager(), getBaseContext());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // Give the SlidingTabLayout the ViewPager, this must be done AFTER the ViewPager has had
        // it's PagerAdapter set.
        mSlidingTabLayout = (SlidingTabLayout) findViewById(R.id.sliding_tabs);
        mSlidingTabLayout.setSelectedIndicatorColors(getResources().getColor(R.color.cvtheme_color));
        mSlidingTabLayout.setViewPager(mViewPager);

        mViewPager.setOffscreenPageLimit(5);

        /**
         * Add a pagechangerlistener to call "onResumeFragment" and "onPauseFragment" when user select a new fragment
         * This allow to fake onResume on fragment when fragment is visible
         */
        mSlidingTabLayout.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            int currentPosition = 0;

            @Override
            public void onPageSelected(int newPosition) {
                // This is a very hacky way to get the good fragment, but using only "getPosition" get a non initialized fragment, or we need at least a view...
                FragmentLifecycle frgToResume = (FragmentLifecycle) getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.pager + ":" + newPosition);
                if (null != frgToResume) {
                    frgToResume.onResumeFragment();
                }
                FragmentLifecycle frgToPause = (FragmentLifecycle) getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.pager + ":" + currentPosition);
                if (null != frgToPause) {
                    frgToPause.onPauseFragment();
                }

                currentPosition = newPosition;
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
            }

            public void onPageScrollStateChanged(int arg0) {
            }
        });
    }

    /**
     * @param menu Menu
     * @return bool
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    /**
     * @param item MenuItem
     * @return bool
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case R.id.menu_add_contact:
                final MenuItem itemAddContact = item;
                final CVDialogFragment dialog = CVDialogFragment.newInstance(
                        getResources().getString(R.string.action_addme),
                        getResources().getString(R.string.action_addme_text),
                        CVDialogFragment.TYPE_YESNO_DIALOG,
                        getResources().getColor(R.color.cvtheme_color));
                dialog.setOnDialogOptionClickListenerNegative(new CVDialogFragment.OnDialogOptionClickListener() {
                    @Override
                    public void onDialogOptionPressed() {
                        dialog.dismiss();
                    }
                });
                dialog.setOnDialogOptionClickListenerPositive(new CVDialogFragment.OnDialogOptionClickListener() {
                    @Override
                    public void onDialogOptionPressed() {
                        if (createNewContact()) {
                            itemAddContact.setVisible(false);
                            itemAddContact.setEnabled(false);
                        }
                    }
                });
                dialog.show(getSupportFragmentManager(), "AddMeContactTag");

                return true;

            case R.id.action_about:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * @param tab                 ActionBar.Tab
     * @param fragmentTransaction FragmentTransaction
     */
    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
    }

    /**
     * @param tab                 ActionBar.Tab
     * @param fragmentTransaction FragmentTransaction
     */
    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    /**
     * @param tab                 ActionBar.Tab
     * @param fragmentTransaction FragmentTransaction
     */
    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    /**
     * Create a new contact : me
     *
     * @return boolean success
     */
    private boolean createNewContact() {
        boolean retour = false;

        ArrayList<ContentProviderOperation> ops = new ArrayList<>();

        ops.add(ContentProviderOperation
                .newInsert(ContactsContract.RawContacts.CONTENT_URI)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                .build());

        // ------------------------------------------------------ Names
        ops.add(ContentProviderOperation
                .newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(
                        ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                .withValue(
                        ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME,
                        getResources().getString(R.string.action_addme_prenom) + " "
                                + getResources().getString(R.string.action_addme_nom)).build());

        // ------------------------------------------------------ Email
        ops.add(ContentProviderOperation
                .newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(
                        ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(
                        ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Email.DATA,
                        getResources().getString(R.string.action_addme_email))
                .withValue(ContactsContract.CommonDataKinds.Email.TYPE,
                        ContactsContract.CommonDataKinds.Email.TYPE_WORK)
                .build());

        // ------------------------------------------------------ Organization
        ops.add(ContentProviderOperation
                .newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(
                        ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(
                        ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE)
                .withValue(
                        ContactsContract.CommonDataKinds.Organization.TITLE,
                        getResources().getString(R.string.action_addme_profession))
                .withValue(
                        ContactsContract.CommonDataKinds.Organization.TYPE,
                        ContactsContract.CommonDataKinds.Organization.TYPE_WORK)
                .build());

        // Asking the Contact provider to create a new contact
        try {
            getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
            Toast.makeText(this, getResources().getString(R.string.action_addme_success),
                    Toast.LENGTH_SHORT).show();
            retour = true;
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, getResources().getString(R.string.action_addme_error),
                    Toast.LENGTH_SHORT).show();
        }
        return retour;
    }

    /**
     * Over ride Log methods : log only in debug
     *
     * @param tag   Tag
     * @param msg   Message
     */
    public static void d(final String tag, final String msg) {
        if (SHOW_LOG) {
            Log.d(tag, msg);
        }
    }

    /**
     * Over ride Log methods : log only in debug, even for info logs
     *
     * @param tag   Tag
     * @param msg   Message
     */
    public static void i(final String tag, final String msg) {
        if (SHOW_LOG) {
            Log.i(tag, msg);
        }
    }

}
