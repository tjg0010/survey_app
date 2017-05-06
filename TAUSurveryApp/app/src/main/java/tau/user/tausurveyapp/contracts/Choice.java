package tau.user.tausurveyapp.contracts;

import com.google.gson.annotations.Expose;

/**
 * Created by ran on 14/04/2017.
 */

public class Choice {
    @Expose
    public String value;
    @Expose
    public int title;
    @Expose
    public ChoiceType type;
}

enum ChoiceType {
    OPTION,
    OTHER
}


