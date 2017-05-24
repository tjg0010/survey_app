package tau.user.tausurveyapp.contracts;

import android.text.TextUtils;

import com.google.gson.annotations.Expose;

import java.util.List;
import java.util.Map;

public class SurveyInfo {
    @Expose
    public TauScreenInfo welcomeScreen;

    @Expose
    public List<NotificationTime> diaryDates;

    @Expose
    public boolean isUserRegistered;

    @Expose
    public Map<TauLocale, Map<String, String>> strings;

    /**
     * Gets a string from the info's strings dictionary with the given locale and string id.
     * @param locale - the locale to use when searching for the given string id.
     * @param StringId - the requested string id.
     * @return the text.
     */
    public String getString(TauLocale locale, int StringId) {
        String result = "";

        if (this.strings != null && !this.strings.isEmpty()) {
            Map<String, String> localeStrings = this.strings.get(locale);
            if (localeStrings != null && !localeStrings.isEmpty()) {
                String str = localeStrings.get(Integer.toString(StringId));

                if (!TextUtils.isEmpty(str)) {
                    result = str;
                }
            }
        }

        return result;
    }
}
