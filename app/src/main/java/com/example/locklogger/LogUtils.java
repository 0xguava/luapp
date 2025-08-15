package com.example.locklogger;

import android.content.Context;
import android.net.Uri;

import androidx.core.content.FileProvider;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LogUtils {

    private static final String LOG_NAME = "locklog.txt";
    private static final ExecutorService io = Executors.newSingleThreadExecutor();
    private static final SimpleDateFormat TS = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);

    public static File getLogFile(Context ctx) {
        return new File(ctx.getFilesDir(), LOG_NAME);
    }

    public static void appendLogAsync(Context ctx, String tag) {
        io.execute(() -> {
            appendLogSync(ctx, tag);
        });
    }

    public static synchronized void appendLogSync(Context ctx, String tag) {
        try (FileWriter fw = new FileWriter(getLogFile(ctx), true)) {
            String line = "[" + TS.format(new Date()) + "] " + tag + "\n";
            fw.write(line);
            fw.flush();
        } catch (Exception ignored) {}
    }

    public static String readAllLogs(Context ctx) {
        File f = getLogFile(ctx);
        if (!f.exists()) return "(no logs yet)";
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String ln;
            while ((ln = br.readLine()) != null) sb.append(ln).append('\n');
        } catch (Exception e) {
            return "(error reading logs)";
        }
        return sb.toString();
    }

    public static void clearLogs(Context ctx) {
        File f = getLogFile(ctx);
        if (f.exists()) {
            // truncate
            try (FileWriter fw = new FileWriter(f, false)) {
                fw.write("");
                fw.flush();
            } catch (Exception ignored) {}
        }
    }

    public static Uri getLogFileUri(Context ctx) {
        File f = getLogFile(ctx);
        if (!f.exists()) return null;
        // You must define a FileProvider in manifest via app's default provider from Android Studio template or add one:
        // (see provider XML in section 3 below)
        return FileProvider.getUriForFile(ctx, ctx.getPackageName() + ".provider", f);
    }
}
