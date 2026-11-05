# 🌈 ESP32 LED Controller – WS2812B

Prosty kontroler taśmy LED **WS2812B** oparty na ESP32. Urządzenie udostępnia lokalny serwer HTTP, który pozwala sterować kolorem wszystkich diod oraz regulować jasność za pomocą żądań API.

## ✨ Funkcje

- Sterowanie kolorem LED w modelu RGB.
- Regulacja jasności w zakresie 0–100%.
- Obsługa taśmy WS2812B.
- Komunikacja przez Wi-Fi.
- REST API oparte na metodzie HTTP POST.
- Odpowiedzi serwera w formacie JSON.

## 🛠️ Wymagane komponenty

| Komponent | Opis |
|---|---|
| ESP32 | Mikrokontroler |
| WS2812B | Adresowalna taśma LED |
| Zasilacz | Odpowiednie zasilanie taśmy LED |
| Przewody | Połączenia elektryczne |

## 🔌 Połączenie

| WS2812B | ESP32 |
|---|---|
| DI | GPIO 16 |
| GND | GND |
| 5V | Zewnętrzne zasilanie 5V |

> **Uwaga:** Przy zasilaniu taśmy z zewnętrznego źródła należy połączyć masę zasilacza z masą ESP32. Przy większej liczbie diod należy również zadbać o odpowiednią wydajność prądową zasilacza.

## ⚙️ Konfiguracja

Przed uruchomieniem należy ustawić dane sieci Wi-Fi oraz liczbę diod:

```cpp
const char* ssid = "Test1";
const char* password = "test1234";

#define LED_PIN  16
#define NUM_LEDS 16
```

### Wymagane biblioteki

- `WiFi.h` – obsługa Wi-Fi.
- `WebServer.h` – lokalny serwer HTTP.
- `ArduinoJson` – przetwarzanie danych JSON.
- `Adafruit NeoPixel` – sterowanie diodami WS2812B.

## 🌐 API

**Endpoint:** `/api/led`

**Metoda:** `POST`

**Content-Type:** `application/json`

### Przykładowe żądanie

```json
{
  "red": 255,
  "green": 0,
  "blue": 100,
  "brightness": 75
}
```

### Parametry

| Parametr | Zakres | Opis |
|---|---:|---|
| `red` | 0–255 | Intensywność czerwonego |
| `green` | 0–255 | Intensywność zielonego |
| `blue` | 0–255 | Intensywność niebieskiego |
| `brightness` | 0–100 | Jasność taśmy w procentach |

Wszystkie parametry są opcjonalne. Domyślnie RGB przyjmuje wartość `0`, a jasność `100`.

### Odpowiedź serwera

Poprawne wykonanie:

```json
{
  "status": "ok"
}
```

Błąd żądania:

```json
{
  "status": "error",
  "message": "Niepoprawny JSON"
}
```

## 🚀 Uruchomienie

1. Podłącz taśmę LED do ESP32.
2. Skonfiguruj dane sieci Wi-Fi.
3. Wgraj program na ESP32.
4. Otwórz monitor portu szeregowego (9600 baud).
5. Odczytaj adres IP urządzenia.
6. Wysyłaj żądania POST na adres:

```text
http://ADRES_IP_ESP32/api/led
```

## 📌 Technologie

`ESP32` · `C++` · `Arduino Framework` · `Wi-Fi` · `HTTP Server` · `JSON` · `WS2812B`
