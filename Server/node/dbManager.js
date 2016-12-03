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
};

exports.getTestData = function(callback){

    con.query('SELECT * FROM testschema.users',function(err,rows){
        if(err) throw err;

        console.log('Data received from Db:\n');
        console.log(rows);

        callback(rows);
    });
};

exports.saveLocation = function(lat, long, callback){
    con.query(
        'INSERT INTO `testschema`.`locations` (`lat`,`long`) VALUES (?, ?);',
        [mysql.escape(lat), mysql.escape(long)],
        function(err,res){
            if(err) {
                console.log("Error saving location: ", err);
                callback(false);
            }
            else {
                console.log("Insereted to DB: ", res.insertId);
                callback(true);
            }
    });
};
