﻿
    var exec = require("cordova/exec");


    var Paypal = {
        pay: function (clientId, email, payerId, payment, environment, completionCallback, cancelCallback) {

            function success(args) {
                if (typeof completionCallback === 'function')
                    completionCallback(args);
            }

            function fail(args) {
                if (typeof cancelCallback === 'function')
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

