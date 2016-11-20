package com.example.qbecker.spotifly1;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

public class JSONGetter{

    public JSONGetter(){}

    public JsonArray sendGet(String toSend) {
    String backup = "[]";
        URL obj = null;
        try {
            obj = new URL(toSend);
        } catch (MalformedURLException e) {
            JsonArray result = new JsonParser().parse(backup).getAsJsonArray();
            return result;
        }
        HttpURLConnection con = null;
        try {
            con = (HttpURLConnection) obj.openConnection();
        } catch (IOException e) {
            JsonArray result = new JsonParser().parse(backup).getAsJsonArray();
            return result;
        }
        // optional default is GET
        try {
            con.setRequestMethod("GET");
        } catch (ProtocolException e) {
            JsonArray result = new JsonParser().parse(backup).getAsJsonArray();
            return result;
        }

        try {
            int responseCode = con.getResponseCode();
        } catch (IOException e) {
            JsonArray result = new JsonParser().parse(backup).getAsJsonArray();
            return result;
        }

        BufferedReader in = null;
        try {
            in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        } catch (IOException e) {
            JsonArray result = new JsonParser().parse(backup).getAsJsonArray();
            return result;
        }
        String inputLine;
        StringBuffer response = new StringBuffer();

        try {
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
        } catch (IOException e) {
            JsonArray result = new JsonParser().parse(backup).getAsJsonArray();
            return result;
        }
        try {
            in.close();
        } catch (IOException e) {
            JsonArray result = new JsonParser().parse(backup).getAsJsonArray();
            return result;
        }
        JsonArray result = new JsonParser().parse(response.toString()).getAsJsonArray();
        return result;

    }

}
