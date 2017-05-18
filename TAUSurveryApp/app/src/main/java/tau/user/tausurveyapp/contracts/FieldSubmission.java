package tau.user.tausurveyapp.contracts;

import android.app.Activity;
import android.text.TextUtils;

import com.google.gson.annotations.Expose;

import tau.user.tausurveyapp.R;

public class FieldSubmission<T> {
    // Don't expose this field to GSON (as json) because it doesn't know how to handle it.
    @Expose(serialize = false, deserialize = false)
    private final Class<T> type;

    @Expose
    public String id;

    @Expose
    public T value;

    @Expose
    public String groupId;

    @Expose
    public String groupRepetitionValue;

    public FieldSubmission(Class<T> type, Field field, T value, Object groupRepetitionTag) {
        this.type = type;
        this.id = field.id;
        this.value = value;
        this.groupId = field.groupId;
        this.groupRepetitionValue = groupRepetitionTag == null ? null : groupRepetitionTag.toString();
    }

    public boolean isValueEmpty(Activity activity) {
        if (this.type == String.class) {
            return TextUtils.isEmpty((String)this.value);
        }
        else if (this.type == Address.class) {
            String[] cities = activity.getResources().getStringArray(R.array.cities_list);
            return ((Address)this.value).isEmpty(cities[0]);
        }

        return (this.value == null);
    }
}

