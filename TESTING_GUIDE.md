# Test Suite for ClearDay App

## ✅ Status: Wszystkie testy przechodzą (BUILD SUCCESSFUL)

## Utworzone testy funkcjonalne/jednostkowe:

### 1. **AqiUtilsTest.kt** (4 testy)
Lokalizacja: `app/src/test/java/com/example/clearday/utils/AqiUtilsTest.kt`

**Testowane funkcje:**
- `calculateAQI()` - sprawdza poprawność obliczeń AQI dla różnych wartości PM2.5 i PM10
- `getAqiLabel()` - weryfikuje etykiety dla różnych zakresów AQI (Good, Moderate, Unhealthy, etc.)
- `getAqiColor()` - sprawdza czy zwracane są odpowiednie kolory dla każdego zakresu

**Pokrycie:**
- ✅ Niskie wartości zanieczyszczeń (Good)
- ✅ Średnie wartości (Moderate)
- ✅ Wysokie wartości (Unhealthy but ok)
- ✅ Wszystkie zakresy kolorów

---

### 2. **WeatherRepositoryTest.kt** (5 testów)
Lokalizacja: `app/src/test/java/com/example/clearday/repository/WeatherRepositoryTest.kt`

**Testowane funkcje:**
- `getCurrentWeather()` - mockuje API i weryfikuje sukces/błąd
- `getAirQuality()` - testuje integrację z WAQI API
- `getForecast()` - sprawdza pobieranie prognozy pogody

**Użyte technologie:**
- Mockito do mockowania serwisów API
- Coroutines Test dla asynchronicznych operacji
- Result<T> pattern testing

**Pokrycie:**
- ✅ Sukces pobierania danych pogodowych
- ✅ Obsługa błędów sieciowych
- ✅ Walidacja odpowiedzi WAQI (status "ok" vs "error")
- ✅ Poprawność danych w odpowiedziach
- ✅ Testowanie Forecast API

---

### 3. **UserTest.kt** (3 testy)
Lokalizacja: `app/src/test/java/com/example/clearday/models/UserTest.kt`

**Testowane funkcje:**
- Domyślne wartości modelu `User`
- Inicjalizacja z niestandardowymi wartościami
- Funkcja `copy()` data class

**Pokrycie:**
- ✅ Sprawdzanie wartości domyślnych
- ✅ Testowanie trackedAllergens (lista alergenów)
- ✅ Weryfikacja ustawień briefingu (godzina/minuta)
- ✅ Kopiowanie i modyfikacja obiektu

---

### 4. **HomeActivityTest.kt** (4 testy)
Lokalizacja: `app/src/test/java/com/example/clearday/HomeActivityTest.kt`

**Testowane funkcje:**
- Inicjalizacja Activity
- Formatowanie czasu dla morning briefing
- Stałe używane w klasie (swipe threshold)

**Pokrycie:**
- ✅ Activity klasa istnieje
- ✅ Formatowanie czasu 07:30
- ✅ Formatowanie czasu 09:05
- ✅ Walidacja stałych progowych

---

### 5. **FirestoreServiceTest.kt** (9 testów) ⭐ NOWE
Lokalizacja: `app/src/test/java/com/example/clearday/services/FirestoreServiceTest.kt`

**Testowane funkcje:**
- `saveUserToFirestore()` - zapis profilu użytkownika
- `getUserProfile()` - odczyt profilu
- `updateUserProfile()` - aktualizacja danych
- `updateDailyLog()` - logika konstrukcji danych
- `saveSymptoms()` - zapis symptomów

**Użyte technologie:**
- MockK dla mockowania Firebase Firestore
- Testowanie callback-based API
- Mockowanie Task<T> z Firebase

**Pokrycie:**
- ✅ Sukces zapisu użytkownika do Firestore
- ✅ Niepowodzenie zapisu (isSuccessful = false)
- ✅ Odczyt istniejącego użytkownika
- ✅ Odczyt nieistniejącego dokumentu (null)
- ✅ Obsługa błędów Firestore (Exception)
- ✅ Aktualizacja profilu użytkownika
- ✅ Konstrukcja danych daily log
- ✅ Konstrukcja mapy symptomów
- ✅ Walidacja struktury danych

---

## Uruchomienie testów

### Windows (cmd/PowerShell):
```powershell
# Wszystkie testy jednostkowe
.\gradlew.bat test

# Tylko moduł app
.\gradlew.bat :app:testDebugUnitTest

# Z raportem HTML
.\gradlew.bat test --continue
# Raport: app/build/reports/tests/testDebugUnitTest/index.html

# Otwórz raport w przeglądarce
Invoke-Item app\build\reports\tests\testDebugUnitTest\index.html
```

### Unix/Linux/Mac:
```bash
./gradlew test
./gradlew :app:testDebugUnitTest
```

### Android Studio:
1. Otwórz klasę testu
2. Kliknij zieloną strzałkę obok nazwy klasy
3. Wybierz "Run '[NazwaKlasy]'"

---

## Dodane zależności testowe

W `app/build.gradle.kts`:

```kotlin
// JUnit 4
testImplementation("junit:junit:4.13.2")

// Mockito
testImplementation("org.mockito:mockito-core:5.7.0")
testImplementation("org.mockito:mockito-inline:5.2.0")
testImplementation("org.mockito.kotlin:mockito-kotlin:5.1.0")

// MockK (alternatywa dla Mockito)
testImplementation("io.mockk:mockk:1.13.8")

// Robolectric
testImplementation("org.robolectric:robolectric:4.11.1")

// Coroutines Test
testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")

// Architecture Components Testing
testImplementation("androidx.arch.core:core-testing:2.2.0")

// Espresso (dla UI tests)
androidTestImplementation("androidx.test.espresso:espresso-contrib:3.5.1")
androidTestImplementation("androidx.test.espresso:espresso-intents:3.5.1")
```

---

## Statystyki pokrycia

| Komponent | Testy | Status |
|-----------|-------|--------|
| AqiUtils | 4 | ✅ |
| WeatherRepository | 6 | ✅ |
| User Model | 3 | ✅ |
| HomeActivity | 6 | ✅ |
| **RAZEM** | **19** | **✅** |

---

## Kolejne kroki (opcjonalne)

1. **FirestoreService testy** - mockowanie Firebase Firestore
2. **PollenRepository testy** - testowanie API pyłków
3. **Testy instrumentacyjne (Espresso)** - dla złożonych scenariuszy UI na emulatorze
4. **Testy snapshot** - dla Compose UI (jeśli używasz)
5. **Code coverage** - JaCoCo dla mierzenia pokrycia kodu

---

## Rozwiązywanie problemów

### "R.jar is locked"
Zamknij Android Studio i emulator, następnie uruchom:
```powershell
.\gradlew.bat clean build --rerun-tasks
```

### Robolectric nie działa
Upewnij się, że SDK version w `@Config(sdk = [28])` jest zainstalowany.

### Mockito nie może zamockować klasy
Dodaj `mockito-inline` do zależności (już dodane).
