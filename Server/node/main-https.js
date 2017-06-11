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

// region: https

// returns an instance of node-greenlock with additional helper methods
var lex = require('greenlock-express').create({
    // set to https://acme-v01.api.letsencrypt.org/directory in production
    server: 'staging'

// If you wish to replace the default plugins, you may do so here
//
    , challenges: { 'http-01': require('le-challenge-fs').create({ webrootPath: '/tmp/acme-challenges' }) }
    , store: require('le-store-certbot').create({ webrootPath: '/tmp/acme-challenges' })

// You probably wouldn't need to replace the default sni handler
// See https://git.daplie.com/Daplie/le-sni-auto if you think you do
//, sni: require('le-sni-auto').create({})

    , approveDomains: approveDomains
});

// handles acme-challenge and redirects to https
require('http').createServer(lex.middleware(require('redirect-https')())).listen(8090, function () {
    console.log("Listening for ACME http-01 challenges on", this.address());
});
// endregion

utils.init();
var app = express();
var surveysConfig;
var surveyRegister;
var surveyDiary;



// This is needed to parse post body.
app.use(bodyParser.urlencoded({extended: false}));
app.use(bodyParser.json());

app.get('/hello/:userId', function (req, res) {
    logger.log('info', '/hello/:userId (GET) called');

    // Parameter validation.
    if (!req.params.userId) {
        logger.log('error', '/hello/:userId (GET) called without mandatory parameter.', {missingParameter: 'params.userId'});
        httpHelper.sendResponseError(res, 400, 'userId was null or empty');
        return;
    }

    // Extract parameter.
    var userId = req.params.userId;

    // Check if this user id already exists in the db's main table.
    db.isUserExists(userId, surveyRegister.metadata.name, function(err, isUserExists) {
        if (!err && isUserExists != undefined && isUserExists != null) {
            // Mark if the user is already registered or not.
            surveysConfig.isUserRegistered = isUserExists;
            httpHelper.sendTextResponseSuccess(res, surveysConfig);
        }
        else {
            httpHelper.sendResponseError(res, 500, 'Failed checking if user is already registered.');
        }
    });
});

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

app.post('/bluetooth/:userId', function (req, res) {
    // Parameter validations.
    if (!req.params.userId) {
        logger.log('error', '/bluetooth/:userId (POST) called without mandatory parameter.', {missingParameter: 'params.userId'});
        httpHelper.sendResponseError(res, 400, 'userId was null or empty');
        return;
    }
    if (!req.body  || !req.body.length) {
        logger.log('error', '/bluetooth/:userId (POST) called without mandatory parameter.', {missingParameter: 'body'});
        httpHelper.sendResponseError(res, 400, 'body was null or empty');
        return;
    }

    // Extract parameters.
    var userId = req.params.userId;
    var samples = req.body;

    logger.log('info', '/bluetooth/:userId (POST) called. User id: %s', userId);

    db.saveBluetoothSamples(userId, samples, function (err) {
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

/**
 * A generic function to save an incoming survey. Used to save the registration survey and the diary surveys.
 * @param req - the current request object.
 * @param res - the current response object.
 * @param apiName - the name of the invoked web API (for logging purposes).
 * @param survey - the received survey.
 */
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

// Used for https.
function approveDomains(opts, certs, cb) {
    // This is where you check your database and associated email addresses with domains and agreements and such


    // The domains being approved for the first time are listed in opts.domains
    // Certs being renewed are listed in certs.altnames
    if (certs) {
        opts.domains = certs.altnames;
    }
    else {
        opts.email = 'tau.survey.app@gmail.com';
        opts.agreeTos = true;
    }

    // NOTE: you can also change other options such as `challengeType` and `challenge`
    // opts.challengeType = 'http-01';
    // opts.challenge = require('le-challenge-fs').create({});

    cb(null, { options: opts, certs: certs });
}




/**
 * Tha main function that starts the server listener.
 */
var server = require('https').createServer(lex.httpsOptions, lex.middleware(app)).listen(8091, function () {
    logger.log("Listening for ACME tls-sni-01 challenges and serve app on", this.address());

    var host = server.address().address;
    var port = server.address().port;

    // Connect to db.
    db.connect();

    // Load the survey json from file system.
    // To support hebrew, the surveryRegister.json file was saved with UTF-8 encoding.
    // We use fs.readFileSync with utf8 encoding, and parse the file with JSON.parse.
    surveyRegister = JSON.parse(fs.readFileSync('surveys/surveyRegister.json', 'utf8'));
    surveyDiary = JSON.parse(fs.readFileSync('surveys/surveyDiary.json', 'utf8'));

    // Load the surveys configuration.
    surveysConfig = JSON.parse(fs.readFileSync('surveys/config.json', 'utf8'));

    // Feed the survey manager with the surveys, so it can later handle it on save and load.
    sm.loadSurvey(surveyRegister);
    sm.loadSurvey(surveyDiary);

    logger.log('info', 'App is listening at https://%s:%s', host, port);
});

// var server = app.listen(8090, function () {
//     var host = server.address().address;
//     var port = server.address().port;
//
//     // Connect to db.
//     db.connect();
//
//     // Load the survey json from file system.
//     // To support hebrew, the surveryRegister.json file was saved with UTF-8 encoding.
//     // We use fs.readFileSync with utf8 encoding, and parse the file with JSON.parse.
//     surveyRegister = JSON.parse(fs.readFileSync('surveys/surveyRegister.json', 'utf8'));
//     surveyDiary = JSON.parse(fs.readFileSync('surveys/surveyDiary.json', 'utf8'));
//
//     // Load the surveys configuration.
//     surveysConfig = JSON.parse(fs.readFileSync('surveys/config.json', 'utf8'));
//
//     // Feed the survey manager with the surveys, so it can later handle it on save and load.
//     sm.loadSurvey(surveyRegister);
//     sm.loadSurvey(surveyDiary);
//
//     logger.log('info', 'App is listening at http://%s:%s', host, port);
// });
