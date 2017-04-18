package tau.user.tausurveyapp.contracts;

import android.text.TextUtils;

import java.util.List;
import java.util.Map;

/**
 * Created by ran on 14/04/2017.
 */

public class Survey {
    public SurveyMetadata metadata;

    public List<Field> fields;

    public Map<TauLocale, Map<String, String>> strings;

    /**
     * Gets a string from the survey's strings dictionary with the given locale and string id.
     * @param locale - the locale to use when searching for the given string id.
     * @param StringId - the requested string id.
     * @return the title text.
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

