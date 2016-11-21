exports.sendJsonResponseSuccess = function(response, json)
{
    // Set response as successful and json data type.
    response.writeHead(200, {'Content-Type': 'text/json'});

    // Return the json as string.
    response.end(JSON.stringify(json));
};
