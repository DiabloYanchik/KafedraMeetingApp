package com.example.kafedrameetingapp;
import android.content.Context;

import com.example.kafedrameetingapp.Meeting;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.util.*;
public class JsonUtils {
    private static final String FILE_NAME = "meetings.json";

    public static List<Meeting> loadMeetings(Context context) {
        try (FileInputStream fis = context.openFileInput(FILE_NAME);
             InputStreamReader isr = new InputStreamReader(fis)) {
            Type listType = new TypeToken<List<Meeting>>() {}.getType();
            return new Gson().fromJson(isr, listType);
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    public static void saveMeeting(Context context, Meeting meeting) {
        List<Meeting> meetings = loadMeetings(context);
        meetings.add(meeting);
        String json = new Gson().toJson(meetings);
        try (FileOutputStream fos = context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE)) {
            fos.write(json.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static int getNextProtocolNumber(Context context) {
        return loadMeetings(context).size() + 1;
    }
    public static void clearMeetings(Context context) {
        try (FileOutputStream fos = context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE)) {
            fos.write("[]".getBytes());  // пустой JSON-массив
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
