// region: Dependencies
var express = require('express');               // Express web server.
var bodyParser = require("body-parser");        // Body parser for parsing post body.
var fs = require('fs');                         // File system.
const winston = require('winston');             // Logging.
var httpHelper = require('./httpHelper.js');    // Our own httpHelper.
var db = require('./dbManager.js');             // Our own dbManager.
var sm = require('./surveyManager.js');         // Our own surveyManager.
// endregion

var app = express();
var surveyRegister;

// This is needed to parse post body.
app.use(bodyParser.urlencoded({extended: false}));
app.use(bodyParser.json());

app.get('/register', function (req, res) {
    winston.log('info', '/register (GET) called');

    // Just send the surveyRegister object as text (no need to convert it to json using sendJsonResponseSuccess since it already is json).
    httpHelper.sendTextResponseSuccess(res, surveyRegister);
});

app.post('/register/:userId', function (req, res) {
    // Parameter validations.
    if (!req.params.userId) {
        winston.log('error', '/register (POST) called without mandatory parameter.', {missingParameter: 'params.userId'});
        httpHelper.sendResponseError(res, 400, 'request.params.userId was null or empty');
        return;
    }
    if (!req.body  || !req.body.length) {
        winston.log('error', '/register (POST) called without mandatory parameter.', {missingParameter: 'body'});
        httpHelper.sendResponseError(res, 400, 'request.body was null or empty');
        return;
    }

    // Save parameters.
    var userId = req.params.userId;
    var fieldSubmissions = req.body;

    winston.log('info', '/register (POST) called. User id: %s', userId);

    // Save the submissions to db using the surveyManager.
    sm.saveSurvey(surveyRegister.metadata.name, userId, fieldSubmissions, function(err) {
        if (!err) {
            // Send an empty successful response.
            httpHelper.sendResponseSuccess(res);
        }
        else {
            // Send an error response and log it.
            winston.log('error', '/register/:userId (POST) failed to save registration data to db.', {error: err});
            httpHelper.sendResponseError(res, 500, 'failed to save registration to db');
        }
    });
});

app.post('/location/:userId', function (req, res) {
    if (req.params.userId) {
        winston.log('info', '/saveLocation called. User id: %s', req.params.userId);
    }

    // TODO: use the userId when saving location to db.
    //req.params.userId

    var lat = req.body.lat;
    var long = req.body.long;

    db.saveLocation(lat, long, function (isSuccess) {
        if (isSuccess) {
            httpHelper.sendResponseSuccess(res);
        }
        else {
            httpHelper.sendResponseError(res, 500, 'Failed saving location to db.');
        }
    });
});

// Start the server.
var server = app.listen(8888, function () {

    var host = server.address().address;
    var port = server.address().port;

    // Connect to db.
    db.connect();

    // Load the survey json from file system.
    // To support hebrew, the surveryRegister.json file was saved with UTF-8 encoding.
    // We use fs.readFileSync with utf8 encoding, and parse the file with JSON.parse.
    surveyRegister = JSON.parse(fs.readFileSync('surveys/surveyRegister.json', 'utf8'));

    // Feed the survey manager with this survey, so it can later handle it on save.
    sm.loadSurvey(surveyRegister);

    winston.log('info', 'App is listening at http://%s:%s', host, port);
});