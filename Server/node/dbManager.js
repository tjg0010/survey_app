var mysql = require("mysql");

var con = mysql.createConnection({
    host: "localhost",
    user: "root",
    password: "Ran1234"
});

exports.connect = function()
{
    con.connect(function(err){
        if(err){
            console.log('Error connecting to Db');
            console.log(err);
            return;
        }
        console.log('Connection established to db');
    });
};

exports.disconnect = function(){
    con.end(function(err) {
        // The connection is terminated gracefully
        // Ensures all previously enqueued queries are still
        // before sending a COM_QUIT packet to the MySQL server.
    });
}

exports.getTestData = function(callback){

    con.query('SELECT * FROM testschema.users',function(err,rows){
        if(err) throw err;

        console.log('Data received from Db:\n');
        console.log(rows);

        callback(rows);
    });
}

