var exec = require('cordova/exec');

function ExternalStorage(){}

ExternalStorage.prototype.getRemovableStorage = function(callbackContext){
	callbackContext = callbackContext || {};
	exec(callbackContext.success || null, callbackContext.error || null, 'ExternalStorage', 'getRemovableStorage', []);
};
ExternalStorage.prototype.getRemovableStorages = function(callbackContext){
	callbackContext = callbackContext || {};
	exec(callbackContext.success || null, callbackContext.error || null, 'ExternalStorage', 'getRemovableStorages', []);
};

module.exports = new ExternalStorage();