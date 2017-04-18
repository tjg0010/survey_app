package tau.user.tausurveyapp.contracts;

/**
 * Created by ran on 14/04/2017.
 */

public class Choice {
    public String value;
    public int title;
    public ChoiceType type;
}

enum ChoiceType {
    OPTION,
    OTHER
}


