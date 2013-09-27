cordova.define("com.huronasolutions.plugins.PaypalPlugin.Paypal", function (require, exports, module) {
    var exec = require("cordova/exec");


    var Paypal = {
        pay: function (clientId, email, payerId, payment, environment, completionCallback, cancelCallback) {

            function success(args) {
                if (typeof successCallback === 'function')
                    completionCallback(args);
            }

            function fail(args) {
                if (typeof failCallback === 'function')
                    cancelCallback(args);
            }

            return exec(
                function (args) { success(args); },
                function (args) { fail(args); },
                'Paypal',
                'PaypalPaymentUI',
                [clientId, email, payerId,environment, payment]);
        }
    }
    module.exports = Paypal;

});