# TAU Survey App

Welcome! Below you will find user instructions on how to configure Tau Survey App. Please follow these instructions to change the surveys and configure the app as you wish.

## Instructions for creating and updating a survey json *(surveyRegister.json and surveyDiary.json files)*:
All strings passed to the client are numbers mapped to the [strings](#the-strings-element) area in the file.
   You **MUST** always supply a number and a matching title in the [strings](#the-strings-element) element or it won't work.
   This was done to enable localization support later on.

Below you will find a breakdown description of the survey json template and how to use it:

### The "metadata" element:
- **"title"** - (mandatory) the survey's title that will appear as a title in the top toolbar part in the app.
- **"startText"** - (not mandatory) some text that appear before the survey fields, e.g instructions.
- **"submitBtnText"** - (mandatory) the text to display on the submit button in the survey.
- **"name"** - (mandatory) the name of the survey. MUST match the db table name, for which this survey is correlated with. if you're asking yourself "what about groups and sub-tables", we will cover that in a moment.

### The "fields" element
An array of fields with the following attributes:
- **"id"** - (mandatory) the id of the field. **MUST** match the db column name in the corresponding table, and must be **UNIQUE** throughout the survey.
- **"mandatory"** - (mandatory) whether the user must or must not fill this field. Can be set to ```true``` or ```false```.
- **"title"** - (not mandatory) the field's title to show above/beside it.
- **"type"** - (mandatory) the field's type. Can be one of the following:
  1. **_"CHOICES"_** - radio buttons the user can choose from. When this type is chosen, you must also supply an attribute called "choices", which is an array of elements that consist of:
      1. "value" - (mandatory) the value to be saved to the db. All values are considered ```VARCHAR(45)``` in the db.
      2. "title" - (not mandatory) a number from the "strings" object.
      3. "type" - (mandatory) can be either "OPTION" (for a regular option) or "OTHER" (that lets the user enter a free text option). "OTHER" is currently not supported and is treated as "OPTION" but we plan to support it in the future if needed.
  3. **_"DATE"_** - a date to be entered by the user and saved to the db as ```DATETIME```.
  4. **_"YEAR"_** - a year to be entered by the user and saved to the db as ```INT(4)```.
  5. **_"ADDRESS"_** - an address consisted of a streetName, streetNumber and city (from a closed list of cities).
                   Note that this type is broken down and saved as 3 different fields in the db (```VARCHAR```).
                   Their names should start with the given id and end with City, Street and Number, i.e <id>City, <id>Street, <id>Number.
  6. **_"BOOLEAN"_** - presents yes/no radio buttons to the user. Mapped to ```TINYINT(1)``` in the db, where 0 represents false and 1 true.
  7. **_"INT"_** - a number (integer) input presented to the user and mapped to ```INT``` in the db.
  8. **_"STRING"_** - a string input presented to the user and mapped to ```VARCHAR``` in the db.
  9. **_"TABLE_INT"_** - a number (integer) input presented to the user as a table row and mapped to ```INT``` in the db.
  10. **_"TITLE"_** - a title field. Doesn't receive any input and ignored in all processing. It is used to show a the user a title text between fields in an easy way.
  11. **_"GROUP"_** - basically, holds a sub query that will be presented to the user in different scenarios. Its id field **MUST** be mapped to a corresponding table in the db. When this type is chosen you must also supply an attribute called "fields" which holds an array of fields just like the survey itself does. Moreover, you must supply an attribute called _"condition"_ which holds the following attributes:
      1. "type" - (mandatory) may be "REPEAT" or "SHOW". While "show" only displays or hides the whole group, "REPEAT" can also repeat it the number of times requested.
      2. "repeatText" - (not mandatory) if type is "REPEAT" you may also supply a repeated text which will appear before each group repetition. Note that that text (defined in [strings](#the-strings-element)) can contain a joker marked as ```##```. This jocker will be replaced with the repetition number (if source="CLIENT") or the corresponding values found in the db (if source="SERVER").
      3. "source" - (mandatory) "SERVER" or "CLIENT". When "CLIENT" is chosen the group will be repeated according to the value in the field with the id specified in "conditionOn". This is done completely in the client side, which sends the server the results. When "SERVER" is chosen the group will be repeated according to the value in the db according to "conditionOn".
      4. "conditionOn" - (mandatory) either a field id to condition on (when source="CLIENT"), or a table.column name to condition on (when source="SERVER"). When source is "SERVER", the name is assumed to be ```<tableName>.<columnName>```, where the column can be of any type. Notice that the survey query is always according to the userId of the user that is requesting for the survey. Therefore if this is the registration survey, you should not use "SERVER" as source since the user doesn't have an id and data to condition on. When source is "CLIENT", the name should **ONLY** be of a field which is of "INT" type. We might add support for "BOOLEAN" in the future.
      5. "repetitions" - (auto generated) Relevant when source is "SERVER". This is filled by the server according to the number of rows found in the db.
      6. "values" - (auto generated) Relevant when source is "SERVER". This is filled by the server that sends the values found in the "conditionOn" column in the db to the app. The values are sent so the client can fill them inside each repeatedText title, if the title contains the "##" joker.
      
### The "strings" element
A map (dictionary) that holds the relevant survey titles according to locale.
The locale can either be "IL" or "EN", though only "IL" is support for the time being.
Each locale holds another dictionary that maps the title id to the title itself. The title id is then used in the various fields and attributes.

### PLEASE NOTE ###
Each update to the surveys requires a server restart for it to take place!

### Example survey json ###
```
{
  "metadata": {
    "title": 1,
    "startText": 2,
    "submitBtnText": 3,
    "name":"main"
  },
  "fields": [
    {"id": "gender", "mandatory": true, "title": 21, "type": "CHOICES",
      "choices":[
        {"value": "male", "title": 22, "type": "OPTION"},
        {"value": "female", "title": 23, "type": "OPTION"}]},
    {"id": "birthYear", "mandatory": true, "title": 31, "type": "YEAR"},
    {"id": "home", "mandatory": true, "title": 41, "type": "ADDRESS"},
    {"id": "hasPrivateCar", "mandatory": true, "title": 51, "type": "BOOLEAN"},
    {"id": "numberOfChildren", "mandatory": true, "title": 61, "type": "INT"},
    {"id": "children", "mandatory": true, "type": "GROUP",
      "condition": {"type": "REPEAT", "repeatText": 71, "source": "CLIENT", "conditionOn": "numberOfChildren"},
      "fields":[
        {"id": "childName", "mandatory": true, "title": 81, "type": "STRING"},
        {"id": "childGender", "mandatory": true, "title": 91, "type": "CHOICES",
          "choices":[
            {"value": "male", "title": 22, "type": "OPTION"},
            {"value": "female", "title": 23, "type": "OPTION"}]}
      ]}
  ],
  "strings": {
    "IL": {
      "1": "הרשמה לסקר",
      "2": "על מנת להירשם לסקר, אנא מלא/י את הפרטים הבאים:",
      "3": "הרשמה",
      "21": "מין",
      "22": "זכר",
      "23": "נקבה",
      "31": "שנת לידה",
      "41": "כתובת מגורים",
      "51": "האם ברשותך רכב פרטי?",
      "61": "מספר ילדים עד גיל 18",
      "71": "פרטי ילד/ה ##:",
      "81": "שם פרטי",
      "91": "מין"
    },
    "EN": {
    }
  }
}
```

## Instructions for creating and updating the surveys configuration *(config.json)*
### "welcomeScreen", "agreeScreen" and "infoScreen"
All are consisted of two attributes: "title" and "text", which are (similarly to the surveys) numbers mapping a string in [the strings element](#the-configuration-strings-element) in this configuration. Both attributes are mandatory.
The strings mapped to each screen's "text" element are parsed as HTML. This means that you can add very basic formatting, by adding <br/> elements to the text to create line breaks.

### The "diaryDates" element
An array of notification times. This element is not mandatory, and if missing the app will not set any notification times. Each notification time in this array is consisted of:
- **"dayOfWeek"** - (mandatory) one of the days of the week in which you want the notification to pop. Can be one of the following: ```"SUNDAY", "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY"```.
- **"hour"** - the hour, in 24 hours format, in which the notification should pop.
- **"minute"** - the minute in the given hour and day of week the notification should pop. Can be between ```0``` and ```59```.

### The "bluetoothSamplingDates" element
Similarly to [diaryDates](#the-diaryDates-element), this is an array of bluetooth sampling times. This element is not mandatory, and when not supplied no bluetooth samples will be done by the client app. Bluetooth sampling was designed this way (by supplying desired times) because of its heavy battery usage. A bluetooth sample may take up to 1 minute (this is set as an active time cap for the sampling), so requested time should at least have a 2 minutes gap between one another. Each sample time in this array is consisted of:
- **"dayOfWeek"** - (mandatory) one of the days of the week in which you want the notification to pop. Can be one of the following: ```"SUNDAY", "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY"```.
- **"hour"** - the hour, in 24 hours format, in which the notification should pop.
- **"minute"** - the minute in the given hour and day of week the notification should pop. Can be between ```0``` and ```59```.

### The configuration "strings" element
See [the strings element](#the-strings-element) for the survey jsons.

### Example config json ###
```
{
  "welcomeScreen": {
    "title": 1,
    "text": 2
  },
  "agreeScreen": {
    "title": 3,
    "text": 4
  },
  "infoScreen": {
    "title": 5,
    "text": 6
  },
  "diaryDates": [
    { "dayOfWeek": "SUNDAY", "hour": 20, "minute": 0 },
    { "dayOfWeek": "MONDAY", "hour": 20, "minute": 0 },
    { "dayOfWeek": "TUESDAY", "hour": 20, "minute": 0 },
    { "dayOfWeek": "WEDNESDAY", "hour": 20, "minute": 0 },
    { "dayOfWeek": "THURSDAY", "hour": 20, "minute": 0 },
    { "dayOfWeek": "FRIDAY", "hour": 20, "minute": 0 },
    { "dayOfWeek": "SATURDAY", "hour": 21, "minute": 0 }
  ],
  "bluetoothSamplingDates": [
    { "dayOfWeek": "SUNDAY", "hour": 11, "minute": 0 },
    { "dayOfWeek": "MONDAY", "hour": 12, "minute": 30 },
    { "dayOfWeek": "TUESDAY", "hour": 15, "minute": 0 },
    { "dayOfWeek": "WEDNESDAY", "hour": 16, "minute": 0 },
    { "dayOfWeek": "THURSDAY", "hour": 17, "minute": 0 },
    { "dayOfWeek": "FRIDAY", "hour": 19, "minute": 30 },
    { "dayOfWeek": "SATURDAY", "hour": 20, "minute": 30 }
  ],
  "strings": {
    "IL": {
      "1": "אודות הסקר",
      "2": "ברוכים הבאים לסקר 'רשת מפגשים' של אוניברסיטת תל אביב.<br/><br/>אנא לחץ על המשך כדי להתחיל בסקר.",
      "3": "טופס הסכמה",
      "4": "משתתף/ת יקר/ה,<br/><br/>הנך מוזמן/ת למלא שאלון זה המהווה חלק ממחקר של אוניברסיטת תל אביב לבחינת התפשטות מחלות",
      "5": "דף מידע",
      "6": "תודה רבה על שיתוף הפעולה!"
    },
    "EN": {
    }
  }
}
```

## Server Setup ##
1. Install node-v6.9.1-x64.msi
2. Open cmd, create a code folder and a project folder, navigate to it and run:
	```
   npm install express --save
	npm install body-parser --save
	npm install multer --save
	npm install mysql
   npm install winston --save
   npm install bluebird --save
   ```
3. Install mySql community edition by running the web installer: mysql-installer-web-community-5.7.16.0.msi


#### For a developer environment, also install: ####
1. Install WebStorm - for node js development
2. Install Android Studio - for mobile app development















