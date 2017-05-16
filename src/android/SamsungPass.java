package com.cordova.plugin.android.samsungpass;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.SparseArray;
import android.util.Log;

import com.samsung.android.sdk.SsdkUnsupportedException;
import com.samsung.android.sdk.pass.Spass;
import com.samsung.android.sdk.pass.SpassFingerprint;
import com.samsung.android.sdk.pass.SpassInvalidStateException;

import org.apache.cordova.CordovaWebView;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.PluginResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SamsungPass extends CordovaPlugin {
    public static final String TAG = "SamsungPass";

    public static String packageName;
    public static Context mContext;
    public static Activity mActivity;

    public static SpassFingerprint mSpassFingerprint;
    public static Spass mSpass;
    public static SpassFingerprint.IdentifyListener listener;

    public static CallbackContext mCallbackContext;
    public static PluginResult mPluginResult;

    /**
     * Constructor.
     */
    public SamsungPass() {
    }

    /**
     * Sets the context of the Command. This can then be used to do things like
     * get file paths associated with the Activity.
     *
     * @param cordova The context of the main Activity.
     * @param webView The CordovaWebView Cordova is running in.
     */
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        Log.v(TAG, "Init SamsungPass");

        // Write initialize code here
        packageName = cordova.getActivity().getApplicationContext().getPackageName();
        mPluginResult = new PluginResult(PluginResult.Status.NO_RESULT);
        mActivity = cordova.getActivity();
        mContext = cordova.getActivity().getApplicationContext();
        mSpass = new Spass();
    }

    /**
     * Executes the request and returns PluginResult.
     *
     * @param action          The action to execute.
     * @param args            JSONArry of arguments for the plugin.
     * @param callbackContext The callback id used when calling back into JavaScript.
     * @return A PluginResult object with a status and message.
     */
    public boolean execute(final String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        mCallbackContext = callbackContext;
        
        Log.v(TAG, "SamsungPass action: " + action);

        listener = new SpassFingerprint.IdentifyListener() {
            @Override
            public void onFinished(int eventStatus) {
                JSONObject resultJson = new JSONObject();

                // It is called when fingerprint identification is finished.
                if (eventStatus == SpassFingerprint.STATUS_AUTHENTIFICATION_SUCCESS) {
                    // Identify operation succeeded with fingerprint
                    try {
                        resultJson.put("withFingerprint", true);
                        mPluginResult = new PluginResult(PluginResult.Status.OK);
                        mCallbackContext.success(resultJson);
                    }
                    catch(Exception ex) {
                        mPluginResult = new PluginResult(PluginResult.Status.ERROR);
                        mCallbackContext.error(ex.getMessage());
                        mCallbackContext.sendPluginResult(mPluginResult);
                    }
                } else if (eventStatus == SpassFingerprint.STATUS_AUTHENTIFICATION_PASSWORD_SUCCESS) {
                    // Identify operation succeeded with alternative password
                    try {
                        resultJson.put("withFingerprint", false);
                        mPluginResult = new PluginResult(PluginResult.Status.OK);
                        mCallbackContext.success(resultJson);
                    }
                    catch(Exception ex) {
                        mPluginResult = new PluginResult(PluginResult.Status.ERROR);
                        mCallbackContext.error(ex.getMessage());
                        mCallbackContext.sendPluginResult(mPluginResult);
                    }
                } else {
                    // Identify operation failed with given eventStatus. 
                    // STATUS_TIMEOUT_FAILED
                    // STATUS_USER_CANCELLED
                    // STATUS_AUTHENTIFICATION_FAILED
                    // STATUS_QUALITY_FAILED
                    // STATUS_USER_CANCELLED_BY_TOUCH_OUTSIDE
                    // STATUS_BUTTON_PRESSED
                    // STATUS_OPERATION_DENIED
                    mPluginResult = new PluginResult(PluginResult.Status.ERROR);
                    switch(eventStatus) {
                        case SpassFingerprint.STATUS_TIMEOUT_FAILED:
                            mCallbackContext.error("STATUS_TIMEOUT_FAILED");
                            break;
                        case SpassFingerprint.STATUS_USER_CANCELLED:
                            mCallbackContext.error("STATUS_USER_CANCELLED");
                            break;
                        case SpassFingerprint.STATUS_AUTHENTIFICATION_FAILED:
                            mCallbackContext.error("STATUS_AUTHENTIFICATION_FAILED");
                            break;
                        case SpassFingerprint.STATUS_QUALITY_FAILED:
                            mCallbackContext.error("STATUS_QUALITY_FAILED");
                            break;
                        case SpassFingerprint.STATUS_USER_CANCELLED_BY_TOUCH_OUTSIDE:
                            mCallbackContext.error("STATUS_USER_CANCELLED_BY_TOUCH_OUTSIDE");
                            break;
                        case SpassFingerprint.STATUS_BUTTON_PRESSED:
                            mCallbackContext.error("STATUS_BUTTON_PRESSED");
                            break;
                        case SpassFingerprint.STATUS_OPERATION_DENIED:
                            mCallbackContext.error("STATUS_OPERATION_DENIED");
                            break;
                        default:
                            break;
                    }
                }
                mCallbackContext.sendPluginResult(mPluginResult);
            }

            @Override
            public void onReady() {
                // It is called when fingerprint identification is ready after
                // startIdentify() is called.
            }

            @Override
            public void onStarted() {

                // It is called when the user touches the fingerprint sensor after
                // startIdentify() is called.
            }

            @Override
            public void onCompleted() {
                //It is called when identify request is completed.
            }
        };

        // Get passed parameters
        final JSONObject params = args.getJSONObject(0);

        // Check if the params has language , then change the locale if it exists
        if(params.has("lang")) {
            final String lang = params.getString("lang");

            Locale locale = new Locale(lang);
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            Resources resources = mActivity.getResources();
            resources.updateConfiguration(config, resources.getDisplayMetrics());
            cordova.getActivity().runOnUiThread(new Runnable() {
                mActivity.recreate();
            });
        }

        JSONObject resultJson = new JSONObject();

        try {
            mSpass.initialize(mActivity);
        } catch (SsdkUnsupportedException e) {
            // Error handling
            mPluginResult = new PluginResult(PluginResult.Status.ERROR);
            mCallbackContext.error(e.getMessage());
            mCallbackContext.sendPluginResult(mPluginResult);
            return false;
        } catch (UnsupportedOperationException e) {
            // Error handling
            mPluginResult = new PluginResult(PluginResult.Status.ERROR);
            mCallbackContext.error(e.getMessage());
            mCallbackContext.sendPluginResult(mPluginResult);
            return false;
        }

        mSpassFingerprint = new SpassFingerprint(mActivity);
        boolean isFeatureEnabled = mSpass.isFeatureEnabled(Spass.DEVICE_FINGERPRINT);
        boolean mHasRegisteredFinger = mSpassFingerprint.hasRegisteredFinger();

        if (action.equals("availability")) {
            try {
                resultJson.put("isAvailable", isFeatureEnabled);
                resultJson.put("hasEnrolledFingerprints", mHasRegisteredFinger);
                mPluginResult = new PluginResult(PluginResult.Status.OK);
                mCallbackContext.success(resultJson);
                mCallbackContext.sendPluginResult(mPluginResult);
                return true;
            }
            catch (Exception ex) {
                mPluginResult = new PluginResult(PluginResult.Status.ERROR);
                mCallbackContext.error(ex.getMessage());
                mCallbackContext.sendPluginResult(mPluginResult);
                return false;
            }
        }
        else if (action.equals("verify")) {
            cordova.getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    mSpassFingerprint.startIdentifyWithDialog(mActivity, listener, false);
                }
            });
            mPluginResult.setKeepCallback(true);
            return true;
        }
        else {
            mPluginResult = new PluginResult(PluginResult.Status.ERROR);
            mCallbackContext.error("Action '" + action + "' does not exist !");
            mCallbackContext.sendPluginResult(mPluginResult);
            return false;
        }
    }
}