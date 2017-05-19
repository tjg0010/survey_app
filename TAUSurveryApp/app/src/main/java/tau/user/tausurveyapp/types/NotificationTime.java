package tau.user.tausurveyapp.types;

import tau.user.tausurveyapp.contracts.DayOfWeek;

public class NotificationTime {
    public DayOfWeek dayOfWeek;
    public int hour;
    public int minute;

    public NotificationTime(DayOfWeek dayOfWeek, int hour, int minute) {
        this.dayOfWeek = dayOfWeek;
        this.hour = hour;
        this.minute = minute;
    }
}
