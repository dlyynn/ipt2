import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Jokeapi 
{
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("--- Joke Api ---");
        System.out.print("Enter a type (general, programming, knock-knock) or press Enter for random: ");
        String category = scanner.nextLine().trim().toLowerCase();

        String apiUrl = "https://official-joke-api.appspot.com/jokes/random";
        if (!category.isEmpty()) {
            apiUrl = "https://official-joke-api.appspot.com/jokes/" + category + "/random";
        }

        try {
            URL url = new URL(apiUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);

            int status = conn.getResponseCode();
            if (status != 200) {
                System.out.println("Error: Server returned status code " + status);
                return;
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) 
            {
                response.append(line);
            }
            reader.close();
            conn.disconnect();

            String json = response.toString();

            System.out.println("\nRaw JSON:");
            System.out.println(json);

            System.out.println("\nJoke Details:");
            System.out.println("ID: " + getJsonValue(json, "id"));
            System.out.println("Type: " + getJsonValue(json, "type"));
            System.out.println("Setup: " + getJsonValue(json, "setup"));
            System.out.println("Punchline: " + getJsonValue(json, "punchline"));

        } catch (Exception e) {
            System.out.println("Something went wrong: " + e.getMessage());
        } finally {
            scanner.close();
        }
    }

    private static String getJsonValue(String json, String key) {
        Pattern pattern = Pattern.compile("\"" + key + "\":\\s*\"?([^\",}]+)\"?");
        Matcher matcher = pattern.matcher(json);
        if (matcher.find()) {
            return matcher.group(1).replace("\\\"", "\"").replace("\\n", "\n");
        }
        return "N/A";
    }
}