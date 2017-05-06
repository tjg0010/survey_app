package tau.user.tausurveyapp.types;

import java.util.ArrayList;

public class SurveySubmitResult {
    private boolean isSuccess;
    private String errorMessage;
    private ArrayList<Integer> emptyViewIds;

    public SurveySubmitResult(boolean isSuccess, String errorMessage, ArrayList<Integer> emptyViewIds) {
        this.isSuccess = isSuccess;
        this.errorMessage = errorMessage;
        this.emptyViewIds = emptyViewIds;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public ArrayList<Integer> getEmptyViewIds() {
        return emptyViewIds;
    }
}
