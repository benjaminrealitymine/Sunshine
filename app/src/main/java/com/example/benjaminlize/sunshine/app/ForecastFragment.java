package com.example.benjaminlize.sunshine.app;

import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastFragment extends Fragment {

    private static final String LOG_TAG = "Forecast Fragment" ;
    ListView lv;

    public ForecastFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main,container);

        //Array dummy data
        ArrayList<String> weekForecast = new ArrayList<String>();{
            weekForecast.add("first element");
            weekForecast.add("second element");
            weekForecast.add("third element");
        }

        //Array Adapter
        ArrayAdapter<String> mForecastAdapter = new ArrayAdapter<String>(
                getActivity(),R.layout.list_item_forecast,R.id.list_item_forecast_textview, weekForecast);

        //ListView
        lv = (ListView)rootView.findViewById(R.id.listview_forecast);

        //Set Adapter
        lv.setAdapter(mForecastAdapter);

        //7days data for london URL: api.openweathermap.org/data/2.5/forecast/daily?q=London&mode=json&units=metric&cnt=7

        return rootView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }


    public class FetchWeatherTask extends AsyncTask<String,Void,String[]>{

        private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();

        @Override
        protected String[] doInBackground(String... params) {
            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String forecastJsonStr = null;

            try {
                // Construct the URL for the OpenWeatherMap query
                // Possible parameters are avaiable at OWM's forecast API page, at
                // http://openweathermap.org/API#forecast

                //URL url = new URL("http://api.openweathermap.org/data/2.5/forecast/daily?q=94043&mode=json&units=metric&cnt=7");

                Uri.Builder uribuilder = new Uri.Builder();
                uribuilder.scheme("http")
                        .authority("api.openweathermap.org")
                        .appendPath("/data/2.5/forecast/daily?q=")
                        .appendEncodedPath(params[0])
                        .appendPath("&mode=json&units=metric&cnt=7");
                uribuilder.build();
                URL url = new URL(uribuilder.toString());

                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                forecastJsonStr = buffer.toString();
                Log.i(LOG_TAG,"forecastJsonStr = " + forecastJsonStr);

            } catch (IOException e) {
                Log.e("ForecastFragment", "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e("ForecastFragment", "Error closing stream", e);
                    }
                }
            }
            String[] weatherData = new String[7];
            try {
                weatherData = getWeatherDataFromJson_Ben(forecastJsonStr, 7);
                Log.i(LOG_TAG,"weatherData first element = " + weatherData[0]);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return weatherData;
        }

        @Override
        protected void onPostExecute(String[] strings) {
            super.onPostExecute(strings);

            ArrayAdapter<String> mForecastAdapter_updated = new ArrayAdapter<String>(
                    getActivity(),R.layout.list_item_forecast,R.id.list_item_forecast_textview, strings);
            lv.setAdapter(mForecastAdapter_updated);
            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Toast.makeText(getActivity(),"This is item "+ position ,Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private String[] getWeatherDataFromJson_Ben(String forecastJsonStr, int numDays)
        throws JSONException {

        final String Log_TG = "getWeatherDataFromJson_Ben";

        String[] stringArray_result = new String[numDays];
        int intforforloop = 0;

        //Instanciate Calendar
        Calendar calendar = Calendar.getInstance();
        //Set date format for day (dd-MM-yyy)
        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
        //Set date format for day (Mon,Tue,...)
        SimpleDateFormat df_day = new SimpleDateFormat("EEE");

        //todays date
        String formattedDate_today = df.format(calendar.getTime());
        Log.i(Log_TG, "formattedDate_today = " + formattedDate_today);

        //todays day
        Date d1 = new Date();
        String dayOfTheWeek_today = df_day.format(d1);
        Log.i(Log_TG, "dayOfTheWeek_today = " + dayOfTheWeek_today);

        for(intforforloop = 0; intforforloop<numDays;intforforloop++){

            //calendar day + 1 and log tomorrows date
            calendar.add(Calendar.DAY_OF_YEAR, intforforloop);
            String formattedDate_loop = df.format(calendar.getTime());
            Log.i(Log_TG,"formattedDate_tomorrow = " + formattedDate_loop);


            //tomorrows day
            Date d2 = new Date();
            d2.setTime(d1.getTime()+intforforloop*24*60*60*1000);
            String dayOfTheWeek_loop = df_day.format(d2);
            Log.i(Log_TG,"dayOfTheWeek_tomorrow = " + dayOfTheWeek_loop);

            //JSON min and max temperature rounded, casted to int and concatenated with a / (maxtemp/mintemp)
            JSONObject ja = new JSONObject(forecastJsonStr);
            JSONArray jo = ja.getJSONArray("list");
            JSONObject day_ = (JSONObject) jo.get(intforforloop);
            JSONObject temp = day_.getJSONObject("temp");
            double maxtemp = temp.getDouble("max");
            double mintemp = temp.getDouble("min");
            maxtemp = Math.round(maxtemp);
            mintemp = Math.round(mintemp);
            int maxtemp_int = (int)maxtemp;
            int mintemp_int = (int)mintemp;
            String maxslashmin_string = new String(String.valueOf(maxtemp_int) + "/" + String.valueOf(mintemp_int));
            //String maxmintemp = getMaxandMinTemperatureForDay(forecastJsonStr,numDays);
            Log.i(Log_TG,"maxmintemp = " + maxslashmin_string);

            JSONArray weather = day_.getJSONArray("weather");
            JSONObject O = weather.getJSONObject(0);
            String main = O.getString("main");
            Log.i(Log_TG,"main = " + main);

            stringArray_result[intforforloop] = dayOfTheWeek_loop +" "+ formattedDate_loop +" - "+ main +" - "+ maxslashmin_string;
        }



        return stringArray_result;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.forecastfragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_refresh) {
            FetchWeatherTask fetchweathertask = new FetchWeatherTask();
            fetchweathertask.execute("94043");
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}





















