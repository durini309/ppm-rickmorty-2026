# Sesión con DataStore

La app recuerda quién inició sesión aunque se cierre. Para eso guardamos el **nombre del usuario** en DataStore Preferences: si hay un nombre guardado, hay sesión; si no, no.

Las piezas:

| Archivo | Qué hace |
|---|---|
| `core/data/SessionPreferences.kt` | Todo lo de DataStore: la instancia, la llave y los métodos para leer, guardar y borrar |
| `login/ui/LoginViewModel.kt` | Guarda el nombre al presionar "Entrar" |
| `profile/ui/ProfileViewModel.kt` | Lee el nombre para mostrarlo y lo borra al cerrar sesión |
| `core/navigation/AppNavigationViewModel.kt` | Escucha el nombre y le dice a `AppNavHost` si hay sesión |
| `core/navigation/AppNavHost.kt` | Navega al Login o a Characters cuando la sesión cambia |

## 1. `SessionPreferences`: un solo lugar para DataStore

```kotlin
// Instancia de DataStore. Va fuera de la clase para que exista una sola en toda la app
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "session")

class SessionPreferences(private val context: Context) {

    // Llaves con las que se guardan los valores
    private val nameKey = stringPreferencesKey("name")

    // Nombre del usuario. Si no se ha guardado nada, devuelve "" (no hay sesión)
    val name: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[nameKey] ?: ""
    }

    // Lee el nombre una sola vez (sin quedarse escuchando cambios)
    suspend fun getName(): String {
        return name.first()
    }

    suspend fun saveName(name: String) {
        context.dataStore.edit { preferences ->
            preferences[nameKey] = name
        }
    }

    suspend fun clearName() {
        context.dataStore.edit { preferences ->
            preferences.remove(nameKey)
        }
    }
}
```

- **¿Por qué `dataStore` está fuera de la clase?** `preferencesDataStore` crea el archivo donde se guardan los datos. Si hubiera dos instancias apuntando al mismo archivo, DataStore lanza un error. Al declararla una sola vez, a nivel de archivo, todos los `SessionPreferences(...)` usan la misma.
- **¿Por qué `?: ""`?** Si la llave no existe, DataStore devuelve `null`. Lo convertimos en `""` para que el nombre solo tenga dos valores posibles: vacío (sin sesión) o con texto (con sesión).
- **¿Por qué los métodos son `suspend`?** Leer y escribir en disco puede tardar. DataStore hace ese trabajo fuera del hilo principal, y `suspend` obliga a llamarlos desde una corrutina (`viewModelScope.launch { ... }`).

## 2. `AndroidViewModel`: ViewModels que necesitan un `Context`

DataStore necesita un `Context`. Un `ViewModel` normal no tiene uno, así que Login, Profile y `AppNavigationViewModel` heredan de **`AndroidViewModel`**, que recibe el `Application`:

```kotlin
class LoginViewModel(application: Application) : AndroidViewModel(application) {
    private val sessionPreferences = SessionPreferences(application)
    ...
}
```

**¿Por qué el `Application` y no la Activity?** El ViewModel vive más que la Activity: cuando rotas el teléfono, la Activity se destruye y se crea otra, pero el ViewModel sigue. Si guardara la Activity, la mantendría en memoria después de destruida (un *memory leak*). El `Application` vive lo mismo que la app, así que no hay problema.

No hay que hacer nada especial para crearlos: `viewModel()` en el `Route` sabe pasarles el `Application`.

## 3. `Flow` o `first()`: escuchar vs leer una vez

`SessionPreferences` ofrece las dos formas, y cada ViewModel usa la que necesita:

| Forma | Quién la usa | Por qué |
|---|---|---|
| `name` (un `Flow`) | `AppNavigationViewModel` | Tiene que **enterarse cada vez** que el nombre cambia, para navegar al Login o a Characters |
| `getName()` (con `first()`) | `ProfileViewModel` | Solo necesita el nombre **una vez** para mostrarlo; mientras estás en Profile no cambia |

`first()` toma el primer valor del `Flow` y deja de escucharlo:

```kotlin
// ProfileViewModel
init {
    viewModelScope.launch {
        val name = sessionPreferences.getName()
        _state.update {
            it.copy(name = name)
        }
    }
}
```

## 4. Login: no dejar pasar un nombre vacío

Como `""` significa "sin sesión", guardar un nombre vacío no tendría sentido: la app se quedaría en el Login. Por eso se valida en dos lugares:

- **En la UI:** el botón "Entrar" está deshabilitado mientras el nombre esté vacío (`enabled = state.name.isNotBlank()`).
- **En el ViewModel:** antes de guardar se quitan los espacios (`trim()`) y, si queda vacío, no se guarda nada. Así, aunque alguien llamara `LoginClick` por otro lado, la regla se cumple.

```kotlin
private fun loginClick() {
    val name = _state.value.name.trim()
    // Un nombre vacío significa "sin sesión", así que no lo dejamos pasar
    if (name.isEmpty()) return

    // Solo guardamos el nombre. AppNavigationViewModel se entera del cambio y AppNavHost navega
    viewModelScope.launch {
        sessionPreferences.saveName(name)
    }
}
```

Fíjate que **Login no navega**. Solo guarda el nombre. Profile hace lo mismo al cerrar sesión: solo llama `clearName()`. ¿Quién navega entonces? `AppNavHost`.

## 5. `AppNavigationViewModel`: los tres estados de la sesión

```kotlin
class AppNavigationViewModel(application: Application) : AndroidViewModel(application) {
    private val sessionPreferences = SessionPreferences(application)

    val state = sessionPreferences.name
        .map { name ->
            AppNavigationState(userName = name)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AppNavigationState()
        )
}
```

`stateIn` convierte el `Flow` de DataStore en un `StateFlow`, que siempre tiene un valor. Por eso `userName` es `String?` y tiene **tres** valores posibles:

| `userName` | Qué significa | Qué pasa |
|---|---|---|
| `null` | Todavía no hemos leído DataStore. Es el `initialValue` | Se muestra el splash (ver [Splash Screen](07-splash-screen.md)) |
| `""` | Ya leímos y no hay nombre guardado | Login |
| `"Rick"` | Ya leímos y hay sesión | Characters |

Ojo: **DataStore nunca devuelve `null`**, porque `SessionPreferences` lo convierte en `""`. El `null` solo existe al principio, mientras llega la primera respuesta.

`WhileSubscribed(5_000)` significa que DataStore solo se escucha mientras alguien observa `state` (en este caso, `AppNavHost`). Si la app pasa más de 5 segundos en segundo plano, deja de escuchar; al volver, sigue con el último valor. Los 5 segundos cubren una rotación sin cortar la lectura.

## 6. `AppNavHost`: el único lugar que navega por la sesión

```kotlin
// Cuando la sesión cambia (Login guarda el nombre o Profile lo borra), navegamos desde aquí.
// Revisamos si estamos en el Login para no navegar de más, por ejemplo al rotar la pantalla.
LaunchedEffect(userName) {
    val isInLogin = navController.currentDestination?.hasRoute(LoginDestination::class) == true

    // popUpTo(0) vacía todo el backstack: back en Login o en Characters cierra la app
    when {
        userName.isEmpty() -> {
            // No hay sesión: vamos al Login (si no estamos ya ahí)
            if (!isInLogin) {
                navController.navigateToLogin(navOptions { popUpTo(0) { inclusive = true } })
            }
        }

        else -> {
            // Hay sesión: si estamos en el Login, vamos a la lista de personajes
            if (isInLogin) {
                navController.navigateToCharactersGraph(navOptions { popUpTo(0) { inclusive = true } })
            }
        }
    }
}
```

El flujo completo al iniciar sesión:

```
Usuario presiona "Entrar"
  → LoginViewModel guarda "Rick" en DataStore
    → el Flow name emite "Rick"
      → AppNavigationViewModel.state cambia a userName = "Rick"
        → LaunchedEffect(userName) vuelve a correr
          → estamos en Login → navega a Characters con popUpTo(0)
```

Al cerrar sesión es igual, pero al revés: Profile borra el nombre, `userName` pasa a `""` y, como no estamos en el Login, navega al Login.

### ¿Por qué revisar `isInLogin`?

Un `LaunchedEffect` no solo corre cuando cambia su llave (`userName`); también corre **cada vez que entra a la composición**. Eso pasa, por ejemplo, al rotar el teléfono: la Activity se recrea, `AppNavHost` se vuelve a componer desde cero y el efecto corre con el `userName` que ya tenía el ViewModel.

Sin la revisión pasaría esto:

- **Rotas en Profile** (`userName = "Rick"`): navegaría a Characters con `popUpTo(0)`. Te sacaría de Profile y perderías el backstack.
- **Rotas en el Login mientras escribes** (`userName = ""`): navegaría otra vez al Login con `popUpTo(0)`. Eso destruye el Login actual y su `LoginViewModel`, y se pierde lo que habías escrito.

Con la revisión, en esos casos no hace nada: ya estás en una pantalla que corresponde a la sesión.

### ¿Por qué no navegar desde Login y Profile?

Se podría: el ViewModel pondría un flag (`isLoggedIn = true`) y el `Route` navegaría al verlo. Pero entonces la navegación de la sesión quedaría en **dos lugares**: `AppNavHost` decidiría al abrir la app, y cada pantalla decidiría con su botón. Centralizarla en `AppNavHost` deja una sola regla fácil de recordar: **cambia la sesión, la app navega sola.**
