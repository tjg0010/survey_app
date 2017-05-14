package tau.user.tausurveyapp.contracts;

import com.google.gson.annotations.Expose;

import java.util.List;

public class FieldCondition {
    @Expose
    public ConditionType type;
    @Expose
    public int repeatText;
    @Expose
    public int repetitions;
    @Expose
    public FieldConditionSource source;
    @Expose
    public String conditionOn;
    @Expose
    public List<String> values;
}
