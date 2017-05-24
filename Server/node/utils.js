/**
 * Pads a given digit to 2 numbers.
 * @param d - the digit to pad.
 * @returns {*}
 */
function twoDigits(d) {
    if(0 <= d && d < 10) return "0" + d.toString();
    if(-10 < d && d < 0) return "-0" + (-1*d).toString();
    return d.toString();
}

exports.init = function() {
    /**
     * Converts the date to its mySql DATE format.
     * @returns {string}
     */
    Date.prototype.toMysqlDate = function() {
        return this.getFullYear() + "-" + twoDigits(1 + this.getMonth()) + "-" + twoDigits(this.getDate());
    };

    /**
     * Converts the date to its mySql DATETIME format.
     * @returns {string}
     */
    Date.prototype.toMysqlDateTime = function() {
        return this.getFullYear() + "-" + twoDigits(1 + this.getMonth()) + "-" + twoDigits(this.getDate()) + " " + twoDigits(this.getHours()) + ":" + twoDigits(this.getMinutes()) + ":" + twoDigits(this.getSeconds());
    };
};


