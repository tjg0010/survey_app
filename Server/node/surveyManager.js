// region: Dependencies
var Promise = require("bluebird");                              // Promises lib.
const winston = require('winston');                             // Logging.
var db = Promise.promisifyAll(require('./dbManager.js'));       // Our own dbManager.
// endregion

var fieldTypes = { choices: 'CHOICES', date: 'DATE', boolean: 'BOOLEAN', int: 'INT', string: 'STRING', address: 'ADDRESS', group: 'GROUP' };
var surveysFields = {};

/**
 * Loads a survey's parameters into memory for future use.
 * This data is later used when a survey needs to be validated and saved to db.
 * @param survey - the survey to load.
 */
exports.loadSurvey = function(survey) {
    // Only do something if we've got a valid survey to load.
    if (survey && survey.fields && survey.metadata && survey.metadata.name) {
        // Create an inner dictionary for this survey.
        surveysFields[survey.metadata.name] = {};

        // Save the survey's fields to this dict.
        saveSurveyFields(survey.metadata.name, survey);
    }
    // If the survey is null or doesn't have fields or a name, throw an exception for this shouldn't happen.
    else {
        winston.log('error', 'surveyManager.loadSurvey got a null survey or a survey without fields or a name');
        throw 'surveyManager.loadSurvey got a null survey or a survey without fields or a name';
    }
};

/**
 * Saves the given field submissions of the given survey name to the db.
 * @param surveyName - the name of the survey we want to save submissions for.
 * @param userId - the user id of the user that submitted the survey.
 * @param fieldSubmissions - the user's field submissions.
 * @param callback - a callback with the saving result.
 */
exports.saveSurvey = function(surveyName, userId, fieldSubmissions, callback) {
    // Only do something if we got some data to save.
    if (fieldSubmissions && fieldSubmissions.length) {
        var expectedFields = surveysFields[surveyName];

        // A dictionary that holds a paramNames list and a paramValues list for each survey or group name.
        var paramsDict = {};
        // Initialize the lists for the main survey results (results that don't belong to any group)
        paramsDict.main = { paramNames: [], paramValues: [] };

        // First, add the user id to the main survey list.
        addUserIdToDbLists(userId, paramsDict.main.paramNames, paramsDict.main.paramValues);

        // Go over the received fields.
        for (var i = 0; i < fieldSubmissions.length; i++) {
            var fieldSub = fieldSubmissions[i];

            // Only save the field if it's an expected one (if the expectedFields dict contains it).
            if (expectedFields[fieldSub.id]) {
                // Treatment for fields which are not part of a group.
                if (!fieldSub.groupId) {
                    // Add the field submission to the paramNames and paramValues lists of the main survey.
                    addFieldToDbLists(fieldSub, expectedFields[fieldSub.id], paramsDict.main.paramNames, paramsDict.main.paramValues);
                }
                // Treatment for fields which ARE a part of a group.
                else {
                    var groupId = fieldSub.groupId;
                    // Create lists for this group id they do not exist.
                    if (!paramsDict[groupId]) {
                        paramsDict[groupId] = { paramNames: [], paramValues: [] };
                    }

                    // Add the field submission to the paramNames and paramValues lists of this group.
                    addFieldToDbLists(fieldSub, expectedFields[fieldSub.id], paramsDict[groupId].paramNames, paramsDict[groupId].paramValues);
                }
            }
        }

        // Create a promises array to which we will save all of the db manager functions promises.
        // We need to do this so we can return the result only once all the promises are finished.
        var promises = [];

        // Save the main survey data to the db.
         promises.push(db.saveSurveyAsync(surveyName, paramsDict.main.paramNames, paramsDict.main.paramValues));

        // Go over all the different params lists we have for the different groups, and save them to the db.
        for (var group in paramsDict) {
            // Skip the main survey since it's already handled.
            if (paramsDict.hasOwnProperty(group) && group !== 'main') {
                // Call saveSurvey with the groupName instead of the surveyName.
                var parsedParamNames = parseGroupParamNames(paramsDict[group].paramNames);
                promises.push(db.saveSurveyGroupAsync(group, parsedParamNames, parseGroupParamValues(paramsDict[group].paramValues, parsedParamNames.length), userId));
            }
        }

        // Call the callback once all promises are done.
        Promise.all(promises).then(function() {
            callback();
        }).error(function(err){
            winston.log('error', 'surveyManager.saveSurvey - Error saving survey to db.', {error: err});
            callback(err);
        });
    }
};

/**
 * Goes over a given survey and enriches it with data from the db according to the given userId.
 * @param survey - the survey to enrich.
 * @param userId - the userId we need to enrich the survey data with.
 * @param callback - a callback to be called when done.
 */
exports.enrichSurvey = function (survey, userId, callback) {
    var serverGroupFound = false;

    if (userId && survey && survey.fields) {
        // Go over the survey's fields and check if any of them is a group field with condition.source marked as "SERVER".
        for (var i = 0; i < survey.fields.length; i++) {
            var field = survey.fields[i];

            if (field.type === fieldTypes.group && field.condition.source === 'SERVER') {
                serverGroupFound = true;

                // 1st param: the table name is assumed to be the first part of the condition.conditionOn attribute (the part before the dot).
                // 2nd param: the param name we want to return from the db is assumed to be the second part of the condition.conditionOn attribute (the part after the dot).
                var dbData = field.condition.conditionOn.split('.');
                db.getSurveyEnrichmentData(dbData[0], dbData[1], userId, function(err, rows) {
                    if (err) {
                        callback(err);
                        return;
                    }

                    // No rows were found, return 0 repetitions and an empty values array.
                    if (!rows) {
                        field.condition.repetitions = 0;
                        field.condition.values = [];
                    } else {
                        field.condition.repetitions = rows.length;
                        // Reset the values array.
                        field.condition.values = [];

                        for (var j = 0; j < rows.length; j++) {
                            var row = rows[j];

                            // Push the row value to the values array.
                            field.condition.values.push(row[dbData[1]]);
                        }
                    }

                    callback(err, survey);
                });
            }
        }

        // If no group with type SERVER was found, we didn't call the callback yet. Call it now.
        if (!serverGroupFound) {
            callback(null, survey);
        }
    }
    else {
        winston.log('error', 'surveyManager.enrichSurvey was called with an empty survey or userId.', {userId: userId, survey: survey});
        callback('Error - empty userId or survey');
    }
};

/**
 * Saves the given survey's fields to the surveysFields dictionary to the given survey name's key.
 * This function runs recursively and saves the survey's groups' fields as well to the dictionary.
 * @param surveyName - the survey name. Instructs where in the surveysFields dictionary should the fields be saved.
 * @param survey - the survey data.
 */
function saveSurveyFields(surveyName, survey){
    // Go over all the survey fields.
    for (var i = 0; i < survey.fields.length; i++) {
        var field = survey.fields[i];

        // Add this field to the survey dictionary.
        surveysFields[surveyName][field.id] = field.type;

        // If this is a group, we should run recursively on his fields and add them too.
        if (field.type === fieldTypes.group) {
            saveSurveyFields(surveyName, field);
        }
    }
}

/**
 * A helper function that adds a given field submission to the given paramNames and paramValues lists.
 * This function is used to prepare the data to be saved into the db.
 * @param fieldSub - the field submission data.
 * @param fieldType - the field type (from fieldTypes).
 * @param paramNames - the paramNames list to add the given fieldSub to.
 * @param paramValues - the paramValues list to add the given fieldSub to.
 */
function addFieldToDbLists(fieldSub, fieldType, paramNames, paramValues) {
    // Special treatment for boolean fields values.
    if (fieldType === fieldTypes.boolean) {
        paramNames.push(fieldSub.id);
        paramValues.push(fieldSub.value ? 1 : 0);
    }
    // Special treatment for address fields values.
    else if (fieldType === fieldTypes.address) {
        // We assume the db is ready to receive this field. If not, it will fail so we can later fix it.
        // Add city.
        paramNames.push(fieldSub.id+'City');
        paramValues.push(fieldSub.value.city);
        // Add street.
        paramNames.push(fieldSub.id+'Street');
        paramValues.push(fieldSub.value.street);
        // Add number.
        paramNames.push(fieldSub.id+'Number');
        paramValues.push(fieldSub.value.number);
    }
    // Special treatment for date fields values.
    else if (fieldType === fieldTypes.date) {
        var date = new Date(fieldSub.value);
        paramNames.push(fieldSub.id);
        paramValues.push(date.toMysqlDate())
    }
    // All other fields are strings, no need to manipulate the param name, and we can just take the value as is.
    else {
        paramNames.push(fieldSub.id);
        paramValues.push(fieldSub.value)
    }
}

/**
 * A helper function that adds the given userId to the given paramNames and paramValues lists.
 * @param userId - the user id to add to the lists.
 * @param paramNames - the paramNames list to add the given fieldSub to.
 * @param paramValues - the paramValues list to add the given fieldSub to.
 */
function addUserIdToDbLists(userId, paramNames, paramValues) {
    paramNames.push('userId');
    paramValues.push(userId);
}

/**
 * Goes over the given param names, and return a new param names array with unique names.
 * @returns {Array}
 */
function parseGroupParamNames(paramNames) {
    var namesDict = {};
    var uniqueNames = [];

    for (var i = 0; i < paramNames.length; i++) {
        var name = paramNames[i];
        if (!namesDict[name]) {
            namesDict[name] = true;
            uniqueNames.push(name);
        }
    }

    return uniqueNames;
}

/**
 * Goes over the given paramValues and divide them into groups of paramNamesLength.
 * @returns {Array}
 */
function parseGroupParamValues(paramValues, paramNamesLength) {
    var i, j, temparray;
    var chunk = paramNamesLength;
    var groupedValues = [];

    for (i = 0, j = paramValues.length; i < j; i += chunk) {
        temparray = paramValues.slice(i,i+chunk);
        groupedValues.push(temparray)
    }

    return groupedValues;
}