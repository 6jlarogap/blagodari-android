package com.ex.diagnosticlib;

import android.app.Application;
import android.content.Intent;
import android.net.Uri;

import androidx.core.content.FileProvider;
import androidx.appcompat.app.AppCompatActivity;

import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

public class Diagnostic {

    private static final String TAG = "DGN";
    private static final String logFileName;
    private static Application App;
    private static boolean isLogEnabled;
    private static boolean DEBUG;

    static {
        logFileName = "logfile.log";
    }

    public static void init (Application app, boolean isLog, boolean debug) {
        App = app;
        setIsLogEnabled(isLog);
        DEBUG = debug;
    }

    public static void setIsLogEnabled (boolean isLogEnabled) {
        Diagnostic.isLogEnabled = isLogEnabled;
    }

    public static void Assert (boolean condition, String message) {
        if (DEBUG && !condition) {
            throw new AssertionError(message);
        }
    }

    public static void i () {
        i("");
    }

    public static void i (String msg) {
        if (isLogEnabled) {
            String location = getLocation();
            String fullMsg = new Date().toString() + location + msg;
            toLog(fullMsg);
            appendLog(fullMsg);
        }
    }

    public static void i (String name, Object object) {
        if (isLogEnabled) {
            String location = getLocation();
            String fullMsg = new Date().toString() + location + name + "=" + Objects.toString(object, "null");
            toLog(fullMsg);
            appendLog(fullMsg);
        }
    }

    public static void e (Throwable e) {
        if (isLogEnabled) {
            String location = getLocation();
            StringWriter stackTraceWriter = new StringWriter();
            e.printStackTrace(new PrintWriter(stackTraceWriter));
            String fullMsg = new Date().toString() + location + "Exception: " + e.getMessage() + ", StackTrace: " + stackTraceWriter.toString();
            toLog(fullMsg);
            appendLog(fullMsg);
        }
    }

    private static synchronized void toLog (String msg) {
        Log.i(TAG, msg);
    }

    private static File getLogFile () {
        return new File(App.getFilesDir(), logFileName);
    }

    private static void createLog () {
        File file = getLogFile();
        if (file.exists()) {
            file.delete();
        }
        try {
            file.createNewFile();

            appendLog("Created at " + new Date().toString());

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private synchronized static void appendLog (String line) {
        File file = getLogFile();
        if (!file.exists()) createLog();

        try {
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file, true));
            bufferedWriter.write(line);
            bufferedWriter.newLine();
            bufferedWriter.flush();
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String getLocation () {
        final String className = Diagnostic.class.getName();
        final StackTraceElement[] traces = Thread.currentThread().getStackTrace();
        boolean found = false;

        for (int i = 0; i < traces.length; i++) {
            StackTraceElement trace = traces[i];

            try {
                if (found) {
                    if (!trace.getClassName().startsWith(className)) {
                        Class<?> clazz = Class.forName(trace.getClassName());
                        return "[" + getClassName(clazz) + ":" + trace.getMethodName() + ":" + trace.getLineNumber() + "]: ";
                    }
                } else if (trace.getClassName().startsWith(className)) {
                    found = true;
                    continue;
                }
            } catch (ClassNotFoundException e) {
            }
        }

        return "[]: ";
    }

    private static String getClassName (Class<?> clazz) {
        if (clazz != null) {
            if (!TextUtils.isEmpty(clazz.getSimpleName())) {
                return clazz.getSimpleName();
            }

            return getClassName(clazz.getEnclosingClass());
        }

        return "";
    }

    public static void sendLog (AppCompatActivity activity, String appId) {
        String action = Intent.ACTION_SEND_MULTIPLE;

        Intent emailIntent = new Intent(action);
        emailIntent.setType("plain/text");
        emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{"blagodarie.developer@gmail.com"});
        emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "log");
        //emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, content);

        ArrayList<Uri> uris = new ArrayList<>();
        File file = getLogFile();
        Uri uri = FileProvider.getUriForFile(activity, appId + ".fileprovider", file);
        uris.add(uri);

        emailIntent.putParcelableArrayListExtra(android.content.Intent.EXTRA_STREAM, uris);
        activity.startActivity(emailIntent);
    }

}