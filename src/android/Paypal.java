package com.huronasolutions.plugins.PaypalPlugin;

import java.math.BigDecimal;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;
import com.paypal.android.sdk.payments.Version;

public class Paypal extends CordovaPlugin {
	private CallbackContext callback;
	

	@Override
	public boolean execute(String action, final JSONArray args,
			final CallbackContext callbackContext) throws JSONException {
		boolean retValue = true;
		if (action.equals("PaypalPaymentUI")) {

			this.callback = callbackContext;
			cordova.getThreadPool().execute(new Runnable() {

				public void run() {
					try{
					startPaypalPaymentActivity(args);
					PluginResult mPlugin = new PluginResult(
							PluginResult.Status.NO_RESULT);
					mPlugin.setKeepCallback(true);
					callbackContext.sendPluginResult(mPlugin);
					}
					catch(JSONException jsEx){
						
						callbackContext.error(jsEx.getMessage());
					}
					// callbackContext.success(); // Thread-safe.
				}
			});
			retValue = true;
		} else if (action.equals("version")) {
			this.version();
		} else {
			retValue = false;
		}

		return retValue;
	}

	@Override
    public void onActivityResult (int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            PaymentConfirmation confirm = data.getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);
            if (confirm != null) {
                try {
                	JSONObject jsonConfirm = confirm.toJSONObject();
                    Log.i("Txt2PaypalPayment", jsonConfirm.toString(4));

                    // TODO: send 'confirm' to your server for verification.
                    // see https://developer.paypal.com/webapps/developer/docs/integration/mobile/verify-mobile-payment/
                    // for more details.
                    
					callback.success(jsonConfirm);

                } catch (JSONException e) {
                    Log.e("Txt2PaypalPayment", "an extremely unlikely failure occurred: ", e);
                }
            }
        }
        else if (resultCode == Activity.RESULT_CANCELED) {
            Log.i("Txt2PaypalPayment", "The user canceled.");
        }
        else if (resultCode == PaymentActivity.RESULT_PAYMENT_INVALID) {
            Log.i("Txt2PaypalPayment", "An invalid payment was submitted. Please see the docs.");
        }
    }
	
	private void startPaypalPaymentActivity(JSONArray args)
			throws JSONException {
		if (args.length() != 5) {
			this.callback
					.error("PaypalPaymentUI requires precisely five arguments");
			return;
		}
		String clientId = args.getString(0);
		String email = args.getString(1);
		String payerId = args.getString(2);
		String environment = args.getString(3);
		JSONObject paymentObject = args.getJSONObject(4);

		String amount = paymentObject.getString("amount");
		String currency = paymentObject.getString("currency");
		String shortDescription = paymentObject.getString("shortDescription");

		PayPalPayment thingToBuy = new PayPalPayment(new BigDecimal(amount),
				currency, shortDescription);

		Intent intent = new Intent(cordova.getActivity(), PaymentActivity.class);

		intent.putExtra(PaymentActivity.EXTRA_PAYPAL_ENVIRONMENT,
				resolveEnvironment(environment));
		intent.putExtra(PaymentActivity.EXTRA_RECEIVER_EMAIL, email);

		intent.putExtra(PaymentActivity.EXTRA_CLIENT_ID, clientId);
		intent.putExtra(PaymentActivity.EXTRA_PAYER_ID, payerId);
		intent.putExtra(PaymentActivity.EXTRA_PAYMENT, thingToBuy);
		this.cordova.startActivityForResult((CordovaPlugin) this, intent, 0);
	}

	// internal implementation

	private String resolveEnvironment(String env) {
		if (env.equals("PayPalEnvironmentNoNetwork")) {
			return PaymentActivity.ENVIRONMENT_NO_NETWORK;
		} else if (env.equals("PayPalEnvironmentProduction")) {
			return PaymentActivity.ENVIRONMENT_LIVE;
		} else if (env.equals("PayPalEnvironmentSandbox")) {
			return PaymentActivity.ENVIRONMENT_SANDBOX;
		} else {
			return PaymentActivity.ENVIRONMENT_NO_NETWORK;
		}
	}

	private void version() {
		this.callback.success(Version.PRODUCT_VERSION);
	}

	@Override
	public void onDestroy() {
		this.cordova.getActivity().stopService(
				new Intent(this.cordova.getActivity(), PayPalService.class));
		super.onDestroy();
	}
}