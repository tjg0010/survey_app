// ********************** Dependencies **********************
var express = require('express');
var db = require('./dbManager.js');
var bodyParser = require("body-parser");
var http = require('./httpHelper.js');
var fs = require('fs');
// **********************************************************

var app = express();

// This is needed to parse post body.
app.use(bodyParser.urlencoded({extended: false}));
app.use(bodyParser.json());

app.get('/register', function (req, res) {
    // To support hebrew, the surveryRegister.json file was saved with UTF-8-BOM encoding.
    // Also, we use fs.readFileSync with utf8 encoding, and do not parse the file as json with stringify
    // (we just send the text using http.sendTextResponseSuccess.
    console.log('/register called');
    var survey = fs.readFileSync('surveys/surveyRegister.json', 'utf8');
    http.sendTextResponseSuccess(res, survey);
});

app.post('/location/:userId', function (req, res) {
    if (req.params.userId) {
        console.log('/saveLocation called. User id: %s', req.params.userId);
    }

    //req.params.userId

    var lat = req.body.lat;
    var long = req.body.long;

    db.saveLocation(lat, long, function (isSuccess) {
        if (isSuccess) {
            res.end("Success");
        }
        else {
            res.end("Fail");
        }
    });
});

// app.get('/', function (req, res) {
//     // TODO: deal with errors, etc...
//     db.getTestData(function (rows) {
//         var result = [];
//         for (var i = 0; i < rows.length; i++) {
//             result.push(rows[i].name);
//         }
//
//         http.sendJsonResponseSuccess(res, result);
//     });
// });

var server = app.listen(8888, function () {

    var host = server.address().address;
    var port = server.address().port;

    db.connect();

    console.log("App is listening at http://%s:%s", host, port);
});