# TAU Survey App

## Instructions for creating and updating a survey json *(surveyRegister.json and surveyDiary.json files)*:

- All strings passed to the client are numbers mapped to the "strings" area in the file.
   You **MUST** always supply a number and a matching title in the "strings" element or it won't work.
   This was done to enable localization support later on.

Below you will find a breakdown description of the survey json template and how to user it:
### The "metadata" element:
- "title" - the survey's title that will appear as a title in the top toolbar part in the app. Mandatory.
- "startText" - some text that appear before the survey fields, e.g instructions. Not mandatory.
- "submitBtnText" - the text to display on the submit button in the survey. Mandatory.
- "name" - the name of the survey. MUST match the db table name, for which this survey is correlated with.
        if you're asking yourself "what about groups and sub-tables", we will cover that in a moment.
### The "fields" element
An array of fields with the following attributes:
- "id" - the id of the field. MUST match the db column name in the corresponding table, and must be UNIQUE throughout the survey. Mandatory.
- "mandatory" - whether the user must or must not fill this field. Mandatory.
- "title" - the field's title to show above/beside it. Not mandatory.
- "type" - the field's type. Mandatory. Can be one of the following:
  1. "CHOICES" - radio buttons the user can choose from. When this type is chosen, you must also supply an attribute called "choices", which is an array of elements that consist of:
      1. "value" - the value to be saved to the db. All values are considered VARCHAR(45) in the db.
      2. "title" - a number from the "strings" object.
      3. "type" - can be either "OPTION" (for a regular option) or "OTHER" (that lets the user enter a free text option).
                 "OTHER" is currently not supported and is treated as "OPTION" but we plan to support it in the future if needed.
  3. "DATE" - a date to be entered by the user and saved to the db as DATETIME.
  4. "YEAR" - a year to be entered by the user and saved to the db as INT(4).
  5. "ADDRESS" - an address consisted of a streetName, streetNumber and city (from a closed list of cities).
                 Note that this type is broken down and saved as 3 different fields in the db (VARCHAR).
                 Their names should start with the given id and end with City, Street and Number, i.e <id>City, <id>Street, <id>Number.
  6. "BOOLEAN" - presents yes/no radio buttons to the db. Mapped to TINYINT(1) in the db, where 0 represents false and 1 true.
  7. "INT" - a number (integer) input presented to the user and mapped to INT in the db.
  8. "STRING" - a string input presented to the user and mapped to VARCHAR in the db.
  9. "TABLE_INT" - a number (integer) input presented to the user as a table row and mapped to INT in the db.
  10. "TITLE" - a title field. Doesn't receive any input and ignored in all processing. It is used to show a the user a title text between fields in an easy way.
  11. "GROUP" - basically, holds a sub query that will be presented to the user in different scenarios. It's id field must be mapped to a corresponding table in the db.
When this type is chosen you must also supply an attribute called "fields" which holds an array of fields just like the survey itself does.
Moreover, you must supply an attribute called "condition" which holds the following attributes:
      1. "type" - may be "REPEAT" or "SHOW". While "show" only displays or hides the whole group, "REPEAT" can also repeat it the number of times requested. Mandatory.
      2. "repeatText" - if type is "REPEAT" you may also supply a repeated text which will appear before each group repetition. Not mandatory.
      3. "source" - "SERVER" or "CLIENT". When "CLIENT" is chosen the group will be repeated according to the value in the field with the id specified in "conditionOn".
                  This is done completely in the client side, which sends the server the results.
                  When "SERVER" is chosen the group will be repeated according to the value in the db according to "conditionOn".
      4. "conditionOn" - either a field id to condition on (when source="CLIENT"), or a table.column name to condition on (when source="SERVER").
                       When source is "SERVER", the name is assumed to be <tableName>.<columnName>, where the column can be of any type.
                       Notice that the survey query is always according to the userId of the user that's asking for the survey.
                       Therefore if a this is the registration survey, you should not user "SERVER" as source since the user doesn't have an id and data.
                       When source is "CLIENT", the name should ONLY be of a field which is of "INT" type. We might add support for "BOOLEAN" in the future.
      5. "repetitions" - Relevant when source is "SERVER". This is filled by the server according to the number of rows found in the db.
      6. "values" - Relevant when source is "SERVER". This is filled by the server to send the values found in the "conditionOn" column in the db.
                  The values are sent so the client can fill them inside the each repeatedText title, if the title contains the "##" joker.
### The "strings" elements 
A map (dictionary) that holds the relevant survey titles according to locale.
The locale can either be "IL" or "EN", though only "IL" is support for the time being.
Each locale holds another dictionary that maps the title id to the title itself. The title id is then used in the various fields and attributes.

### PLEASE NOTE ###
Each update to the surveys requires a server restart for it to take place!
