package com.leoscafe.app;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private WebView web;
    private EditText address;
    private LinearLayout config;
    private String baseUrl = "";
    private boolean pendingLocation = false;
    private boolean tracking = false;
    private LocationManager locationManager;
    private final LocationListener listener = location -> runOnUiThread(() -> {
        if (web != null && (pendingLocation || tracking)) {
            String js = "window.leoNativeLocation && window.leoNativeLocation(" + location.getLatitude() + "," + location.getLongitude() + ")";
            web.evaluateJavascript(js, null);
            pendingLocation = false;
        }
    });

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);
        LinearLayout root = new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setBackgroundColor(Color.rgb(17,17,15));
        LinearLayout bar = new LinearLayout(this); bar.setPadding(16, 12, 16, 12); bar.setBackgroundColor(Color.rgb(27,27,24));
        TextView title = new TextView(this); title.setText("LEO'S CAFE"); title.setTextColor(Color.WHITE); title.setTextSize(18); title.setGravity(android.view.Gravity.CENTER_VERTICAL);
        bar.addView(title, new LinearLayout.LayoutParams(0, 48, 1));
        Button settings = new Button(this); settings.setText("Server"); settings.setOnClickListener(v -> showConfig()); bar.addView(settings);
        root.addView(bar);
        config = new LinearLayout(this); config.setOrientation(LinearLayout.VERTICAL); config.setPadding(26, 36, 26, 20);
        TextView guidance = new TextView(this); guidance.setText("Enter the cafe server address. For testing on the same Wi-Fi, use your computer's LAN IP, for example http://192.168.1.10:3000. Keep the Node server running."); guidance.setTextSize(17); guidance.setTextColor(Color.WHITE);
        config.addView(guidance);
        address = new EditText(this); address.setSingleLine(true); address.setTextColor(Color.WHITE); address.setHintTextColor(0xffaaaaaa); address.setHint("http://192.168.1.10:3000"); address.setInputType(android.text.InputType.TYPE_TEXT_VARIATION_URI); address.setText(getPreferences(0).getString("server", ""));
        config.addView(address);
        Button connect = new Button(this); connect.setText("Connect to Leo's Cafe"); connect.setOnClickListener(v -> connect(address.getText().toString())); config.addView(connect);
        root.addView(config);
        web = new WebView(this); web.getSettings().setJavaScriptEnabled(true); web.getSettings().setDomStorageEnabled(true); web.getSettings().setGeolocationEnabled(false); web.getSettings().setMixedContentMode(android.webkit.WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        web.addJavascriptInterface(new LocationBridge(), "LeoAndroid");
        web.setWebViewClient(new WebViewClient() {
            @Override public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest req) {
                Uri uri = req.getUrl(); if (uri.toString().startsWith(baseUrl + "/") || uri.toString().equals(baseUrl)) return false;
                try { startActivity(new Intent(Intent.ACTION_VIEW, uri)); } catch (Exception ignored) { Toast.makeText(MainActivity.this,"Cannot open link",Toast.LENGTH_SHORT).show(); }
                return true;
            }
            @Override public void onReceivedError(WebView view, WebResourceRequest req, android.webkit.WebResourceError err) {
                if (req.isForMainFrame()) Toast.makeText(MainActivity.this,"Server unreachable. Check the address and Wi-Fi.",Toast.LENGTH_LONG).show();
            }
        });
        web.setWebChromeClient(new WebChromeClient()); root.addView(web, new LinearLayout.LayoutParams(-1, 0, 1)); setContentView(root);
        String saved = address.getText().toString(); if (!saved.isEmpty()) connect(saved); else showConfig();
    }
    private void showConfig() { stopLocation(); web.setVisibility(View.GONE); config.setVisibility(View.VISIBLE); }
    private void connect(String raw) {
        String value = raw.trim().replaceAll("/+$", ""); Uri uri = Uri.parse(value);
        if (!("http".equals(uri.getScheme()) || "https".equals(uri.getScheme())) || uri.getHost()==null || uri.getHost().isEmpty() || uri.getUserInfo()!=null) {
            Toast.makeText(this,"Enter a valid http or https server URL",Toast.LENGTH_LONG).show(); return;
        }
        baseUrl=value; getPreferences(0).edit().putString("server",value).apply(); config.setVisibility(View.GONE); web.setVisibility(View.VISIBLE); web.loadUrl(value);
    }
    private void requestLocation(boolean continuous) {
        tracking=continuous; pendingLocation=!continuous;
        if (Build.VERSION.SDK_INT>=23 && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},17); return;
        }
        try {
            if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,5000,5,listener);
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,5000,5,listener);
            Location last=locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (last!=null) listener.onLocationChanged(last);
            if (!continuous) web.postDelayed(() -> { if(!tracking) stopLocation(); },15000);
        } catch (SecurityException e) { Toast.makeText(this,"Location permission needed",Toast.LENGTH_SHORT).show(); }
    }
    @Override public void onRequestPermissionsResult(int requestCode,String[] permissions,int[] results) {
        super.onRequestPermissionsResult(requestCode,permissions,results);
        if (requestCode==17 && results.length>0 && (results[0]==PackageManager.PERMISSION_GRANTED || results.length>1 && results[1]==PackageManager.PERMISSION_GRANTED)) requestLocation(tracking);
        else if (requestCode==17) Toast.makeText(this,"Location permission denied",Toast.LENGTH_SHORT).show();
    }
    private void stopLocation() { tracking=false; pendingLocation=false; if (locationManager!=null) locationManager.removeUpdates(listener); }
    @Override protected void onPause() { super.onPause(); stopLocation(); }
    @Override protected void onResume() { super.onResume(); if(web!=null) web.evaluateJavascript("window.leoNativeTrackingStopped && window.leoNativeTrackingStopped()",null); }
    @Override protected void onDestroy() { stopLocation(); if(web!=null) web.destroy(); super.onDestroy(); }
    @Override public void onBackPressed() { if (web.getVisibility()==View.VISIBLE && web.canGoBack()) web.goBack(); else showConfig(); }
    private class LocationBridge {
        @JavascriptInterface public void requestOnce() { runOnUiThread(() -> requestLocation(false)); }
        @JavascriptInterface public void startTracking(String orderId) { runOnUiThread(() -> requestLocation(true)); }
        @JavascriptInterface public void stopTracking() { runOnUiThread(() -> stopLocation()); }
    }
}
