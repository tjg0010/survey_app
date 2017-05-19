package tau.user.tausurveyapp;

import java.util.Calendar;

import tau.user.tausurveyapp.contracts.DayOfWeek;

/**
 * Created by ran on 19/05/2017.
 */

public class NotificationManager {
    private static final NotificationManager self = new NotificationManager();

    public static NotificationManager getInstance() {
        return self;
    }

    private NotificationManager() {
    }

    public void setDates(DayOfWeek dayOfWeek, int hour, int minute) {

    }
}


