// ********************** Dependencies **********************
var express = require('express');
var db = require('./dbManager.js');
var http = require('./httpHelper.js');
// **********************************************************

var app = express();

// This is needed to parse post body.
app.use(bodyParser.urlencoded({ extended: false }));
app.use(bodyParser.json());

app.get('/', function (req, res) {
    // TODO: deal with errors, etc...
    db.getTestData(function(rows) {
        var result = [];
        for (var i = 0; i < rows.length; i++) {
            result.push(rows[i].name);
        }

        http.sendJsonResponseSuccess(res, result);
    });
});

app.post('/saveLocation', function (req, res) {

    var lat = req.body.lat;
    var long = req.body.long;

    db.saveLocation(lat, long, function(isSuccess) {
        if (isSuccess) {
            res.end("Success");
        }
        else {
            res.end("Fail");
        }
    });
});

var server = app.listen(8888, function () {

    var host = server.address().address;
    var port = server.address().port;

    db.connect();

    console.log("App is listening at http://%s:%s", host, port);
});