# NaturEntdecker – Imaginary National Park

Android app for the "Naturentdecker" National Park providing information about guided animal safaris bookable via the park hotline.

---

## Architecture

```
UI Layer (Compose Screens)
        ↓
ViewModel 
        ↓ invoke()
Use Cases 
        ↓ Flow / suspend
Repository ──► Room DB  ◄── single source of truth
        ↓ refresh()
  Retrofit API
```
## CI/CD

GitHub Actions runs automatically on every push and PR:

1. **Lint** — static analysis
2. **Unit Tests** — ViewModel + UseCase tests with MockK and Turbine
3. **Build** — assembles debug APK

See [`.github/workflows/ci.yml`](.github/workflows/ci.yml).


## Project Structure

```
app/src/main/java/com/example/naturentdecker/
├── data/
│   ├── local/
│   │   ├── dao/          TourDao.kt
│   │   ├── entity/       TourEntity.kt
│   │   ├── NaturEntdeckerDatabase.kt
│   │   └── TourMapper.kt
│   ├── model/            Tour.kt, Animal.kt, Contact.kt
│   ├── remote/
│   │   ├── api/          ToursApiService.kt
│   │   └── interceptor/  AcceptJsonInterceptor, LoggingInterceptor
│   ├── usecase/          GetAllToursUseCase, GetTop5ToursUseCase, GetTourDetailUseCase
│   └── ToursRepository.kt
├── di/
│   ├── DatabaseModule.kt
│   └── NetworkModule.kt
├── ui/
│   ├── components/       Shimmer.kt, EmptyState.kt
│   ├── theme/            Theme.kt
│   ├── tourdetail/       TourDetailScreen.kt, TourDetailViewModel.kt
│   └── tours/            ToursListScreen.kt, ToursViewModel.kt
├── util/
│   ├── Result.kt         Sealed Result<T> with AppException types
│   └── NetworkExt.kt     safeApiCall { } wrapper
├── MainActivity.kt       Adaptive portrait/landscape host
└── NaturEntdeckerApp.kt  Hilt entry point, Timber init
```

---
