import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

public class Location
   {
      public static void main(String[] args) {
      String apiKey = "181af488749705b55bc3cf444e337ee8";
      double latitude;
      double longitude;

      Scanner dataIn = new Scanner(System.in);
      System.out.print("Enter Latitude (-90 to 90): ");
      latitude = dataIn.nextDouble();
      System.out.print("Enter Longitude (-180 to 180): ");
      longitude = dataIn.nextDouble();
   
   try 
   {
      String location = getLocation(apiKey, latitude, longitude);
      displayLocation(location, latitude, longitude);
   }
   catch (IOException | JSONException e) 
   {
      e.printStackTrace();
   }
   finally 
   {
      dataIn.close();
   }
}

   private static String getLocation(String apiKey, double latitude, double longitude) throws IOException {
      String apiUrl = "https://api.openweathermap.org/data/2.5/weather?lat=" + latitude + "&lon=" + longitude + "&appid=" + apiKey;
      URL url = new URL(apiUrl);
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();

      try (InputStream inputStream = connection.getInputStream();
      Scanner scanner = new Scanner(inputStream)) 
{

      scanner.useDelimiter("\\A");
      String response = scanner.hasNext() ? scanner.next() : "";

   try 
   {
      JSONObject json = new JSONObject(response);
      String city = json.optString("name");
      return city.isEmpty() ? "Unknown Location" : city;
   }
   catch (JSONException e) 
   {
      return "Unknown Location";
   }
} 
   finally 
   {
      connection.disconnect();
   }
}

   private static void displayLocation(String location, double latitude, double longitude) throws JSONException
   {
      System.out.println("Location is " + location + " (Latitude " + latitude + " and Longitude " + longitude + ")");
   }
}