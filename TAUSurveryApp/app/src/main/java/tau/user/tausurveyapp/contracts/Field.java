package tau.user.tausurveyapp.contracts;

import android.text.TextUtils;

import com.google.gson.annotations.Expose;

import java.util.List;

/**
 * Created by ran on 14/04/2017.
 */

public class Field {
    @Expose
    public String id;
    @Expose
    public boolean mandatory;
    @Expose
    private int title;
    @Expose
    private FieldType type;
    @Expose
    public Choice[] choices;
    @Expose
    public FieldCondition condition;
    @Expose
    public List<Field> fields;

    @Expose(serialize = false, deserialize = false)
    public String groupId;

    public FieldType getType() {
        return this.type;
    }

    public int getTitleId() {
        return this.title;
    }
}

