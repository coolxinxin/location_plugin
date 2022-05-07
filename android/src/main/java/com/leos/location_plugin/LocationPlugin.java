package com.leos.location_plugin;

import android.app.Activity;
import android.location.Address;
import android.location.Geocoder;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;

/**
 * LocationPlugin
 */
public class LocationPlugin implements FlutterPlugin, MethodCallHandler, ActivityAware {
    /// The MethodChannel that will the communication between Flutter and native Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity
    private MethodChannel channel;
    private Activity activity;

    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
        channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "location_plugin");
        channel.setMethodCallHandler(this);
    }

    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
        switch (call.method) {
            case "getPlatformVersion":
                result.success("Android " + android.os.Build.VERSION.RELEASE);
                break;
            case "findAddressesFromQuery":
                String address = (String) call.argument("address");
                result.success("");
                break;
            case "findAddressesFromCoordinates":
                try {
                    float latitude = ((Number) call.argument("latitude")).floatValue();
                    float longitude = ((Number) call.argument("longitude")).floatValue();
                    List<Address> list = new ArrayList<>();
                    list.add(getAddress(latitude, longitude));
                    result.success(createAddressMapList(list));
                } catch (Exception e) {
                    e.printStackTrace();
                    result.success("");
                }
                break;
            default:
                result.notImplemented();
                break;
        }
    }

    private Address getAddress(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(activity, Locale.ENGLISH);
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses.size() > 0) return addresses.get(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Map<String, Object> createCoordinatesMap(Address address) {
        if (address == null)
            return null;
        Map<String, Object> result = new HashMap<>();
        result.put("latitude", address.getLatitude());
        result.put("longitude", address.getLongitude());
        return result;
    }

    private Map<String, Object> createAddressMap(Address address) {
        if (address == null)
            return null;
        // Creating formatted address
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(address.getAddressLine(i));
        }
        Map<String, Object> result = new HashMap<>();
        result.put("coordinates", createCoordinatesMap(address));
        result.put("featureName", address.getFeatureName());
        result.put("countryName", address.getCountryName());
        result.put("countryCode", address.getCountryCode());
        result.put("locality", address.getLocality());
        result.put("subLocality", address.getSubLocality());
        result.put("thoroughfare", address.getThoroughfare());
        result.put("subThoroughfare", address.getSubThoroughfare());
        result.put("adminArea", address.getAdminArea());
        result.put("subAdminArea", address.getSubAdminArea());
        result.put("addressLine", sb.toString());
        result.put("postalCode", address.getPostalCode());

        return result;
    }

    private List<Map<String, Object>> createAddressMapList(List<Address> addresses) {
        if (addresses == null)
            return new ArrayList<>();
        List<Map<String, Object>> result = new ArrayList<>(addresses.size());
        for (Address address : addresses) {
            result.add(createAddressMap(address));
        }
        return result;
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        channel.setMethodCallHandler(null);
    }

    @Override
    public void onAttachedToActivity(@NonNull ActivityPluginBinding binding) {
        activity = binding.getActivity();
    }

    @Override
    public void onDetachedFromActivityForConfigChanges() {
        this.onDetachedFromActivity();
    }

    @Override
    public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding binding) {
        this.onAttachedToActivity(binding);
    }

    @Override
    public void onDetachedFromActivity() {
        activity = null;
    }
}
