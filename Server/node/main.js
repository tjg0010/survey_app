// region: Dependencies
var utils = require('./utils.js');              // Our own utils lib.
var express = require('express');               // Express web server.
var bodyParser = require("body-parser");        // Body parser for parsing post body.
var fs = require('fs');                         // File system.
const logger = require('./logger.js');          // Our own logger.
var httpHelper = require('./httpHelper.js');    // Our own httpHelper.
var db = require('./dbManager.js');             // Our own dbManager.
var sm = require('./surveyManager.js');         // Our own surveyManager.
// endregion

utils.init();
var app = express();
var surveyRegister;
var surveyDiary;

// This is needed to parse post body.
app.use(bodyParser.urlencoded({extended: false}));
app.use(bodyParser.json());

app.get('/register', function (req, res) {
    logger.log('info', '/register (GET) called');

    // Just send the surveyRegister object as text (no need to convert it to json using sendJsonResponseSuccess since it already is json).
    httpHelper.sendTextResponseSuccess(res, surveyRegister);
});

app.post('/register/:userId', function (req, res) {
    saveSurvey(req, res, '/register (POST)', surveyRegister);
});

app.post('/location/:userId', function (req, res) {
    var userId = req.params.userId;
    var lat = req.body.lat;
    var long = req.body.long;
    var time = req.body.time;

    if (userId) {
        logger.log('info', '/saveLocation called. User id: %s', userId);
    }

    // Only do something if we got all required fields.
    if (userId && lat && long && time) {
        db.saveLocation(userId, lat, long, time, function (err) {
            if (!err) {
                httpHelper.sendResponseSuccess(res);
            }
            else {
                httpHelper.sendResponseError(res, 500, 'Failed saving location to db.');
            }
        });
    } else {
        // Log an error and return an error response.
        logger.log('error', '/location/:userId (GET) didn\'t get all expected parameters.',
                    {userId: userId, lat: lat, long: long, time: time});
        httpHelper.sendResponseError(res, 500, 'Failed saving location to db. Not all required parameters were supplied.');
    }
});

app.post('/location/:userId/bulk', function (req, res) {
    // Parameter validations.
    if (!req.params.userId) {
        logger.log('error', '/location/:userId/bulk (POST) called without mandatory parameter.', {missingParameter: 'params.userId'});
        httpHelper.sendResponseError(res, 400, 'userId was null or empty');
        return;
    }
    if (!req.body  || !req.body.length) {
        logger.log('error', '/location/:userId/bulk (POST) called without mandatory parameter.', {missingParameter: 'body'});
        httpHelper.sendResponseError(res, 400, 'body was null or empty');
        return;
    }

    // Extract parameters.
    var userId = req.params.userId;
    var locations = req.body;

    logger.log('info', '/location/:userId/bulk (POST) called. User id: %s', userId);

    db.saveLocationsBulk(userId, locations, function (err) {
        if (!err) {
            httpHelper.sendResponseSuccess(res);
        }
        else {
            httpHelper.sendResponseError(res, 500, 'Failed saving locations to db.');
        }
    });

});

app.get('/diary/:userId', function (req, res) {
    var userId = req.params.userId;

    logger.log('info', '/diary/:userId (GET) called', {userId: userId});

    // Only do something if we got a user id.
    if (userId) {
        sm.enrichSurvey(surveyDiary, userId, function(err, data) {
            // If the enrichment didn't have any errors and we got the data (which is the survey enriched).
            if (!err && data) {
                httpHelper.sendTextResponseSuccess(res, data);
            } else {
                // Otherwise, send an error back.
                httpHelper.sendResponseError(res, 500, 'Failed saving location to db.' + err);
            }
        });
    } else {
        // Log an error and return an error response.
        logger.log('error', '/diary/:userId (GET) called without a user id.');
        httpHelper.sendResponseError(res, 400, 'Failed loading the survey. User ID is missing.');
    }
});

app.post('/diary/:userId', function (req, res) {
    saveSurvey(req, res, '/diary (POST)', surveyDiary);
});

// Start the server.
var server = app.listen(8090, function () {

    var host = server.address().address;
    var port = server.address().port;

    // Connect to db.
    db.connect();

    // Load the survey json from file system.
    // To support hebrew, the surveryRegister.json file was saved with UTF-8 encoding.
    // We use fs.readFileSync with utf8 encoding, and parse the file with JSON.parse.
    surveyRegister = JSON.parse(fs.readFileSync('surveys/surveyRegister.json', 'utf8'));
    surveyDiary = JSON.parse(fs.readFileSync('surveys/surveyDiary.json', 'utf8'));

    // Feed the survey manager with this survey, so it can later handle it on save.
    sm.loadSurvey(surveyRegister);
    sm.loadSurvey(surveyDiary);

    logger.log('info', 'App is listening at http://%s:%s', host, port);
});


function saveSurvey(req, res, apiName, survey) {
    // Parameter validations.
    if (!req.params.userId) {
        logger.log('error', apiName +' called without mandatory parameter.', {missingParameter: 'params.userId'});
        httpHelper.sendResponseError(res, 400, 'userId was null or empty');
        return;
    }
    if (!req.body  || !req.body.length) {
        logger.log('error', apiName + ' called without mandatory parameter.', {missingParameter: 'body'});
        httpHelper.sendResponseError(res, 400, 'body was null or empty');
        return;
    }

    // Extract parameters.
    var userId = req.params.userId;
    var fieldSubmissions = req.body;

    logger.log('info', apiName + ' called. User id: %s', userId);

    // Save the submissions to db using the surveyManager.
    sm.saveSurvey(survey.metadata.name, userId, fieldSubmissions, function(err) {
        if (!err) {
            // Send an empty successful response.
            httpHelper.sendResponseSuccess(res);
        }
        else {
            // Send an error response and log it.
            logger.log('error', apiName +' (POST) failed to save registration data to db.', {error: err});
            httpHelper.sendResponseError(res, 500, 'failed to save registration to db');
        }
    });
}