import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;



public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        int limit = 7;
        String apikey;
        double lat = 55.7558, lon = 37.6173;
        String url = "https://api.weather.yandex.ru/v2/forecast?lat=" + lat + "&lon=" + lon + "&limit=" + limit;

        //чтение апи ключа
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(".env")));
        apikey = reader.readLine();

        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("X-Yandex-Weather-Key", apikey)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println(response.body());

        JsonNode root = new ObjectMapper().readTree(response.body());

        // текущая температура
        int currentTemp = root.path("fact").path("temp").asInt();
        System.out.println("Текущая температура (fact.temp): " + currentTemp + "°C");

        //средняя по прогнозу
        JsonNode forecasts = root.path("forecasts");

        double sum = 0;
        int count = 0;

        for (JsonNode day : forecasts) {
            if (count >= limit) break; // страховка, если API вернёт больше

            JsonNode tempAvg = day.path("parts").path("day").path("temp_avg");
            if (tempAvg.isNumber()) {
                double v = tempAvg.asDouble();
                sum += v;
                count++;
                System.out.printf("%s → temp_avg = %.1f°C%n",
                        day.path("date").asText(), v);
            }
        }

        if (count > 0) {
            double avg = sum / count;
            System.out.printf("Средняя за %d суток: %.2f°C%n", count, avg);
        } else {
            System.out.println("Нет данных forecasts[*].parts.day.temp_avg");
        }
    }
}