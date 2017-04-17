package tau.user.tausurveryapp;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.toptoche.searchablespinnerlibrary.SearchableSpinner;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import java.util.Calendar;
import java.util.HashMap;

import tau.user.tausurveryapp.contracts.Choice;
import tau.user.tausurveryapp.contracts.Field;
import tau.user.tausurveryapp.contracts.TauLocale;
import tau.user.tausurveryapp.contracts.Survey;
import tau.user.tausurveryapp.uiListeners.TauDateListener;

/**
 * Created by ran on 15/04/2017.
 */

public class SurveyBuilder {

    private final float titleTextSize = 22;
    private int idsCounter;

    private HashMap<String, Field> idToFieldDict;
    private HashMap<String, Integer> fieldIdToViewId;

    public SurveyBuilder() {
        // Initialize idsCounter to 1000, so it won't get mixed up with other ids (we probably won't have a 1000 views).
        idsCounter = 1000;
        idToFieldDict = new HashMap<String, Field>();
        fieldIdToViewId = new HashMap<String, Integer>();
    }


    public void BuildSurvey(Activity activity, Survey survey, LinearLayout view, TauLocale locale) {
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
                    LinearLayout fieldLayout = createFieldLayout(survey, field, activity, locale);

                    // Add the field to the view.
                    view.addView(fieldLayout);
                }
            }
        }
    }

    private LinearLayout createFieldLayout(Survey survey, Field field, Activity activity, TauLocale locale) {
        // We put each field inside a linear layout.
        LinearLayout ll = createWrapperLinearLayout(activity, locale);

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
                LinearLayout addressGroup = createAddressGroup(activity, survey, field, locale);
                ll.addView(addressGroup);
                break;
            case STRING:
                break;
            case INT:
                break;
            case BOOLEAN:
                RadioGroup booleanGroup = createBooleanRadioButtons(activity, survey, field, locale);
                ll.addView(booleanGroup);
                break;
            case DATE:
                LinearLayout dateInput = createDateInput(activity, survey, field, locale);
                ll.addView(dateInput);
                break;
            case CHOICES:
                if (field.choices != null && field.choices.length > 0) {
                    RadioGroup choicesGroup = createRadioButtons(activity, survey, field, locale);
                    ll.addView(choicesGroup);
                }
                break;
            case GROUP:
                break;
        }

        return ll;
    }

    private LinearLayout createWrapperLinearLayout(Context context, TauLocale locale) {
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

            RadioButton radioButton = new RadioButton(context);
            radioButton.setText(survey.getString(locale, choice.title));
            RadioGroup.LayoutParams layoutParams = new RadioGroup.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            radioButton.setLayoutParams(layoutParams);
            // We will need an id to figure out later which radio button was selected.
            radioButton.setId(i);
            radioGroup.addView(radioButton);
        }

        return radioGroup;
    }

    private RadioGroup createBooleanRadioButtons(Activity activity, Survey survey, Field field, TauLocale locale) {
        // First create the radio group to which we'll add the radio buttons.
        RadioGroup booleanGroup = new RadioGroup(activity);
        booleanGroup.setId(getViewId(field));
        booleanGroup.setOrientation(RadioGroup.VERTICAL);
        booleanGroup.setLayoutParams(createLinearLayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, locale));

        // Create two radio buttons: yes and no.
        // Yes radio button.
        RadioButton radioButtonYes = new RadioButton(activity);
        radioButtonYes.setText(activity.getResources().getString(R.string.boolean_true));
        radioButtonYes.setLayoutParams(createLinearLayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, locale));
        // We will need an id to figure out later which radio button was selected.
        radioButtonYes.setId(0);
        booleanGroup.addView(radioButtonYes);

        // No radio button.
        RadioButton radioButtonNo = new RadioButton(activity);
        radioButtonNo.setText(activity.getResources().getString(R.string.boolean_false));
        radioButtonNo.setLayoutParams(createLinearLayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, locale));
        // We will need an id to figure out later which radio button was selected.
        //noinspection ResourceType
        radioButtonNo.setId(1);
        booleanGroup.addView(radioButtonNo);

        return booleanGroup;
    }

    private LinearLayout createDateInput(final Activity activity, Survey survey, Field field, TauLocale locale) {
        // Create a container for the "add date" button.
        LinearLayout container = new LinearLayout(activity);
        container.setOrientation(LinearLayout.HORIZONTAL);
        container.setLayoutParams(createLinearLayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, locale));

        // Add a text view that will hold the chosen date.
        TextView dateText = new TextView(activity);
        dateText.setId(getViewId(field));
        dateText.setTextColor(Color.BLACK);
        dateText.setVisibility(View.GONE);
        dateText.setPaintFlags(dateText.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        LinearLayout.LayoutParams dateTextParams = createLinearLayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT);
        dateTextParams.setMarginStart(70);
        dateText.setLayoutParams(dateTextParams);

        // Create a button that the user can click to add a date via a dialog.
        Button dateButton = new Button(activity);
        dateButton.setText(activity.getResources().getString(R.string.date_button));
        dateButton.setLayoutParams(createLinearLayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, locale));

        // Create a date listener to catch the set event of the date picker.
        // We supply it with the dateHiddenText, so we can retrieve the date later on.
        final TauDateListener dateListener = new TauDateListener(dateText);

        // Show a DatePickerDialog when the user clicks the button.
        dateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar now = Calendar.getInstance();
                // Supply the DatePickerDialog with our dateListener.
                DatePickerDialog dpd = DatePickerDialog.newInstance(
                        dateListener,
                        now.get(Calendar.YEAR),
                        now.get(Calendar.MONTH),
                        now.get(Calendar.DAY_OF_MONTH)
                );
                dpd.show(activity.getFragmentManager(), "DatePickerDialog");
            }
        });

        container.addView(dateButton);
        container.addView(dateText);

        return container;
    }

    @SuppressWarnings("ResourceType")
    private LinearLayout createAddressGroup(Context context, Survey survey, Field field, TauLocale locale) {
        // Create a container for the whole address input group.
        LinearLayout container = new LinearLayout(context);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setLayoutParams(createLinearLayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, locale));

        // Create a container for "street" and "street number" (which are side by side).
        LinearLayout streetContainer = new LinearLayout(context);
        streetContainer.setOrientation(LinearLayout.HORIZONTAL);
        streetContainer.setLayoutParams(createLinearLayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, locale));

        // Create the "street" input.
        EditText streetName = new EditText(context);
        streetName.setId(0);
        streetName.setInputType(InputType.TYPE_CLASS_TEXT);
        streetName.setHint(R.string.address_street);
        streetName.setLayoutParams(createLinearLayoutParamsWithWeight(0.7));
        streetContainer.addView(streetName);
        // Create the "street number" input.
        EditText streetNumber = new EditText(context);
        streetNumber.setId(1);
        streetNumber.setInputType(InputType.TYPE_CLASS_NUMBER);
        streetNumber.setHint(R.string.address_street_number);
        streetNumber.setLayoutParams(createLinearLayoutParamsWithWeight(0.3));
        InputFilter[] filterArray = new InputFilter[1];
        filterArray[0] = new InputFilter.LengthFilter(5); // Set maximum length of street number.
        streetNumber.setFilters(filterArray);
        streetContainer.addView(streetNumber);

        // Create a container for the city name input.
        LinearLayout cityContainer = new LinearLayout(context);
        cityContainer.setOrientation(LinearLayout.VERTICAL);
        cityContainer.setLayoutParams(createLinearLayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, locale));

        // Load the cities string array from the resources.
        String[] cities = context.getResources().getStringArray(R.array.cities_list);

        // Create the "city" input with auto complete.
        SearchableSpinner cityName = new SearchableSpinner(context);
        cityName.setId(2);
        cityName.setLayoutParams(createLinearLayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, locale));
        cityName.setTitle(context.getResources().getString(R.string.address_city));
        cityName.setPositiveButton(context.getResources().getString(R.string.cancel));
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, cities);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        cityName.setAdapter(adapter);
        cityContainer.addView(cityName);

        // Add everything to the container.
        container.addView(streetContainer);
        container.addView(cityContainer);

        return container;
    }



    // region: Helper functions

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

    private LinearLayout.LayoutParams createLinearLayoutParams(int matchOrWrapParent){
        return createLinearLayoutParams(matchOrWrapParent, null);
    }

    private LinearLayout.LayoutParams createLinearLayoutParamsWithWeight(double weight){
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                (float) weight);

        return layoutParams;
    }

    // endregion
}
