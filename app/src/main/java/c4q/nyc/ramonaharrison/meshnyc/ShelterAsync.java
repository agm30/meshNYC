package c4q.nyc.ramonaharrison.meshnyc;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import javax.net.ssl.HttpsURLConnection;

/**
 * Created by July on 8/1/15.
 */
public class ShelterAsync extends AsyncTask<Void, Void, Void> {

    private static final String API_DATA = "https://searchbertha-hrd.appspot.com/_ah/api/search/v1/zipcodes/10101/programs?api_key=3007cb21281f817773bd7a1aff9adb75&serviceTag=emergency%20shelter";
    private Context context;
    private static final String DATABASE_CREATED = "database_created";

    public ShelterAsync(Context context) {
        this.context = context;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        SQLHelper helper = SQLHelper.getInstance(context);
        helper.deleteTableShelters();
        String result = null;
        try {

            //open connection
            result = openConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {

            //parse JSON
            parseJSON(result);
        } catch (JSONException e) {
            e.printStackTrace();
        }

//        //store boolean in sharedpreference that DB is created
//        databaseCreated(context);
        return null;
    }

    //method to parse JSON
    public void parseJSON(String rawString) throws JSONException {
        int count = 0;
        String city;
        JSONObject obj = new JSONObject(rawString);
        JSONArray main = obj.getJSONArray("programs");

        for (int i = 0; i < main.length(); i++) {
            JSONObject program = main.getJSONObject(i);
            JSONArray offices = program.getJSONArray("offices");

            for (int k = 0; k < offices.length(); k++) {
                JSONObject info = offices.getJSONObject(k);
                JSONObject location = info.getJSONObject("location");
                double latitude = location.getDouble("latitude");
                double longitude = location.getDouble("longitude");
                if (info.has("city")) {
                    city = info.getString("city");
                }
                else{
                    city = "N/A";
                }
                String address = info.getString("address1");
                String postal = info.getString("postal");

                //insert a row in db
                SQLHelper helper = SQLHelper.getInstance(context);
                helper.insertRow(city, address, latitude, longitude, postal);
                count++;

                Log.i("yuliya", count + " ");
            }
        }
    }

    public String openConnection() throws IOException {
        String line;
        URL urlString = new URL(API_DATA);
        HttpsURLConnection connection = (HttpsURLConnection) urlString.openConnection();
        StringBuilder stringBuilder = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

        while ((line = reader.readLine()) != null) {
            stringBuilder.append(line + "\n");
        }
        String resultString = stringBuilder.toString();
        return resultString;
    }

    public void databaseCreated(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(DATABASE_CREATED, true);
        editor.commit();
    }
}