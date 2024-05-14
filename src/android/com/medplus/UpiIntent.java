package com.medplus;


import android.app.Activity;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.apache.cordova.PluginResult.Status;

import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;

import android.content.Context;
import android.util.Log;
import android.os.Bundle;
import android.content.Context;
import android.content.Intent;

import android.net.Uri;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.Iterator;




public class UpiIntent extends CordovaPlugin {
  private static final String TAG = "UpiIntent";
  private CallbackContext cbContext;
  private Context context;
  public void initialize(CordovaInterface cordova, CordovaWebView webView) {
    super.initialize(cordova, webView);
    Log.d(TAG, "Initializing UpiIntent");
    this.cordova.setActivityResultCallback((CordovaPlugin)this);
    this.context = cordova.getContext();

  }

  private Activity getCurrentActivity() {
    return cordova.getActivity();
}

  public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {
    if(action.equals("startUpiActivity")) {
      this.cbContext = callbackContext;
      Log.d(TAG, "starting transactions");
      Log.d(TAG, args.get(0).toString());
      cordova.setActivityResultCallback(this);
      cordova.getThreadPool().execute(new Runnable() {
            public void run() {
                try{
                    JSONObject upiIntentObject = (JSONObject)args.get(0);
                    String deepLinkUrl = upiIntentObject.getString("deepLinkUrl");
                    deepLinkUrl = deepLinkUrl.replace(" ","+");
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(deepLinkUrl));
                    Intent chooser = Intent.createChooser(intent, "Pay with...");
                    cordova.getActivity().startActivityForResult(chooser, 100);
              }catch(Exception e){
                  Log.e(TAG, e.getMessage());
                  callbackContext.sendPluginResult(createPluginResult(false,"Exception Occured", "Unable to process response"));
              }
               
            }
        });
                 
    } else if (action.equals("supportedApps")) {
      fetchSupportedApps(callbackContext);
    }
    return true;
  }

  private void fetchSupportedApps(final CallbackContext callbackContext) {
    JSONArray result = new JSONArray();
    List<ResolveInfo> appsList = getAllInstalledUPIApps(getCurrentActivity().getApplicationContext());
    for (ResolveInfo appRI : appsList) {
        try {
            JSONObject app = new JSONObject();
            app.put("appId", appRI.activityInfo.packageName);
            app.put("appName",
                    (String) getCurrentActivity().getApplicationContext().getPackageManager()
                            .getApplicationLabel(getCurrentActivity().getApplicationContext().getPackageManager()
                                    .getApplicationInfo(appRI.activityInfo.packageName, 0)));
            result.put(app);
        } catch (final Exception e) {
        }
    }
    callbackContext.success(result);
}

private List<ResolveInfo> getAllInstalledUPIApps(Context context) {
    Intent intent = new Intent();
    intent.setAction(Intent.ACTION_VIEW);
    intent.setData(Uri.parse(
            "upi://pay?pn=MERCHANT&pa=M2306160483220675579140@ybl&tid=YBL60c7891e33cb42daaf86b0aeb992a8b9&tr=P1806151323093900554957&am=10.00&cu=INR&url=https://phonepe.com&mc=5311&tn=Payment%20for%20P1806151323093900554957"));
    List<ResolveInfo> list = context.getPackageManager().queryIntentActivities(intent,
            PackageManager.MATCH_DEFAULT_ONLY);
    Log.i(TAG, "UPI supported apps count " + list.size());
    return list;
}

  PluginResult createPluginResult(Boolean isSuccess, String message, String response){
    JSONObject data = new JSONObject();
    try{
      data.put("message", message);
      if(isSuccess)data.put("response", response);
    }catch(Exception e){
      Log.d(TAG, e.getMessage());
    }
    
    PluginResult result;
    if(isSuccess){
      result = new PluginResult(PluginResult.Status.OK, data);
    }else{
      result = new PluginResult(PluginResult.Status.ERROR, message);
    }
    result.setKeepCallback(true);
    return result;
  }

 public static String intentToString(Intent intent) {
    if (intent == null)
        return "";

    StringBuilder stringBuilder = new StringBuilder("action: ")
            .append(intent.getAction())
            .append(" data: ")
            .append(intent.getDataString())
            .append(" extras: ")
            ;
    for (String key : intent.getExtras().keySet())
        stringBuilder.append(key).append("=").append(intent.getExtras().get(key)).append(" ");

    return stringBuilder.toString();

}


 

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    Log.d(TAG, "onActivityResult, req. code: "+requestCode);
    Log.d(TAG,"Response Data: "+intentToString(data));
    super.onActivityResult(requestCode, resultCode, data);
    if(requestCode == 100 && data != null){
      this.cbContext.sendPluginResult(createPluginResult(true, data.getStringExtra("Status"), data.getStringExtra("response")));
    }
  }
  
}
