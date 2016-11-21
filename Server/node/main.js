// ********************** Dependencies **********************
var express = require('express');
var db = require('./dbManager.js');
var http = require('./httpHelper.js');
// **********************************************************

var app = express();

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

app.post('/saveUser', function (req, res) {
    // TODO: get data from request and save to db.
    //data = JSON.parse( req.data? );

    // TODO: return a respose.
    //res.end( JSON.stringify(data));
});

var server = app.listen(8081, function () {

    var host = server.address().address;
    var port = server.address().port;

    db.connect();

    console.log("App is listening at http://%s:%s", host, port);
});