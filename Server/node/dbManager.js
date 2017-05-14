// region: Dependencies
const winston = require('winston');         // Logging.
const mysql = require("mysql");             // MySql DB connector.
// endregion

// A map that holds all table names (as values) and their name representation in the json file (as keys).
var surveyTableMap = { registration: 'main', children: 'children' };

var con = mysql.createConnection({
    host: "localhost",
    user: "root",
    password: "Ran1234"
});

exports.connect = function()
{
    con.connect(function(err){
        if(err){
            winston.log('error', 'Error connecting to Db', {error: err});
            return;
        }
        winston.log('info', 'Connection established to db');
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
        'INSERT INTO `tausurvey`.`locations` (`userId`,`lat`,`long`,`time`) VALUES (?, ?, ?, ?);',
        [userId, lat, long, datetime],
        function(err,res){
            if(err) {
                logError('Error saving location.', err);
                callback(err);
            }
            else {
                winston.log('info', 'Data inserted to DB.', {id: res.insertId});
                callback();
            }
    });
};

exports.saveSurvey = function(surveyName, paramNames, paramValues, callback) {
    // Only do something if we got a recognized survey.
    if (surveyName && surveyTableMap[surveyName]) {
        var parsedValues = escapeDataArray(paramValues);

        con.query(
            'INSERT INTO `tausurvey`.`' + surveyTableMap[surveyName] + '` ' // the table name.
            + '(' + paramNames.join(',') + ')'                              // param names.
            + ' VALUES ( ' + parsedValues.join(',') + ');',                 // param values.
            function(err,res){
                if(err) {
                    logError('Error saving survey.', err);
                    callback(err);
                }
                else {
                    winston.log('info', 'Data inserted to DB (saveSurvey).', {id: res.insertId});
                    callback();
                }
            });
    }
    // Otherwise, throw an exception.
    else {
        winston.log('error', 'dbManager.saveSurvey got a null surveyName or an unrecognized one');
        throw 'dbManager.saveSurvey got a null surveyName or an unrecognized one';
    }
};

exports.saveSurveyGroup = function(surveyName, paramNames, paramValuesGroup, userId, callback) {
    // Only do something if we got a recognized survey.
    if (surveyName && surveyTableMap[surveyName]) {
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
            'INSERT INTO `tausurvey`.`' + surveyTableMap[surveyName] + '` ' // the table name.
            + '(' + paramNames.join(',') + ')'                              // param names.
            + ' VALUES ' + paramValuesStrings.join(',') + ';',              // param values arrays.
            function(err,res){
                if(err) {
                    logError('Error saving survey group.', err);
                    callback(err);
                }
                else {
                    winston.log('info', 'Data inserted to DB (saveSurveyGroup).', {id: res.insertId});
                    callback();
                }
            });
    }
    // Otherwise, throw an exception.
    else {
        winston.log('error', 'dbManager.saveSurvey got a null surveyName or an unrecognized one');
        throw 'dbManager.saveSurvey got a null surveyName or an unrecognized one';
    }
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
    winston.log('error', title, {error: err});
    if (err.message) {
        winston.log('error', 'error message', {message: err.message});
    }
}