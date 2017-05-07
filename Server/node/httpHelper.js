/**
 * Sends a successful (200) response message with the given json object.
 * @param res - the response object.
 * @param json - the json object to be sent to the caller, after stringify-ing.
 */
exports.sendJsonResponseSuccess = function(res, json)
{
    // Set response as successful and json data type.
    res.status(200);
    res.set('Content-Type', 'text/json');

    // Send the json.
    res.json(json);
};

/**
 * Sends a successful (200) response message with the given text.
 * @param res - the response object.
 * @param text - the text to be sent to the caller (assumed to be json).
 */
exports.sendTextResponseSuccess = function(res, text)
{
    // Set response as successful and json data type.
    res.status(200);
    res.set('Content-Type', 'text/json');

    // Send the json text.
    res.send(text);
};

/**
 * Sends an empty successful (200) response message.
 * @param res - the response object.
 */
exports.sendResponseSuccess = function(res) {
    // Set response as successful and json data type.
    res.set('Content-Type', 'text/json');
    res.status(200);

    // Send an empty response.
    res.end();
};

/**
 * Sends and error response to the caller.
 * @param res - the response object.
 * @param status - the desired status code to be sent to the caller.
 * @param msg - a custom error message to be added to the response.
 */
exports.sendResponseError = function(res, status, msg) {
    // Set the requested status code and content type as plain text.
    res.status(status);
    res.set('Content-Type', 'text/plain');

    // Send the error message.
    res.send(msg);
};
