package com.dgupta3.helloworld;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import java.io.BufferedWriter;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;

import android.os.AsyncTask;
import android.os.Bundle;

import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;


public class MainActivity extends AppCompatActivity {

    EditText searchString;
    TextView result;
    TextView result2;
    Button search;

    public static final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final String MY_URL ="http://bluetooth-env-test.eba-brqgvwur.us-east-2.elasticbeanstalk.com/WebApp/search.php?name=";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

//        new Thread(new ClientThread()).start();

        searchString = findViewById(R.id.editText);
        result = findViewById(R.id.result);
        result2 = findViewById(R.id.result2);
        search = findViewById(R.id.search_button);

//        search.setOnClickListener(new View.OnClickListener(){
//            public void onClick(View view){
//                Log.d("tag","someone pushed button");
//            }
//        });

    }

    public void clickHandler(View view){
        String str = searchString.getText().toString();
        Log.d("tag","someone pushed button");
        Toast.makeText(this, "Searching", Toast.LENGTH_SHORT).show();
        SearchAsyncTask task = new SearchAsyncTask();
        task.execute();
    }

    private void updateUi(data faculty) {
        // Display the earthquake title in the UI
        result.setText("");
        result2.setText("");

        if (faculty != null){
            int ans = faculty.availability;
            if (ans == 1)
                result.setText("Dr. "+ faculty.fname + " "+faculty.lname+" is available and was last seen at :" + faculty.timeStamp);
            else
                result2.setText("Dr. "+ faculty.lname+" is not available");
        }else{
            result2.setText("Faculty Not Found");
        }


    }


    /**
     * {@link AsyncTask} to perform the network request on a background thread, and then
     * update the UI with the first earthquake in the response.
     */
    private class SearchAsyncTask extends AsyncTask<URL, Void, data> {

        @Override
        protected data doInBackground(URL... urls) {
            // Create URL object
            URL url = createUrl(MY_URL);
            Log.d("tag","URL Generated: "+ url);
            // Perform HTTP request to the URL and receive a JSON response back
            String jsonResponse = "";
            try {
                jsonResponse = makeHttpRequest(url);
                Log.d("HTTP RESPONSE","returned = " + jsonResponse);
            } catch (IOException e) {
                // TODO Handle the IOException
            }

            // Extract relevant fields from the JSON response and create an object
            data faculty = extractFeatureFromJson(jsonResponse);


            return faculty;
        }

        /**
         * Update the screen with the given faculty (which was the result of the
         * {@link SearchAsyncTask}).
         */
        @Override
        protected void onPostExecute(data faculty) {
            updateUi(faculty);
        }

        /**
         * Returns new URL object from the given string URL.
         */
        private URL createUrl(String stringUrl) {
            URL url = null;
            try {
                url = new URL(stringUrl + searchString.getText().toString());
            } catch (MalformedURLException exception) {
                Log.e(LOG_TAG, "Error with creating URL", exception);
                return null;
            }
            return url;
        }

        /**
         * Make an HTTP request to the given URL and return a String as the response.
         */
        private String makeHttpRequest(URL url) throws IOException {
            String jsonResponse = "";
            HttpURLConnection urlConnection = null;
            InputStream inputStream = null;
            try {
                urlConnection = (HttpURLConnection) url.openConnection();
                Log.d("makeHttpRequest: ", urlConnection.toString());
                urlConnection.setRequestMethod("GET");
                urlConnection.setReadTimeout(10000 /* milliseconds */);
                urlConnection.setConnectTimeout(15000 /* milliseconds */);
                urlConnection.connect();
                Log.d("makeHttpRequest: ", urlConnection.toString());
                inputStream = urlConnection.getInputStream();
                Log.d("makeHttpRequest: ", inputStream.toString());
                jsonResponse = readFromStream(inputStream);
            } catch (IOException e) {
                // TODO: Handle the exception
                Log.d("Connection Failed ",e.toString());
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (inputStream != null) {
                    // function must handle java.io.IOException here
                    inputStream.close();
                }
            }

            Log.d("HTTP", jsonResponse);
            return jsonResponse;
        }

        /**
         * Convert the {@link InputStream} into a String which contains the
         * whole JSON response from the server.
         */
        private String readFromStream(InputStream inputStream) throws IOException {
            StringBuilder output = new StringBuilder();
            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
                BufferedReader reader = new BufferedReader(inputStreamReader);
                String line = reader.readLine();
                while (line != null) {
                    output.append(line);
                    line = reader.readLine();
                }
            }
            return output.toString();
        }

        /**
         * Return an {@link data} object by parsing out information
         * about the first earthquake from the input earthquakeJSON string.
         */
        private data extractFeatureFromJson(String responseJSON) {
            Log.d("tag","Extracting Features");
            Log.d("tag",responseJSON);
            try {
                Log.d(LOG_TAG,responseJSON);
                JSONObject baseJsonResponse = new JSONObject(responseJSON);
                JSONArray featureArray = baseJsonResponse.getJSONArray("data");


                // If there are results in the features array
                if (featureArray.length() > 0) {
                    // Extract out the first feature (which is an earthquake)
                    JSONObject firstFeature = featureArray.getJSONObject(0);


                    // Extract out the title, time, and tsunami values
                    String firstName = firstFeature.getString("fname");
                    String lastName = firstFeature.getString("lname");
                    int avail = firstFeature.getInt("availability");

                    String time = firstFeature.getString("last_modified");


                    // Create a new {@link Event} object
                    return new data(firstName, lastName, avail, time);
                }
            } catch (JSONException e) {
                Log.e(LOG_TAG, "Problem parsing the JSON results", e);
            }
            return null;
        }
    }


}
