function random(upDiapazon) {
    return Math.floor(Math.random() * upDiapazon);
}

function isNumeric(n) {
    return !isNaN(parseFloat(n)) && isFinite(n);
}

/** round decimal points for point */
const dpPnt = 4;

/** round decimal points for color */
const dpClr = 4;

/** round decimal points for time */
const dpTm  = 4;

function round(n, decimalPoints) {
    if (arguments.length < 2)
        decimalPoints = 2;
    var multiplicator = Math.pow(10, decimalPoints);
    n = parseFloat((n * multiplicator).toFixed(11));
    var test = Math.round(n) / multiplicator;
    return +(test.toFixed(decimalPoints));
}

var hasMinDiff = function(double1, double2) {
    return Math.abs(double1 - double2) < 0.0001;
};

function toInt (value) {
    return value | 0;
//    var intvalue = Math.floor( floatvalue );
//    var intvalue = Math.ceil( floatvalue );
//    var intvalue = Math.round( floatvalue );
//    var intvalue = Math.trunc( floatvalue ); // `Math.trunc` was added in ECMAScript 6
}

/** setTimeout as Promise */
var onTimer = (milliseconds) => Promise(function(resolve, reject) {
        var timerId = setTimeout( function() { resolve(timerId); }, milliseconds);
    })
    .then(function(timerId) {
        clearTimeout(timerId);
    });
