package tau.user.tausurveyapp.contracts;

/**
 * An enum to represent the day of the week.
 * This enum is synced with the day_of_week values of the utils.Calendar class.
 */
public enum DayOfWeek {
    SUNDAY(1), MONDAY(2), TUESDAY(3), WEDNESDAY(4), THURSDAY(5), FRIDAY(6), SATURDAY(7);

    private final int value;

    private DayOfWeek(int value) {
        this.value = value;
    }

    /**
     * @return Returns the int value of the DayOfWeek.
     */
    public int getValue() {
        return value;
    }
}