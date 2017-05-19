package tau.user.tausurveyapp;

import android.content.Context;


public class SurveyManager {
    private static final SurveyManager ourInstance = new SurveyManager();

    public static SurveyManager getInstance() {
        return ourInstance;
    }

    private SurveyManager() {
    }

    public void surveyStarted(Context context) {
        // Check if the survey wasn't already started.
        Long surveyEndTime = Utils.getLongFromPrefs(context, R.string.key_survey_end_time);

        // If the key wasn't found (because it wasn't set yet) save it.
        if (surveyEndTime == 0) {
            // Save a flag that stores the end time of this survey (in 7 days).
            Utils.setLongToPrefs(context, R.string.key_survey_end_time, Utils.getFutureMillisTimeByDays(8));
        }
    }

    public boolean isSurveyFinished(Context context) {
        Long surveyEndTime = Utils.getLongFromPrefs(context, R.string.key_survey_end_time);

        // If surveyEndTime is zero, this means the survey didn't get a start time yet and we should return false.
        if (surveyEndTime == 0) {
            return false;
        }

        // Otherwise, the survey should continue if the current time is less than the end date (they are both longs in millis).
        return Utils.getCurrentTimeInMillis() <= surveyEndTime;
    }
}
