package th.ac.dusit.dbizcom.bikeparking.etc;

import android.content.Context;
import android.content.SharedPreferences;

import th.ac.dusit.dbizcom.bikeparking.model.User;

public class MyPrefs {

    private static final int INVALID_USER_ID = -1;

    private static final String KEY_PREF_FILE = "pref_file";
    private static final String KEY_USER_ID = "user_id_pref";
    private static final String KEY_USER_PID = "user_pid_pref";
    private static final String KEY_USER_FIRST_NAME = "user_first_name_pref";
    private static final String KEY_USER_LAST_NAME = "user_last_name_pref";
    private static final String KEY_PROVIDER_ID = "provider_id_pref";
    private static final String KEY_PROVIDER_PID = "provider_pid_pref";
    private static final String KEY_PROVIDER_FIRST_NAME = "provider_first_name_pref";
    private static final String KEY_PROVIDER_LAST_NAME = "provider_last_name_pref";

    private static SharedPreferences getSharedPref(Context context) {
        return context.getSharedPreferences(
                KEY_PREF_FILE, Context.MODE_PRIVATE
        );
    }

    public static void setUserPref(Context context, User user) {
        SharedPreferences.Editor editor = getSharedPref(context).edit();
        editor.putInt(KEY_USER_ID, user == null ? INVALID_USER_ID : user.id);
        editor.putString(KEY_USER_PID, user == null ? "" : user.pid);
        editor.putString(KEY_USER_FIRST_NAME, user == null ? "" : user.firstName);
        editor.putString(KEY_USER_LAST_NAME, user == null ? "" : user.lastName);

        editor.apply();
    }

    public static User getUserPref(Context context) {
        int userId = getSharedPref(context).getInt(KEY_USER_ID, INVALID_USER_ID);
        if (userId == INVALID_USER_ID) {
            return null;
        } else {
            String pid = getSharedPref(context).getString(KEY_USER_PID, "");
            String firstName = getSharedPref(context).getString(KEY_USER_FIRST_NAME, "");
            String lastName = getSharedPref(context).getString(KEY_USER_LAST_NAME, "");
            return new User(userId, pid, firstName, lastName);
        }
    }
    public static void setProviderPref(Context context, User provider) {
        SharedPreferences.Editor editor = getSharedPref(context).edit();
        editor.putInt(KEY_PROVIDER_ID, provider == null ? INVALID_USER_ID : provider.id);
        editor.putString(KEY_PROVIDER_PID, provider == null ? "" : provider.pid);
        editor.putString(KEY_PROVIDER_FIRST_NAME, provider == null ? "" : provider.firstName);
        editor.putString(KEY_PROVIDER_LAST_NAME, provider == null ? "" : provider.lastName);
        editor.apply();
    }

    public static User getProviderPref(Context context) {
        int providerId = getSharedPref(context).getInt(KEY_PROVIDER_ID, INVALID_USER_ID);
        if (providerId == INVALID_USER_ID) {
            return null;
        } else {
            String pid = getSharedPref(context).getString(KEY_PROVIDER_PID, "");
            String firstName = getSharedPref(context).getString(KEY_PROVIDER_FIRST_NAME, "");
            String lastName = getSharedPref(context).getString(KEY_PROVIDER_LAST_NAME, "");
            return new User(providerId, pid, firstName, lastName);
        }
    }
}