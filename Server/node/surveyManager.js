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
                    addFieldToDbLists(fieldSub, expectedFields[fieldSub.id].type, paramsDict.main.paramNames, paramsDict.main.paramValues);
                }
                // Treatment for fields which ARE a part of a group.
                else {
                    var groupId = fieldSub.groupId;
                    // We store the group's fields data in a different data structure, since they may come in repetitions.
                    if (!paramsDict[groupId]) {
                        // tauServerParamNames is used to collect the param names of this group, so we can later on form an insert query for those params.
                        paramsDict[groupId] = { tauServerParamNames: {} };
                    }
                    // 1 is the first repetition (repetitions are not zero based). We set 1 as the default value (for cases in which we didn't get a repetitionValue).
                    var repetitionValue = fieldSub.groupRepetitionValue || 1;
                    if (!paramsDict[groupId][repetitionValue]) {
                        paramsDict[groupId][repetitionValue] = {};
                    }
                    // Add this field submission value according to it's param name to the dict. We will later use it.
                    paramsDict[groupId][repetitionValue][fieldSub.id] = fieldSub.value;
                    // Mark this param name as being used.
                    paramsDict[groupId].tauServerParamNames[fieldSub.id] = fieldSub;
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
                var parsedParamNames = [];
                var parsedParamValues = [];
                parseGroupParams(expectedFields[group], paramsDict[group], parsedParamNames, parsedParamValues);
                // Call saveSurvey with the groupName instead of the surveyName.
                promises.push(db.saveSurveyGroupAsync(group, parsedParamNames, parsedParamValues, userId));
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
        surveysFields[surveyName][field.id] = field;

        // If this is a group, we should run recursively on his fields and add them too.
        if (field.type === fieldTypes.group) {
            saveSurveyFields(surveyName, field);
        }
    }
}

/**
 * A helper function that adds a given field submission to the given paramNames and paramValues lists.
 * Wraps addParamNameToDbList and addParamValueToDbList to a single function.
 * This function is used to prepare the data to be saved into the db.
 * @param fieldSub - the field submission data.
 * @param fieldType - the field type (from fieldTypes).
 * @param paramNames - the paramNames list to add the given fieldSub to.
 * @param paramValues - the paramValues list to add the given fieldSub to.
 */
function addFieldToDbLists(fieldSub, fieldType, paramNames, paramValues) {
    addParamNameToDbList(fieldSub.id, fieldType, paramNames);
    addParamValueToDbList(fieldSub.value, fieldType, paramValues);
}

/**
 * A helper function that adds a given field submission to the given paramNames lists.
 * This can be used when you want to add the param names separately from the param values.
 * This function is used to prepare the data to be saved into the db.
 * @param fieldId - the field submission id.
 * @param fieldType - the field type (from fieldTypes).
 * @param paramNames - the paramNames list to add the given fieldSub to.
 */
function addParamNameToDbList(fieldId, fieldType, paramNames) {
    // Special treatment for boolean fields values.
    if (fieldType === fieldTypes.boolean) {
        paramNames.push(fieldId);
    }
    // Special treatment for address fields values.
    else if (fieldType === fieldTypes.address) {
        // We assume the db is ready to receive this field. If not, it will fail so we can later fix it.
        // Add city.
        paramNames.push(fieldId+'City');
        // Add street.
        paramNames.push(fieldId+'Street');
        // Add number.
        paramNames.push(fieldId+'Number');
    }
    // Special treatment for date fields values.
    else if (fieldType === fieldTypes.date) {
        paramNames.push(fieldId);
    }
    // All other fields are strings, no need to manipulate the param name, and we can just take the value as is.
    else {
        paramNames.push(fieldId);
    }
}

/**
 * A helper function that adds a given field submission to the given paramValues lists.
 * This can be used when you want to add the param names separately from the param values.
 * This function is used to prepare the data to be saved into the db.
 * @param fieldValue - the field submission value.
 * @param fieldType - the field type (from fieldTypes).
 * @param paramValues - the paramValues list to add the given fieldSub to.
 */
function addParamValueToDbList(fieldValue, fieldType, paramValues) {
    // Special treatment for boolean fields values.
    if (fieldType === fieldTypes.boolean) {
        var value = fieldValue ? 1 : (fieldValue === null ? null : 0);
        paramValues.push(value);
    }
    // Special treatment for address fields values.
    else if (fieldType === fieldTypes.address) {
        // We assume the db is ready to receive this field. If not, it will fail so we can later fix it.
        // Add city.
        paramValues.push(fieldValue ? fieldValue.city : null);
        // Add street.
        paramValues.push(fieldValue ? fieldValue.street : null);
        // Add number.
        paramValues.push(fieldValue ? fieldValue.number : null);
    }
    // Special treatment for date fields values.
    else if (fieldType === fieldTypes.date) {
        if (fieldValue) {
            var date = new Date(fieldValue);
            paramValues.push(date.toMysqlDate())
        } else {
            paramValues.push(null)
        }
    }
    // All other fields are strings, no need to manipulate the param name, and we can just take the value as is (even if it's null).
    else {
        paramValues.push(fieldValue)
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
 * Goes over the groupParams dictionary and created both resultParamNames and resultParamValues to use when saving to the db.
 * @param group - the group for which we are creating the params.
 * @param groupParams - the groupParams dict as described and built in the saveSurvey function.
 * @param resultParamNames - an array of param names for the db insert query.
 * @param resultParamValues - an array of value arrays, that holds the values we want to save to the db. Might have null values.
 */
function parseGroupParams(group, groupParams, resultParamNames, resultParamValues) {
    var tempRepetitions = {};
    var repetition;

    if (group) {
        // Go over all of the param names of this group.
        for (var paramName in groupParams.tauServerParamNames) {
            if (groupParams.tauServerParamNames.hasOwnProperty(paramName)) {
                var fieldSub = groupParams.tauServerParamNames[paramName];
                // Add the param name to the resultParamNames list.
                addParamNameToDbList(paramName, fieldSub.type, resultParamNames);

                // Go over all of this group's repetitions and add the value (if exists) to the repetitions temp dict.
                for (repetition in groupParams) {
                    if (groupParams.hasOwnProperty(repetition) && repetition != 'tauServerParamNames') {
                        // Initialize in the repetitions temp dict if not yet initialized.
                        if (!tempRepetitions[repetition]) {
                            tempRepetitions[repetition] = [];
                        }
                        // Add the field's value for this param if exists in this repetition. If it doesn't exist, add null.
                        addParamValueToDbList(groupParams[repetition][paramName] ? groupParams[repetition][paramName] : null, fieldSub.type, tempRepetitions[repetition]);
                    }
                }
            }
        }

        // Now go over the repetitions and add the repetition value with the paramName, but only if this is a server group.
        if (group.condition.source == "SERVER" && group.condition.conditionOn) {
            // Add the column name. It is expected to be the value after the dot in the conditionOn attribute (the value before is the table).
            resultParamNames.push(group.condition.conditionOn.split('.')[1]);

            // Add the repetition value to each repetition.
            for (repetition in groupParams) {
                if (groupParams.hasOwnProperty(repetition) && repetition != 'tauServerParamNames') {
                    tempRepetitions[repetition].push(repetition);
                }
            }
        }

        // resultParamValues should be an array that holds arrays of values. Simply convert the temp repetitions dict to this structure.
        // Add the repetition value to each repetition.
        for (repetition in groupParams) {
            if (groupParams.hasOwnProperty(repetition) && repetition != 'tauServerParamNames') {
                resultParamValues.push(tempRepetitions[repetition]);
            }
        }
    }
}

/**
 * Goes over the given paramValues and divide them into groups of paramNamesLength.
 * @returns {Array}
 */
function parseGroupParamValues(paramValues, paramNamesLength) {
    var i, j, tempArray;
    var chunk = paramNamesLength;
    var groupedValues = [];

    for (i = 0, j = paramValues.length; i < j; i += chunk) {
        tempArray = paramValues.slice(i,i+chunk);
        groupedValues.push(tempArray)
    }

    return groupedValues;
}