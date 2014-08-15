package net.jmf.cv.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import net.jmf.cv.R;

/**
 * Generic DialogFragment class
 * Handles YesNo and OK dialog types
 * Support color Customization
 */
public class CVDialogFragment extends DialogFragment implements DialogInterface.OnClickListener {
    /**
     * Type of the DialogFragment
     */
    public static final int TYPE_OK_DIALOG = 1;
    public static final int TYPE_YESNO_DIALOG = 2;
    /**
     * Handle customization
     */
    private static final int NO_CUSTOMIZATION = -1;
    /**
     * Listener for the buttons
     */
    private OnDialogOptionClickListener clickListenerPositive;
    private OnDialogOptionClickListener clickListenerNegative;

    /**
     * Default constructor
     */
    public CVDialogFragment() {
    }

    /**
     * Create a CVDialogFragment without customization
     *
     * @param title   title
     * @param message text
     * @param type    YesNo or OK dialog
     * @return CVDialogFragment
     */
    public static CVDialogFragment newInstance(String title, String message, int type) {
        return newInstance(title, message, type, NO_CUSTOMIZATION);
    }

    /**
     * Create a CVDialogFragment with color customization
     *
     * @param title      title
     * @param message    text
     * @param type       YesNo or OK dialog
     * @param colorStyle color (int) of the customization
     * @return CVDialogFragment
     */
    public static CVDialogFragment newInstance(String title, String message, int type, int colorStyle) {
        CVDialogFragment frag = new CVDialogFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        args.putString("message", message);
        args.putInt("type", type);
        args.putInt("colorStyle", colorStyle);
        frag.setArguments(args);
        return frag;
    }

    /**
     * Handler for a click on Button Positive
     *
     * @param clickListener positive clickListener
     */
    public void setOnDialogOptionClickListenerPositive(OnDialogOptionClickListener clickListener) {
        this.clickListenerPositive = clickListener;
    }

    /**
     * Handler for a click on Button Negative
     *
     * @param clickListener negative clickListener
     */
    public void setOnDialogOptionClickListenerNegative(OnDialogOptionClickListener clickListener) {
        this.clickListenerNegative = clickListener;
    }

    /**
     * Create the DialogFragment
     *
     * @param savedInstanceState bundle
     * @return Dialog
     */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle args = getArguments();
        String title = args.getString("title");
        String message = args.getString("message");
        int type = args.getInt("type", 1);

        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());

        alert.setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.yes, this);
        if (type == 2) {
            alert.setNegativeButton(R.string.annuler, this);
        }

        AlertDialog alertDialog = alert.create();

        /** Styling Buttons */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                      @Override
                      public void onShow(DialogInterface dialog) {
                          Button button = ((AlertDialog) getDialog()).getButton(AlertDialog.BUTTON_NEGATIVE);
                          if (null != button)
                            button.setBackgroundResource(R.drawable.cvtheme_item_background_holo_light);
                          button = ((AlertDialog) getDialog()).getButton(AlertDialog.BUTTON_POSITIVE);
                          button.setBackgroundResource(R.drawable.cvtheme_item_background_holo_light);
                      }
                  }
            );
        }
        return alertDialog;
    }

    /**
     * Handle clicks
     *
     * @param dialog dialog that received the click
     * @param which  button that was clicked
     */
    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (which == DialogInterface.BUTTON_POSITIVE) {
            if (this.clickListenerPositive != null) {
                this.clickListenerPositive.onDialogOptionPressed();
            }
        } else {
            if (this.clickListenerNegative != null) {
                this.clickListenerNegative.onDialogOptionPressed();
            }
        }
    }

    /**
     * This handle customization color
     */
    @Override
    public void onStart() {
        super.onStart();
        final Resources res = getResources();
        Bundle args = getArguments();
        int customizationColor = args.getInt("colorStyle", NO_CUSTOMIZATION);

        // We do not need this on pre ICS versions
        if (customizationColor != NO_CUSTOMIZATION && Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {

            // Title
            final int titleId = res.getIdentifier("alertTitle", "id", "android");
            final View title = getDialog().findViewById(titleId);
            if (title != null) {
                ((TextView) title).setTextColor(customizationColor);
            }

            // Title divider
            final int titleDividerId = res.getIdentifier("titleDivider", "id", "android");
            final View titleDivider = getDialog().findViewById(titleDividerId);
            if (titleDivider != null) {
                titleDivider.setBackgroundColor(customizationColor);
            }
        }
    }

    /**
     * Interface used for handling clicks buttons
     */
    public interface OnDialogOptionClickListener {
        void onDialogOptionPressed();
    }
}
