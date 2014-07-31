var counter = require("counter");
var db = require("examples/db");
var future = require("future");

exports.handle = function (req) {
    var tokens = req.uri.split("/");
    tokens.shift();
    tokens.shift();
    var operator = tokens.shift();
    switch (operator) {
        case "counter":
            return new Response('Hello ' + req.uri + ' ' + counter.count());
        case "futureCounter":
            return future.create(function () {
                return new Response('Hello future ' + req.uri + ' ' + counter.count());
            });
        case "insert":
            db.insert(tokens[0], tokens[1]);
            break;
        case "find":
            db.find.apply(db.find, tokens);
            break;
        case "findOne":
            db.findOne.apply(db.findOne, tokens);
            break;
        default:
            break;
    }
    return new Response("Hello World");
}
