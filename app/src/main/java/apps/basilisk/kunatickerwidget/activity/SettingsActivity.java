package apps.basilisk.kunatickerwidget.activity;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Base64;
import android.view.MenuItem;

import java.util.List;

import apps.basilisk.kunatickerwidget.R;
import apps.basilisk.kunatickerwidget.Session;
import apps.basilisk.kunatickerwidget.tools.Enigma;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends AppCompatPreferenceActivity {
    private final String TAG = "SettingsActivity";

    public static final String APP_PREF_SWITCH_SORT_PRICE = "switch_sort_price";
    public static final String APP_PREF_SWITCH_MARKET_UAH = "switch_market_uah";
    public static final String APP_PREF_SWITCH_PRICE_USD = "switch_price_usd";
    public static final String APP_PREF_RATE_USDUAH = "rate_usduah";

    public static final String APP_PREF_SWITCH_OBSERVER = "switch_observer";
    public static final String APP_PREF_SWITCH_OBSERVER_PRICE = "switch_observer_price";
    public static final String APP_PREF_SWITCH_OBSERVER_ORDER = "switch_observer_order";
    public static final String APP_PREF_OBSERVER_FREQUENCY = "observer_frequency";

    public static final String APP_PREF_NOTIFICATIONS_ALERT = "notifications_alert";
    public static final String APP_PREF_NOTIFICATIONS_SOUND = "notifications_sound";
    public static final String APP_PREF_NOTIFICATIONS_RINGTONE = "notifications_ringtone";
    public static final String APP_PREF_NOTIFICATIONS_VIBRATE = "notifications_vibrate";
    public static final String APP_PREF_NOTIFICATIONS_LED = "notifications_led";

    public static final String APP_PREF_KEY_REMOVAL = "key_removal";
    public static final String APP_PREF_KEY_PRIVATE_API = "key_private_api";

    public static final String EXTRA_KEY = "key";
    public static final String EXTRA_KEY_NAME = "KEY_NAME";
    public static final int REQUEST_CODE_KEY = 777;

    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();

            if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                // Set the summary to reflect the new value.
                preference.setSummary(
                        index >= 0
                                ? listPreference.getEntries()[index]
                                : null);

            } else if (preference instanceof RingtonePreference) {
                // For ringtone preferences, look up the correct display value
                // using RingtoneManager.
                if (TextUtils.isEmpty(stringValue)) {
                    // Empty values correspond to 'silent' (no ringtone).
                    //preference.setSummary(R.string.pref_ringtone_silent);
                    preference.setSummary("Silent");

                } else {
                    Ringtone ringtone = RingtoneManager.getRingtone(
                            preference.getContext(), Uri.parse(stringValue));

                    if (ringtone == null) {
                        // Clear the summary if there was a lookup error.
                        preference.setSummary(null);
                    } else {
                        // Set the summary to reflect the new ringtone display
                        // name.
                        String name = ringtone.getTitle(preference.getContext());
                        preference.setSummary(name);
                    }
                }

            } else {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                preference.setSummary(stringValue);
            }

            return true;
        }
    };

    /**
     * Helper method to determine if the device has an extra-large screen. For
     * example, 10" tablets are extra-large.
     */
    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     *
     * @see #sBindPreferenceSummaryToValueListener
     */
    private static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActionBar();
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            if (!super.onMenuItemSelected(featureId, item)) {
                NavUtils.navigateUpFromSameTask(this);
            }
            return true;
        }
        return super.onMenuItemSelected(featureId, item);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.pref_headers, target);
    }

    /**
     * This method stops fragment injection in malicious applications.
     * Make sure to deny any unknown fragments here.
     */
    protected boolean isValidFragment(String fragmentName) {
        return PreferenceFragment.class.getName().equals(fragmentName)
                || GeneralPreferenceFragment.class.getName().equals(fragmentName)
                || ObserverPreferenceFragment.class.getName().equals(fragmentName)
                || NotificationPreferenceFragment.class.getName().equals(fragmentName)
                || KeysPreferenceFragment.class.getName().equals(fragmentName);
    }

    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class GeneralPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_general);
            setHasOptionsMenu(true);

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            bindPreferenceSummaryToValue(findPreference("rate_usduah"));
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                //startActivity(new Intent(getActivity(), SettingsActivity.class));
                getActivity().finish();
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     * This fragment shows Watcher preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class ObserverPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_observer);
            setHasOptionsMenu(true);

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            bindPreferenceSummaryToValue(findPreference("observer_frequency"));
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                //startActivity(new Intent(getActivity(), SettingsActivity.class));
                getActivity().finish();
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     * This fragment shows notification preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class NotificationPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_notification);
            setHasOptionsMenu(true);

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            bindPreferenceSummaryToValue(findPreference("notifications_ringtone"));
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                //startActivity(new Intent(getActivity(), SettingsActivity.class));
                getActivity().finish();
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     * This fragment shows keys preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class KeysPreferenceFragment extends PreferenceFragment implements Preference.OnPreferenceClickListener {
        Preference textPublicKey;
        Preference textPrivateKey;
        Preference keyRemoval;
        Preference preference;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_keys);
            setHasOptionsMenu(true);

            textPublicKey = findPreference(Session.APP_PREF_PUBLIC_KEY);
            textPublicKey.setOnPreferenceClickListener(this);
            bindPreferenceSummaryToValue(textPublicKey);

            textPrivateKey = findPreference(Session.APP_PREF_PRIVATE_KEY);
            textPrivateKey.setOnPreferenceClickListener(this);
            bindPreferenceSummaryToValue(textPrivateKey);

            textPublicKey.setEnabled(Session.getInstance().isPrivateApiPaid());
            textPrivateKey.setEnabled(Session.getInstance().isPrivateApiPaid());

            keyRemoval = findPreference(APP_PREF_KEY_REMOVAL);
            keyRemoval.setOnPreferenceClickListener(this);
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                //startActivity(new Intent(getActivity(), SettingsActivity.class));
                getActivity().finish();
                return true;
            }
            return super.onOptionsItemSelected(item);
        }

        @Override
        public boolean onPreferenceClick(Preference preference) {
            this.preference = preference;
            switch (preference.getKey()) {
                case APP_PREF_KEY_REMOVAL:
                    AlertDialog.Builder builder;
                    builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle(R.string.key_removal)
                            .setMessage(R.string.key_removal_confirm)
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext()).edit();
                                    editor.remove(Session.APP_PREF_PUBLIC_KEY);
                                    editor.remove(Session.APP_PREF_PRIVATE_KEY);
                                    editor.commit();
                                    textPublicKey.setSummary("");
                                    textPrivateKey.setSummary("");
                                }
                            })
                            .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // do nothing
                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                    break;

                default:
                    Intent intent = new Intent(getActivity(), KeyInputActivity.class);
                    intent.putExtra(EXTRA_KEY_NAME, (preference.getKey().equals(Session.APP_PREF_PUBLIC_KEY)) ?
                            getString(R.string.public_key) : getString(R.string.private_key));
                    startActivityForResult(intent, REQUEST_CODE_KEY);
                    break;
            }

            return true;
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            if (requestCode == REQUEST_CODE_KEY && resultCode == RESULT_OK) {
                String textKey = data.getStringExtra(EXTRA_KEY);
                if (textKey != null && preference != null) {
                    byte[] encryptBytes = new byte[0];
                    String encryptString = "";
                    try {
                        encryptBytes = Enigma.easEncrypt(getActivity().getApplicationContext(), textKey.getBytes(), Session.getInstance().getPasswordValue());
                        encryptString = Base64.encodeToString(encryptBytes, Base64.NO_WRAP);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (!(encryptString.isEmpty())) {
                        preference.setSummary(encryptString);
                        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext()).edit();
                        editor.putString(preference.getKey(), encryptString);
                        editor.commit();
                    }
                }
            } else {
                super.onActivityResult(requestCode, resultCode, data);
            }
        }
    }
}
