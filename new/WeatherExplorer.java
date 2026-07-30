import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class WeatherExplorer {

    private static final String API_KEY = "181af488749705b55bc3cf444e337ee8";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("==============================================");
        System.out.println("        GEOGRAPHIC WEATHER EXPLORER           ");
        System.out.println("==============================================");
        
        double latitude = readValidCoordinate(scanner, "Latitude", -90.0, 90.0);
        double longitude = readValidCoordinate(scanner, "Longitude", -180.0, 180.0);

        System.out.println("\nConnecting to OpenWeatherMap...");

        try {
            String jsonResponse = fetchWeatherData(latitude, longitude);
            displayWeatherCard(jsonResponse, latitude, longitude);
        } catch (IOException e) {
            System.err.println("\nNetwork Error: Could not reach weather service.");
            System.err.println("Details: " + e.getMessage());
        } catch (JSONException e) {
            System.err.println("\nJSON Parsing Error: Could not parse response.");
            System.err.println("Details: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("\nUnexpected Error: " + e.getMessage());
        } finally {
            scanner.close();
        }
    }

    private static double readValidCoordinate(Scanner scanner, String name, double min, double max) {
        double val;
        while (true) {
            System.out.printf("Enter %s (%.0f to %.0f): ", name, min, max);
            if (scanner.hasNextDouble()) {
                val = scanner.nextDouble();
                if (val >= min && val <= max) {
                    return val;
                }
            } else {
                scanner.next();
            }
            System.out.printf("  ❌ Invalid input! %s must be a number between %.0f and %.0f.\n", name, min, max);
        }
    }

    private static String fetchWeatherData(double lat, double lon) throws IOException {
        String apiUrl = "https://api.openweathermap.org/data/2.5/weather?lat=" 
                        + lat + "&lon=" + lon + "&appid=" + API_KEY + "&units=metric";

        URL url = new URL(apiUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        try (InputStream inputStream = conn.getInputStream();
             Scanner scanner = new Scanner(inputStream)) {

            scanner.useDelimiter("\\A");
            return scanner.hasNext() ? scanner.next() : "";
        } finally {
            conn.disconnect();
        }
    }

    private static void displayWeatherCard(String jsonString, double lat, double lon) throws JSONException {
        JSONObject json = new JSONObject(jsonString);

        String city = json.optString("name", "");
        String country = json.has("sys") ? json.getJSONObject("sys").optString("country", "") : "";
        
        String locationDisplay;
        if (city.isEmpty()) {
            locationDisplay = "Open Ocean / Unnamed Region";
        } else if (!country.isEmpty()) {
            locationDisplay = city + ", " + country;
        } else {
            locationDisplay = city;
        }

        JSONObject main = json.getJSONObject("main");
        JSONObject weather = json.getJSONArray("weather").getJSONObject(0);
        JSONObject wind = json.optJSONObject("wind");

        String description = weather.optString("description", "N/A");
        double temp = main.optDouble("temp", 0.0);
        double feelsLike = main.optDouble("feels_like", 0.0);
        int humidity = main.optInt("humidity", 0);
        double windSpeed = (wind != null) ? wind.optDouble("speed", 0.0) : 0.0;

        if (!description.isEmpty()) {
            description = Character.toUpperCase(description.charAt(0)) + description.substring(1);
        }

        System.out.println("\n+-------------------------------------------------+");
        System.out.println(" |              WEATHER & LOCATION REPORT           |");
        System.out.println("+---------------------------------------------------+");
        System.out.printf("   Coordinates : %.4f, %.4f\n", lat, lon);
        System.out.printf("   Location    : %s\n", locationDisplay);
        System.out.println(" -------------------------------------------------");
        System.out.printf("   Condition   : %s\n", description);
        System.out.printf("   Temperature : %.1f°C (Feels like %.1f°C)\n", temp, feelsLike);
        System.out.printf("   Humidity    : %d%%\n", humidity);
        System.out.printf("   Wind Speed  : %.1f m/s\n", windSpeed);
        System.out.println("+-------------------------------------------------+\n");
    }
}