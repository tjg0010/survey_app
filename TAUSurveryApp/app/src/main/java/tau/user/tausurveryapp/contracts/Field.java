package tau.user.tausurveryapp.contracts;

import java.util.List;

/**
 * Created by ran on 14/04/2017.
 */

public class Field {
    public String id;
    public boolean mandatory;
    private int title;
    private FieldType type;
    public Choice[] choices;
    public String conditionOn;
    public ConditionType conditionType;
    public int repeatText;
    public List<Field> fields;


    public FieldType getType() {
        return this.type;
    }

    public int getTitleId() {
        return this.title;
    }
}

