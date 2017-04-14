package tau.user.tausurveryapp.contracts;

import java.util.List;

/**
 * Created by ran on 14/04/2017.
 */

public class Field {
    public String id;
    public boolean mandatory;
    public int title;
    public FieldType type;
    public List<Choice> choices;
    public String conditionOn;
    public ConditionType conditionType;
}

enum FieldType {
    STRING,
    INT,
    BOOLEAN,
    DATE,
    ADDRESS,
    CHOICES,
    GROUP
}

enum ConditionType {
    REPEAT,
    SHOW
}

