#include <WiFi.h>
#include <WebServer.h>
#include <ArduinoJson.h>
#include <Adafruit_NeoPixel.h>

// --- KONFIGURACJA SIECI WI-FI ---
const char* ssid = "Test1";
const char* password = "test1234";

// --- KONFIGURACJA LED WS2812B ---
#define LED_PIN    16   // Pin ESP32 podłączony do DI w taśmie LED
#define NUM_LEDS   16   // Wpisz dokładną liczbę diod w Twojej taśmie!

Adafruit_NeoPixel strip(NUM_LEDS, LED_PIN, NEO_GRB + NEO_KHZ800);
WebServer server(80);

void handleLedCommand() {
  server.sendHeader("Access-Control-Allow-Origin", "*");

  if (!server.hasArg("plain")) {
    server.send(400, "application/json", "{\"status\":\"error\",\"message\":\"Brak body JSON\"}");
    return;
  }

  String body = server.arg("plain");
  StaticJsonDocument<200> doc;
  DeserializationError error = deserializeJson(doc, body);

  if (error) {
    server.send(400, "application/json", "{\"status\":\"error\",\"message\":\"Niepoprawny JSON\"}");
    return;
  }

  int red = doc["red"] | 0;
  int green = doc["green"] | 0;
  int blue = doc["blue"] | 0;
  int brightness = doc["brightness"] | 100;

  // Przeliczenie jasności (0-100% na zakres 0-255 dla biblioteki)
  int scaledBrightness = map(brightness, 0, 100, 0, 255);
  strip.setBrightness(scaledBrightness);

  // Ustawienie wskazanego koloru na wszystkich diodach
  for (int i = 0; i < NUM_LEDS; i++) {
    strip.setPixelColor(i, strip.Color(red, green, blue));
  }
  strip.show(); // Wysłanie komendy do diod

  Serial.printf("Ustawiono diody: R=%d, G=%d, B=%d (Jasność: %d%%)\n", red, green, blue, brightness);
  server.send(200, "application/json", "{\"status\":\"ok\"}");
}

void setup() {
  Serial.begin(9600);

  // Inicjalizacja taśmy LED
  strip.begin();
  strip.show(); // Wyłączenie diod na start

  // Połączenie z Wi-Fi
  WiFi.begin(ssid, password);
  Serial.print("Łączenie z Wi-Fi");
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }

  Serial.println("\nPołączono z Wi-Fi!");
  Serial.print("Adres IP ESP32: ");
  Serial.println(WiFi.localIP());

  server.on("/api/led", HTTP_POST, handleLedCommand);
  server.begin();
  Serial.println("Serwer gotowy.");
}

void loop() {
  server.handleClient();
}