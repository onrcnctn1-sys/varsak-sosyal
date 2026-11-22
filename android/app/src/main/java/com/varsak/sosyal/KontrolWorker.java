package com.varsak.sosyal;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Arka planda yaklaşık 15 dakikada bir data/bildirim.json dosyasına bakar.
 * Dosyadaki "id" değişmişse telefona Varsak Sosyal bildirimi gösterir.
 */
public class KontrolWorker extends Worker {

    static final String URL_BILDIRIM = "https://onrcnctn1-sys.github.io/varsak-sosyal/data/bildirim.json";
    static final String PREFS = "varsak";
    static final String KEY_SON = "son_bildirim_id";

    public KontrolWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            JSONObject j = indir();
            if (j == null) return Result.success();
            String id = j.optString("id", "");
            if (id.isEmpty()) return Result.success();
            SharedPreferences sp = getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
            String son = sp.getString(KEY_SON, null);
            sp.edit().putString(KEY_SON, id).apply();
            // İlk çalışmada sadece kaydet; eski haberler için bildirim gönderme.
            if (son == null || son.equals(id)) return Result.success();
            goster(getApplicationContext(), j.optString("baslik", "Varsak Sosyal"), j.optString("metin", "Yeni içerik geldi"), j.optString("ekran", "ana"));
        } catch (Exception ignored) {
        }
        return Result.success();
    }

    private JSONObject indir() throws Exception {
        HttpURLConnection c = (HttpURLConnection) new URL(URL_BILDIRIM + "?t=" + System.currentTimeMillis()).openConnection();
        c.setConnectTimeout(15000);
        c.setReadTimeout(15000);
        c.setUseCaches(false);
        try {
            if (c.getResponseCode() != 200) return null;
            StringBuilder sb = new StringBuilder();
            BufferedReader r = new BufferedReader(new InputStreamReader(c.getInputStream(), StandardCharsets.UTF_8));
            String line;
            while ((line = r.readLine()) != null) sb.append(line);
            r.close();
            return new JSONObject(sb.toString());
        } finally {
            c.disconnect();
        }
    }

    static void goster(Context ctx, String baslik, String metin, String ekran) {
        if (Build.VERSION.SDK_INT >= 33
                && ctx.checkSelfPermission("android.permission.POST_NOTIFICATIONS") != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        MainActivity.kanalOlustur(ctx);
        Intent i = new Intent(ctx, MainActivity.class);
        i.putExtra("ekran", ekran);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pi = PendingIntent.getActivity(ctx, 1, i, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        Notification.Builder b = Build.VERSION.SDK_INT >= 26
                ? new Notification.Builder(ctx, MainActivity.KANAL)
                : new Notification.Builder(ctx);
        b.setSmallIcon(R.drawable.ic_stat_paw)
                .setContentTitle(baslik)
                .setContentText(metin)
                .setStyle(new Notification.BigTextStyle().bigText(metin))
                .setColor(0xFF00818C)
                .setAutoCancel(true)
                .setContentIntent(pi);
        NotificationManager nm = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm != null) nm.notify(1001, b.build());
    }
}
