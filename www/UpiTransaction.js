var exec = require('cordova/exec');

var PLUGIN_NAME = 'UpiIntent';

var UpiIntent = {
  startUpiActivity: function(options, success, error) {
    exec(success, error, PLUGIN_NAME, 'startUpiActivity', [options]);
  }
};

module.exports = UpiIntent;