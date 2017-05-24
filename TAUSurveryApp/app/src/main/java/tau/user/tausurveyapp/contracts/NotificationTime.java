package tau.user.tausurveyapp.contracts;

import com.google.gson.annotations.Expose;


public class NotificationTime {
    @Expose
    public DayOfWeek dayOfWeek;
    @Expose
    public int hour;
    @Expose
    public int minute;

    public NotificationTime(DayOfWeek dayOfWeek, int hour, int minute) {
        this.dayOfWeek = dayOfWeek;
        this.hour = hour;
        this.minute = minute;
    }
}
