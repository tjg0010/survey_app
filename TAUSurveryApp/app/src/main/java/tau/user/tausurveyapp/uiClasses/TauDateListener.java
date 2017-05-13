package tau.user.tausurveyapp.uiClasses;

import android.view.View;
import android.widget.TextView;

import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import tau.user.tausurveyapp.R;
import tau.user.tausurveyapp.Utils;

/**
 * Created by ran on 17/04/2017.
 */

public class TauDateListener implements DatePickerDialog.OnDateSetListener {

    private TextView dateTextView;

    public TauDateListener(TextView dateTextView) {
        this.dateTextView = dateTextView;
    }

    /**
     * This event is trigger when a date is set in a DatePickerDialog.
     * It then parses the date into a user readable format, and saves it into the given dateTextView to show it to the user,
     * and so the caller can retrieve it later.
     */
    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        String date = makeTwoDigits(dayOfMonth)+"/"+makeTwoDigits(monthOfYear+1)+"/"+year;
        // Set the user text of the selected date.
        dateTextView.setText(date);
        // Save the actual time (unix time) as the text view first tag.
        dateTextView.setTag(R.id.tau_date_tag, Utils.getUnixDate(year, monthOfYear, dayOfMonth));
        // Make the dateTextView visible, now that it holds a date.
        dateTextView.setVisibility(View.VISIBLE);
    }

    /**
     * A helper function that turns numbers into 2 digits (assuming it receives a single digit or a two digit number).
     */
    private String makeTwoDigits(int number) {
        String numStr;

        if (number < 10) {
            numStr = "0" + Integer.toString(number);
        }
        else {
            numStr = Integer.toString(number);
        }

        return numStr;
    }
}
