package com.example.weatherapp;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.atomic.AtomicReference;

public class HelloApplication extends Application {
    private Group root;
    private AtomicReference<String> weatherOutput;
    private AtomicReference<ImageView> iconImageView;

    @Override
    public void start(Stage stage) throws IOException {
        root = new Group();
        TextField city = new TextField("Podaj nazwę miasta");
        root.getChildren().add(city);
        city.setLayoutX(150);
        city.setLayoutY(100);
        Button button = new Button();
        button.setLayoutX(170);
        button.setLayoutY(150);
        weatherOutput = new AtomicReference<>(null); //potrzebna jest zmienna mutowalna w takim formacie pliku jaki jest
        iconImageView = new AtomicReference<>(null);

        button.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                try {
                    String cityName = city.getText();
                    fetchWeatherData(cityName);
                } catch (IOException e) {
                    System.out.println(e);
                }
            }
        });

        root.getChildren().add(button);

        Scene scene = new Scene(root, 600, 600);
        stage.setScene(scene);
        stage.show();
    }

    private void fetchWeatherData(String cityName) throws IOException {
        String API_KEY = "8a4f5647721eafbdfeee1d3ce3864ab0";
        String GEO_API = "http://api.openweathermap.org/geo/1.0/direct?q=" + cityName + "&limit=5&appid=" + API_KEY;

        URL obj = new URL(GEO_API);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("GET");
        int responseCode = con.getResponseCode();
        System.out.println("Get Response Code for obj :: " + responseCode);

        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            StringBuilder response = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            String output = response.toString();
            System.out.println(output);
            JSONArray jArray = new JSONArray(output);
            JSONObject jObj = jArray.getJSONObject(0);
            double latValue = jObj.getDouble("lat");
            double lonValue = jObj.getDouble("lon");
            System.out.println("Latitude for object " + ": " + latValue);
            System.out.println("Longitude for object " + ": " + lonValue);

            String WEATHER_API = "https://api.openweathermap.org/data/2.5/weather?lat="+ latValue + "&lon="+ lonValue + "&appid=" + API_KEY + "&lang=pl" + "&units=metric";
            URL weather = new URL(WEATHER_API);
            HttpsURLConnection conHttps = (HttpsURLConnection) weather.openConnection();
            conHttps.setRequestMethod("GET");
            int responseWeather = conHttps.getResponseCode();
            System.out.println("Get Response Code for weather :: " + responseWeather);
            if (responseWeather == HttpsURLConnection.HTTP_OK) {
                BufferedReader inHttps = new BufferedReader(new InputStreamReader(conHttps.getInputStream()));
                String inputLineHttps;
                StringBuffer responseHttps = new StringBuffer();

                while ((inputLineHttps = inHttps.readLine()) != null) {
                    responseHttps.append(inputLineHttps);
                }
                inHttps.close();

                String weatherOutputString = responseHttps.toString();
                updateUI(weatherOutputString);
            }
        }
    }

    private void updateUI(String weatherData) {
        JSONObject jObject = new JSONObject(weatherData);
        JSONObject main = jObject.getJSONObject("main");
        JSONArray weatherLoc = jObject.getJSONArray("weather");
        JSONObject description = weatherLoc.getJSONObject(0);
        String weatherDescription = description.getString("description");
        String icon = description.getString("icon");
        String iconString = "https://openweathermap.org/img/wn/" + icon + "@2x.png";
        double visiblity = jObject.getDouble("visibility");
        JSONObject wind = jObject.getJSONObject("wind");
        double speed = wind.getDouble("speed");

        JSONObject clouds = jObject.getJSONObject("clouds");
        String name = jObject.getString("name");
        System.out.println(icon);
        Image iconImage = new Image(iconString);
        ImageView imageView = new ImageView(iconImage);

        String mainString = "Temperatura: " + main.getDouble("temp") + "\n"
                + "Temperatura Minimalna: " + main.getDouble("temp_min") + "\n"
                + "Wilgotność: " + main.getDouble("humidity") + "\n"
                + "Ciśnienie: " + main.getDouble("pressure") + "\n"
                + "Odczuwalna:" + main.getDouble("feels_like") + "\n"
                + "Temperatura Maksymalna: " + main.getDouble("temp_max") + "\n";
        weatherOutput.set(mainString + "\n"
                + "Widoczność: " + visiblity + "\n"
                + "Prędkość Wiatru: " + wind.getDouble("speed") + "\n"
                + "Chmury: " + clouds.getDouble("all") + "\n"
                + "Miasto: " + name + "\n");

        root.getChildren().clear();

        Text text = new Text(weatherOutput.get());
        text.setX(100);
        text.setY(200);

        imageView.setLayoutX(20);
        imageView.setLayoutY(20);

        root.getChildren().add(text);
        root.getChildren().add(imageView);
    }

    public static void main(String[] args) {
        launch();
    }
}