# Smart Home — sterownik ESP32 LED

Aplikacja na Androida do zarządzania urządzeniami ESP32 i sterowania oświetleniem LED. Umożliwia wybór koloru RGB, regulację jasności oraz zapis ustawień na telefonie.

## Wymagania

- Telefon lub tablet z Androidem 7.0 lub nowszym.
- ESP32 z oprogramowaniem obsługującym opisane poniżej API sterowania LED.
- Połączenie sieciowe między telefonem a ESP32, np. wspólna sieć Wi-Fi.
- Adres IPv4 urządzenia ESP32.

## Pierwsze uruchomienie

1. Zainstaluj aplikację i ją otwórz.
2. Wybierz „Utwórz lokalne konto”.
3. Podaj poprawny adres email i hasło o długości od 8 do 128 znaków, a następnie powtórz hasło.
4. Naciśnij „Zarejestruj konto”. Po utworzeniu konta pojawi się lista urządzeń.

Można utworzyć wiele kont na jednym urządzeniu. Każde konto ma własną listę urządzeń i zapisane ustawienia LED. Adres email musi być unikalny w tej instalacji aplikacji. Konta działają lokalnie, bez serwera, synchronizacji między telefonami i odzyskiwania hasła.

## Dodawanie i zarządzanie urządzeniami

### Dodawanie ESP32

1. Na liście urządzeń wybierz „Dodaj ESP32”.
2. Wpisz nazwę urządzenia i adres IPv4, np. `192.168.1.50`.
3. Opcjonalnie podaj pomieszczenie. Puste pole otrzyma wartość „Główne”.
4. Wybierz „Zapisz urządzenie”.

Nazwa i nazwa pomieszczenia mogą mieć najwyżej 80 znaków. Pole adresu przyjmuje wyłącznie IPv4, bez prefiksu `http://`, numeru portu ani ścieżki.

### Edycja

Na karcie urządzenia wybierz „Edytuj”. Zmień nazwę, adres IP lub pomieszczenie i zapisz formularz. Edycja danych urządzenia zachowuje jego zapisane ustawienia LED.

### Usuwanie

Wybierz „Usuwanie”, wskaż urządzenie i potwierdź operację. Usunięcie urządzenia usuwa również jego zapisane ustawienia koloru i jasności.

## Sterowanie oświetleniem

1. Na karcie urządzenia wybierz „Steruj”.
2. Ustaw suwaki „Czerwony”, „Zielony” i „Niebieski” w zakresie 0–255.
3. Ustaw jasność w zakresie 0–100%.
4. Wybierz „Zapisz i wyślij do ESP32”.

Podgląd pokazuje wybrany kolor. Przesunięcie suwaków nie wysyła komendy automatycznie. Przycisk najpierw zapisuje ustawienia na telefonie, a następnie wysyła je do ESP32. Jeśli połączenie się nie powiedzie, zapisane wartości pozostają dostępne po ponownym uruchomieniu aplikacji.

Komunikat o wysłaniu komendy oznacza poprawną odpowiedź API. Aplikacja nie odczytuje rzeczywistego stanu oświetlenia z urządzenia.

## Połączenie z ESP32

Aplikacja wysyła komendy przez HTTP w sieci lokalnej. Nie wymaga certyfikatu ani dodatkowego ustawiania protokołu. Telefon musi mieć dostęp do adresu IP urządzenia ESP32.

HTTP przesyła komendy bez szyfrowania. Z aplikacji należy korzystać w zaufanej sieci lokalnej.

## Konto i dane

- Wybierz „Wyloguj”, aby zakończyć sesję i wrócić do ekranu logowania. Aby zmienić konto, zaloguj się na inny email lub utwórz nowe konto.
- Po ponownym uruchomieniu procesu aplikacji wymagane jest ponowne logowanie.
- Urządzenia i zapisane wartości LED są przechowywane lokalnie, oddzielnie dla każdego konta. Aktywny email jest widoczny na liście urządzeń.
- Hasło nie jest zapisywane w jawnej postaci. Dane konta są szyfrowane z użyciem Android Keystore.
- Kopie zapasowe i przenoszenie danych aplikacji na inne urządzenie są wyłączone.
- Usunięcie danych aplikacji lub jej odinstalowanie usuwa wszystkie konta, listy urządzeń i ustawienia.

Konto zabezpiecza dostęp do interfejsu aplikacji. Dostęp sieciowy do ESP32 zależy od zabezpieczeń jego oprogramowania i sieci.

## Rozwiązywanie problemów

| Problem | Co sprawdzić |
| --- | --- |
| Nie można się zalogować | Użyj emaila i hasła podanych podczas tworzenia lokalnego konta. |
| Email jest już zajęty | Zaloguj się na istniejące konto lub zarejestruj inne z innym adresem email. |
| Adres IP jest odrzucany | Wpisz sam adres IPv4, np. `192.168.1.50`. |
| Błąd połączenia z ESP32 | Sprawdź zasilanie ESP32, adres IP i dostępność urządzenia w sieci. |
| Komenda została wysłana, ale LED nie reaguje | Sprawdź konfigurację pinów, podłączenie LED i obsługę komend w oprogramowaniu ESP32. |
| Nie można odczytać lub zapisać danych | Uruchom aplikację ponownie i sprawdź dostępne miejsce w pamięci urządzenia. |

## Zgodność oprogramowania ESP32

ESP32 powinno obsługiwać żądanie `POST /api/led` przez HTTP na porcie 80. Aplikacja wysyła dane z nagłówkiem `Content-Type: application/json`:

```json
{
  "red": 255,
  "green": 128,
  "blue": 0,
  "brightness": 75
}
```

Pola `red`, `green` i `blue` mają zakres 0–255, a `brightness` 0–100. Odpowiedź HTTP 2xx jest traktowana jako sukces. Aplikacja nie obsługuje przekierowań ani uwierzytelniania API tokenem. Limit czasu całego żądania wynosi 10 sekund.


