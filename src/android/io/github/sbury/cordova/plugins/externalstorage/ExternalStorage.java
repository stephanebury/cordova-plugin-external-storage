package io.github.sbury.cordova.plugins.externalstorage;

import java.io.File;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class ExternalStorage extends CordovaPlugin {

  int vers = android.os.Build.VERSION.SDK_INT;
  
  @Override
  public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
    
	if (action.equalsIgnoreCase("getRemovableStorage")) {
      JSONObject result = new JSONObject();
      try{
        result = getRemovableStorage();
        callbackContext.success(result);
      return true;
      }catch(JSONException e){
        result.put("message", e.getMessage());
        callbackContext.error(result);
      }
    }
	
	if (action.equalsIgnoreCase("getRemovableStorages")) {
      JSONObject result = new JSONObject();
      try{
        result = getRemovableStorages();
        callbackContext.success(result);
      return true;
      }catch(JSONException e){
        result.put("message", e.getMessage());
        callbackContext.error(result);
      }
    }	

    return false;
  }
  
  /**
   * 
   * @return JSONObject
   * @throws JSONException
   * 
   * getRemovableStorageDir
   * 
   */
  public JSONObject getRemovableStorage() throws JSONException {
	Context context   = cordova.getActivity().getApplicationContext();
	JSONObject result = new JSONObject();
    try {
      List<File> storages = _getRemovableStorages(context);
      if (!storages.isEmpty()) {
		  result.put("storages", storages.get(0).getAbsolutePath());
		  return result;
      }
    }
	catch (Exception ignored) {
    }
    final String SECONDARY_STORAGE = System.getenv("SECONDARY_STORAGE");
    if (SECONDARY_STORAGE != null) {
      File file = new File(SECONDARY_STORAGE.split(":")[0]);
	  result.put("storages", file.getAbsolutePath());
	  return result;	  
    }
    return null;
  }

   /**
   * 
   * @return JSONObject
   * @throws JSONException
   * 
   * getRemovableStoragesDir
   * 
   */  
  public JSONObject getRemovableStorages() throws JSONException {
	Context context   = cordova.getActivity().getApplicationContext();
	JSONObject result = new JSONObject();

    try {
      List<File> storages = _getRemovableStorages(context);
      if (!storages.isEmpty()) {
		
		/*JSONArray _array = new JSONArray();
		for (File storageFile : storages) {
			_array.add(storageFile.getAbsolutePath());
		}
		
		result.put("storages", _array);*/
		
		result.put("storages", "test");
		return result;
      }
    }
	catch (Exception ignored) {
    }

    return null;
  }  
  
  /**
   * 
   * @return List<File>
   * @throws Exception
   * 
   * getRemovableStorages
   * 
   */  
  public List<File> _getRemovableStorages(Context context) throws Exception {	
	List<File> storages = new ArrayList<File>();

    Method getService = Class.forName("android.os.ServiceManager").getDeclaredMethod("getService", String.class);
    if (!getService.isAccessible()) {
		getService.setAccessible(true);
	}
    IBinder service = (IBinder) getService.invoke(null, "mount");

    Method asInterface = Class.forName("android.os.storage.IMountService$Stub").getDeclaredMethod("asInterface", IBinder.class);
    if (!asInterface.isAccessible()) {
		asInterface.setAccessible(true);
	}
    Object mountService = asInterface.invoke(null, service);

    Object[] storageVolumes;
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      String packageName = context.getPackageName();
      int uid = context.getPackageManager().getPackageInfo(packageName, 0).applicationInfo.uid;
	  
      Method getVolumeList = mountService.getClass().getDeclaredMethod("getVolumeList", int.class, String.class, int.class);
      if (!getVolumeList.isAccessible()) {
		getVolumeList.setAccessible(true);
	  }
      storageVolumes = (Object[]) getVolumeList.invoke(mountService, uid, packageName, 0);
    } 
	else {
      Method getVolumeList = mountService.getClass().getDeclaredMethod("getVolumeList");
      if (!getVolumeList.isAccessible()) {
		  getVolumeList.setAccessible(true);
	  }
      storageVolumes = (Object[]) getVolumeList.invoke(mountService, (Object[]) null);
    }

    for (Object storageVolume : storageVolumes) {
      Class<?> cls = storageVolume.getClass();
	  
      Method isRemovable = cls.getDeclaredMethod("isRemovable");
      if (!isRemovable.isAccessible()) {
		  isRemovable.setAccessible(true);
	  }
	  
	  boolean b = (Boolean) isRemovable.invoke(storageVolume);
      if (b) {
		  
		//State
        Method getState = cls.getDeclaredMethod("getState");
        if (!getState.isAccessible()) {		
			getState.setAccessible(true);
		}
        String state = (String) getState.invoke(storageVolume, (Object[]) null);
        
		if (state.equals("mounted")) {
		
		  //path
          Method getPath = cls.getDeclaredMethod("getPath");
          if (!getPath.isAccessible()) {
			  getPath.setAccessible(true);
		  }
		  
          String path = (String) getPath.invoke(storageVolume, (Object[]) null);		  
          storages.add(new File(path));
        }
      }
    }
    return storages;
  }   
}
