function SamsungPass() {

}

SamsungPass.prototype.verifyFingerprint = function(params, successCallback, errorCallback) {
    cordova.exec(
        successCallback,
        errorCallback,
        "SamsungPass", // Java Class
        "verify", // action
        [ // Array of arguments to pass to the Java class
            params
        ]
    );
};

SamsungPass.prototype.isAvailable = function(successCallback, errorCallback) {
    cordova.exec(
        successCallback,
        errorCallback,
        "SamsungPass", // Java Class
        "availability", // action
        []
    );
};

SamsungPass.prototype.changeLocale = function(lang, successCallback, errorCallback) {
    cordova.exec(
        successCallback,
        errorCallback,
        "SamsungPass", // Java Class
        "changeLocale", // action
        [{lang: lang}]
    );
};

SamsungPass = new SamsungPass();
module.exports = SamsungPass;