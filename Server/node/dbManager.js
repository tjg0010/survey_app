// region: Dependencies
const logger = require('./logger.js');         // Our own logger.
const mysql = require("mysql");             // MySql DB connector.
// endregion

// A map that holds all table names (as values) and their name representation in the json file (as keys).

var con = mysql.createConnection({
    host: "localhost",
    user: "root",
    password: "Ran1234"
});

exports.connect = function()
{
    con.connect(function(err){
        if(err){
            logger.log('error', 'Error connecting to Db', {error: err});
            return;
        }
        logger.log('info', 'Connection established to db');
    });
};

exports.disconnect = function(){
    con.end(function(err) {
        // The connection is terminated gracefully
        // Ensures all previously enqueued queries are still
        // before sending a COM_QUIT packet to the MySQL server.
    });
};

exports.saveLocation = function(userId, lat, long, time, callback){
    // Convert the given time in UTC format, to mySql DATETIME format.
    var datetime = new Date(parseInt(time)).toMysqlDateTime();

    con.query(
        // Using ? to supply values auto escapes them to a sql injection safe format.
        'INSERT INTO `tausurvey`.`locations` (`userId`,`latitude`,`longitude`,`time`) VALUES (?, ?, ?, ?);',
        [userId, lat, long, datetime],
        function(err,res){
            if(err) {
                logError('Error saving location.', err);
                callback(err);
            }
            else {
                logger.log('info', 'Data inserted to DB.', {id: res.insertId});
                callback();
            }
    });
};

exports.saveLocationsBulk = function(userId, locations, callback) {
    var paramValuesStrings = [];

    // The paramValuesGroup is an array of paramValues arrays, since groups can have repeated items.
    // We parse each paramValues list separately.
    for (var i = 0; i < locations.length; i++) {
        var location = locations[i];
        // Only add this location if all of its parameters are valid.
        if (location && location.latitude && location.longitude && location.time) {
            // Convert the given time in UTC format, to mySql DATETIME format.
            var datetime = new Date(parseInt(location.time)).toMysqlDateTime();
            var parsedValues = escapeDataArray([userId, location.latitude, location.longitude, datetime]);
            paramValuesStrings.push('(' + parsedValues.join(',') + ')');
        }
    }

    con.query(
        'INSERT INTO `tausurvey`.`locations` (`userId`,`latitude`,`longitude`,`time`)' + ' VALUES ' + paramValuesStrings.join(',') + ';',
            function(err,res){
                if(err) {
                    logError('Error saving location.', err);
                    callback(err);
                }
                else {
                    logger.log('info', 'Data inserted to DB.', {id: res.insertId});
                    callback();
                }
            });
};

exports.saveSurvey = function(surveyName, paramNames, paramValues, callback) {
    // Only do something if we got a recognized survey.
    if (surveyName) {
        var parsedValues = escapeDataArray(paramValues);

        con.query(
            'INSERT INTO `tausurvey`.`' + surveyName + '` '                 // the table name.
            + '(' + paramNames.join(',') + ')'                              // param names.
            + ' VALUES ( ' + parsedValues.join(',') + ');',                 // param values.
            function(err,res){
                if(err) {
                    logError('Error saving survey.', err);
                    callback(err);
                }
                else {
                    logger.log('info', 'Data inserted to DB (saveSurvey).', {id: res.insertId});
                    callback();
                }
            });
    }
    // Otherwise, throw an exception.
    else {
        logger.log('error', 'dbManager.saveSurvey got a null surveyName or an unrecognized one');
        throw 'dbManager.saveSurvey got a null surveyName or an unrecognized one';
    }
};

exports.saveSurveyGroup = function(surveyName, paramNames, paramValuesGroup, userId, callback) {
    // Only do something if we got a recognized survey.
    if (surveyName) {
        // Add the userId to the param names.
        paramNames.push('userId');

        var paramValuesStrings = [];

        // The paramValuesGroup is an array of paramValues arrays, since groups can have repeated items.
        // We parse each paramValues list separately.
        for (var i = 0; i < paramValuesGroup.length; i++) {
            var parsedValues = escapeDataArray(paramValuesGroup[i]);
            // Also add the userId to each list.
            paramValuesStrings.push('(' + parsedValues.join(',') + ',' + mysql.escape(userId) + ')');
        }

        con.query(
            'INSERT INTO `tausurvey`.`' + surveyName + '` '                 // the table name.
            + '(' + paramNames.join(',') + ')'                              // param names.
            + ' VALUES ' + paramValuesStrings.join(',') + ';',              // param values arrays.
            function(err,res){
                if(err) {
                    logError('Error saving survey group.', err);
                    callback(err);
                }
                else {
                    logger.log('info', 'Data inserted to DB (saveSurveyGroup).', {id: res.insertId});
                    callback();
                }
            });
    }
    // Otherwise, throw an exception.
    else {
        logger.log('error', 'dbManager.saveSurvey got a null surveyName or an unrecognized one');
        throw 'dbManager.saveSurvey got a null surveyName or an unrecognized one';
    }
};

exports.getSurveyEnrichmentData = function(tableName, paramName, userId, callback) {
    logger.log('info', 'dbManager.getSurveyEnrichmentData called.', {tableName: tableName, paramName: paramName, userId: userId});

    con.query('SELECT ' + paramName + ' FROM tausurvey.' + tableName + ' WHERE userId = ?', userId, function(err, rows){
        if(err) {
            logError('Error selecting survey enrichment data.', err);
            callback(err);
        }
        else {
            logger.log('info', 'dbManager.getSurveyEnrichmentData found ' + rows.length + ' results.');
            callback(null, rows);
        }
    });
};

exports.isUserExists = function(userId, mainTableName, callback) {
    logger.log('info', 'dbManager.isUserExists called.', {userId: userId,mainTableName: mainTableName});

    con.query('SELECT userId FROM tausurvey.' + mainTableName + ' WHERE userId = ?', userId, function(err, rows){
        if(err) {
            logError('Error checking if user id exists in db.', err);
            callback(err);
        }
        else {
            logger.log('info', 'dbManager.isUserExists found ' + rows.length + ' users with the given user id.');
            callback(null, (rows.length > 0));
        }
    });
};

/**
 * Goes over the given data array and runs mysql.escape on each value.
 * @param dataArray - the array to escape for the db.
 * @returns {Array}
 */
function escapeDataArray(dataArray) {
    var escapedArray = [];

    // Go over all the data we got and escape each value.
    for (var i = 0; i < dataArray.length; i++) {
        escapedArray.push(mysql.escape(dataArray[i]));
    }

    return escapedArray;
}

function logError(title, err) {
    logger.log('error', title, {error: err});
    if (err.message) {
        logger.log('error', 'error message', {message: err.message});
    }
}