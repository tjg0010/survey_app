package tau.user.tausurveryapp;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.LayoutDirection;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.util.Dictionary;
import java.util.List;

import tau.user.tausurveryapp.contracts.Choice;
import tau.user.tausurveryapp.contracts.Field;
import tau.user.tausurveryapp.contracts.TauLocale;
import tau.user.tausurveryapp.contracts.Survey;

/**
 * Created by ran on 15/04/2017.
 */

public class SurveyBuilder {

    private final float titleTextSize = 24;
    private final float defaultTextSize = 14;
    private int idsCounter;

    private Dictionary<String, Field> idToFieldDict;
    private Dictionary<String, Integer> fieldIdToViewId;

    public SurveyBuilder() {
        idsCounter = 0;
    }


    public void BuildSurvey(Activity activity, Survey survey, View view, TauLocale locale) {
        // Set the activity's title.
        activity.setTitle(survey.getString(locale, survey.metadata.title));

        // Go over all the survey fields and create its layout.
        if (survey != null && survey.fields != null && !survey.fields.isEmpty()) {
            for (Field field : survey.fields) {
                // Only do something if the field has an id and a type.
                if (!TextUtils.isEmpty(field.id) && field.getType() != null) {
                    // Add the the field to the dictionary.
                    idToFieldDict.put(field.id, field);

                    // Create the field's layout and add it to the view.
                    createFieldLayout(survey, field, activity, view, locale);
                }
            }
        }
    }

    private void createFieldLayout(Survey survey, Field field, Activity activity, View view, TauLocale locale) {
        // We put each field inside a linear layout.
        LinearLayout ll = createLinearLayout(activity, locale);

        // If the field has a title.
        if (field.getTitleId() > 0) {
            // Get the title according to the given locale.
            String title = survey.getString(locale, field.getTitleId());

            // Create a text view for this title.
            TextView titleView = createTextView(activity, title);

            // Add it to the field's linear layout.
            ll.addView(titleView);
        }

        switch (field.getType()) {
            case ADDRESS:
                break;
            case STRING:
                break;
            case INT:
                break;
            case BOOLEAN:
                break;
            case DATE:
                break;
            case CHOICES:
                if (field.choices != null && field.choices.length > 0) {
                    RadioGroup radioButtons = createRadioButtons(activity, survey, field, locale);
                    ll.addView(radioButtons);
                }
                break;
            case GROUP:
                break;
        }
    }

    private LinearLayout createLinearLayout(Context context, TauLocale locale) {
        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        LinearLayout.LayoutParams layoutParams = createLinearLayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, locale);
        layoutParams.setMargins(15,15,15,15);
        linearLayout.setLayoutParams(layoutParams);

        return linearLayout;
    }

    private TextView createTextView(Context context, String text) {
        TextView textView = new TextView(context);
        textView.setText(text);
        textView.setTextSize(titleTextSize);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        textView.setLayoutParams(layoutParams);

        return textView;
    }

    private RadioGroup createRadioButtons(Context context, Survey survey, Field field, TauLocale locale) {
        // First create the radio group to which we'll add the radio buttons.
        RadioGroup radioGroup = new RadioGroup(context);
        radioGroup.setId(getViewId(field));
        radioGroup.setOrientation(RadioGroup.VERTICAL);
        radioGroup.setLayoutParams(createLinearLayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, locale));

        // Go over the choices we got and create a radio button for each one, then add it to the radio group.
        for (int i = 0; i < field.choices.length; i++) {
            Choice choice = field.choices[i];

            if (!TextUtils.isEmpty(choice.value) && choice.title != 0){
                RadioButton radioButton = new RadioButton(context);
                radioButton.setText(survey.getString(locale, choice.title));
                // We will need an id to figure out later which radio button was selected.
                radioButton.setId(i);
                radioGroup.addView(radioButton);
            }
        }

        return radioGroup;
    }


    // Helper functions

    /**
     * Takes care of incrementing the idsCounter, so that each view gets its unique id.
     * It also documents which field got which view id, so we can retrieve each field's value later.
     * @return a unique id for a view.
     */
    private int getViewId(Field field) {
        idsCounter++;

        fieldIdToViewId.put(field.id, idsCounter);

        return idsCounter;
    }

    /**
     * Creates a linear LayoutParams object.
     * @param matchOrWrapParent - either LinearLayout.LayoutParams.MATCH_PARENT or LinearLayout.LayoutParams.WRAP_CONTENT.
     * @param locale - if not null, direction rtl or ltr will be added to the layoutParams according to locale.
     * @return a LinearLayout.LayoutParams object.
     */
    private LinearLayout.LayoutParams createLinearLayoutParams(int matchOrWrapParent, @Nullable TauLocale locale){
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(matchOrWrapParent, matchOrWrapParent);

        if (locale != null) {
            switch (locale) {
                case EN:
                    layoutParams.setLayoutDirection(0); // 0 means LTR.
                    break;
                case IL:
                default:
                    layoutParams.setLayoutDirection(1); // 1 means RTL.
                    break;
            }
        }

        return layoutParams;
    }
}
