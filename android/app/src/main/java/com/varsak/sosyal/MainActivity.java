package com.varsak.sosyal;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

/**
 * Varsak Sosyal: GitHub Pages'teki web uygulamasını tam ekran açar.
 * Uygulama içi sayfalar burada kalır; Instagram / TikTok / YouTube linkleri
 * telefondaki kendi uygulamalarında (ya da tarayıcıda) açılır.
 * Arka planda yeni içerik için bildirim kontrolünü de başlatır.
 */
public class MainActivity extends Activity {

    private static final String HOME = "https://onrcnctn1-sys.github.io/varsak-sosyal/index.html";
    private static final String HOST = "onrcnctn1-sys.github.io";
    static final String KANAL = "varsak_yenilikler";
    private WebView web;

    static void kanalOlustur(Context ctx) {
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationManager nm = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
            if (nm != null && nm.getNotificationChannel(KANAL) == null) {
                NotificationChannel ch = new NotificationChannel(KANAL, "Yenilikler", NotificationManager.IMPORTANCE_DEFAULT);
                ch.setDescription("Yeni video fikirleri, uyarlamalar ve güncellemeler");
                nm.createNotificationChannel(ch);
            }
        }
    }

    private void kontrolBaslat() {
        Constraints c = new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build();
        PeriodicWorkRequest w = new PeriodicWorkRequest.Builder(KontrolWorker.class, 15, TimeUnit.MINUTES)
                .setConstraints(c).build();
        WorkManager.getInstance(getApplicationContext())
                .enqueueUniquePeriodicWork("varsak-kontrol", ExistingPeriodicWorkPolicy.KEEP, w);
    }

    private String hedef(Intent i) {
        String ekran = i != null ? i.getStringExtra("ekran") : null;
        return (ekran == null || ekran.isEmpty()) ? HOME : HOME + "#" + ekran;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        web = new WebView(this);
        setContentView(web);

        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);
        s.setCacheMode(WebSettings.LOAD_DEFAULT);
        s.setSupportMultipleWindows(false);
        s.setUserAgentString(s.getUserAgentString() + " VarsakSosyalApp/1.2");

        web.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri uri = request.getUrl();
                if (HOST.equals(uri.getHost())) return false;
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, uri));
                } catch (Exception ignored) { }
                return true;
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                if (request.isForMainFrame()) {
                    view.loadData(
                        "<html><body style='font-family:sans-serif;padding:32px;color:#12201e'>"
                        + "<h2>İnternet bağlantısı yok</h2>"
                        + "<p>Varsak Sosyal verileri internetten alır. Bağlantınızı kontrol edip uygulamayı yeniden açın.</p>"
                        + "</body></html>", "text/html; charset=utf-8", "UTF-8");
                }
            }
        });

        if (savedInstanceState != null) {
            web.restoreState(savedInstanceState);
        } else {
            web.loadUrl(hedef(getIntent()));
        }

        kanalOlustur(this);
        if (Build.VERSION.SDK_INT >= 33
                && checkSelfPermission("android.permission.POST_NOTIFICATIONS") != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{"android.permission.POST_NOTIFICATIONS"}, 42);
        }
        try { kontrolBaslat(); } catch (Exception ignored) { }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent != null && intent.getStringExtra("ekran") != null) web.loadUrl(hedef(intent));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        web.saveState(outState);
    }

    @Override
    public void onBackPressed() {
        if (web.canGoBack()) web.goBack();
        else super.onBackPressed();
    }
}
