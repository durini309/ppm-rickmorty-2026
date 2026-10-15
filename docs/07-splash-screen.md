# Splash Screen

Al abrir la app hay que leer DataStore para saber si hay sesión. Esa lectura no es instantánea, así que algo se tiene que mostrar mientras tanto: ese algo es el **splash**.

Hay dos formas de hacerlo. En el laboratorio se hizo la primera porque es más fácil de entender; este proyecto usa la segunda, que es la recomendada por Android.

## Las dos formas

### Forma 1: una pantalla de splash propia (la del laboratorio)

El splash es una pantalla más del `NavHost` y es el `startDestination`:

```
Abro la app → SplashDestination (logo + loader)
            → DataStore responde
            → navego al Login o a Characters y saco el Splash del backstack
```

Funciona, y se ve claro el estado `null`: "mientras no sé, estoy en el Splash". Pero tiene un problema.

### El problema: dos splash seguidos

Desde **Android 12**, el sistema **siempre** muestra un splash al abrir una app en frío: el ícono de la app sobre un fondo de color. No se puede quitar. Entonces, con la forma 1, el usuario ve:

```
Splash del sistema (ícono) → nuestro Splash (logo + loader) → Login o Characters
```

Dos pantallas de carga seguidas, que además no se parecen. Google recomienda no hacer un splash propio y usar el del sistema.

### Forma 2: la SplashScreen API (la de este proyecto)

En lugar de agregar otra pantalla, le decimos al splash del sistema **"quédate en pantalla mientras no haya leído DataStore"**:

```
Abro la app → splash del sistema (se queda mientras userName == null)
            → DataStore responde
            → se quita el splash y ya se ve el Login o Characters
```

Un solo splash, y la app arranca directo en la pantalla correcta.

| | Forma 1: pantalla propia | Forma 2: SplashScreen API |
|---|---|---|
| ¿Cuántos splash ve el usuario? | Dos (Android 12+) | Uno |
| ¿Dónde vive? | `SplashDestination` en el `NavHost` | Tema + `MainActivity` |
| ¿Qué representa el `null`? | Estar en la pantalla Splash | Que el splash del sistema siga visible |
| ¿Cómo se sale del splash? | Navegando con `popUpTo` | El sistema lo quita solo |
| ¿Hay que sacarlo del backstack? | Sí | No, nunca estuvo en el backstack |

La lógica de la sesión (`AppNavigationViewModel` y sus tres estados) es **la misma** en las dos formas. Lo único que cambia es qué se ve mientras `userName` es `null`.

## Cómo se configura

### 1. La dependencia

En `gradle/libs.versions.toml`:

```toml
[versions]
coreSplashscreen = "1.2.0"

[libraries]
androidx-core-splashscreen = { group = "androidx.core", name = "core-splashscreen", version.ref = "coreSplashscreen" }
```

Y en `app/build.gradle.kts`:

```kotlin
implementation(libs.androidx.core.splashscreen)
```

**¿Por qué una librería si el splash ya viene en Android 12?** Porque nuestra app soporta desde Android 8 (`minSdk = 26`). La librería hace que el splash se vea y se comporte igual en versiones anteriores a Android 12, y nos da la misma API en todas.

### 2. El tema del splash

En `res/values/themes.xml`:

```xml
<!-- Tema del splash del sistema (SplashScreen API). Cuando se quita el splash, se usa postSplashScreenTheme -->
<style name="Theme.RickAndMorty.Starting" parent="Theme.SplashScreen">
    <item name="windowSplashScreenBackground">@color/splash_background</item>
    <item name="windowSplashScreenAnimatedIcon">@drawable/ic_launcher_foreground</item>
    <item name="postSplashScreenTheme">@style/Theme.RickAndMorty</item>
</style>
```

| Atributo | Qué hace |
|---|---|
| `parent="Theme.SplashScreen"` | El tema base que trae la librería |
| `windowSplashScreenBackground` | Color de fondo del splash |
| `windowSplashScreenAnimatedIcon` | El ícono del centro. Usamos el *foreground* del ícono de la app |
| `postSplashScreenTheme` | El tema que se pone **cuando se quita el splash**: el tema normal de la app |

El color de fondo se define dos veces para que respete el modo oscuro:

- `res/values/colors.xml` → `splash_background` blanco
- `res/values-night/colors.xml` → `splash_background` negro

### 3. El manifest

La Activity arranca con el tema del splash:

```xml
<activity
    android:name=".MainActivity"
    android:theme="@style/Theme.RickAndMorty.Starting"
    ... >
```

### 4. `MainActivity`: instalar el splash y decirle cuándo quitarse

```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Tiene que ir antes de super.onCreate()
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        // Aunque sea una variable local, al rotar se recibe el mismo ViewModel (lo guarda la Activity)
        val appNavigationViewModel: AppNavigationViewModel by viewModels()

        // El splash del sistema se queda en pantalla mientras no hayamos leído DataStore (userName == null)
        splashScreen.setKeepOnScreenCondition {
            appNavigationViewModel.state.value.userName == null
        }

        enableEdgeToEdge()
        setContent {
            RickAndMortyTheme {
                AppNavHost(viewModel = appNavigationViewModel)
            }
        }
    }
}
```

- **`installSplashScreen()`** conecta el splash con la Activity y, al terminar, cambia al `postSplashScreenTheme`. Va **antes** de `super.onCreate()` porque el tema se tiene que cambiar antes de que la Activity cree su ventana.
- **`setKeepOnScreenCondition { ... }`** recibe una función que el sistema revisa **antes de dibujar cada frame**. Mientras devuelva `true`, el splash se queda. En cuanto `userName` deja de ser `null`, devuelve `false` y el splash se quita.
- **`by viewModels()`** es la versión para Activities de `viewModel()`. Le pasamos ese mismo ViewModel a `AppNavHost`, así la condición del splash y la navegación miran el mismo estado.

### 5. `AppNavHost`: no dibujar nada mientras no sabemos

```kotlin
val state by viewModel.state.collectAsStateWithLifecycle()

// Mientras no hemos leído DataStore no dibujamos nada: el splash del sistema sigue encima (ver MainActivity)
val userName = state.userName ?: return

// La primera pantalla se decide una sola vez, con lo que había en DataStore al abrir la app.
// Así no hay que pasar por el Login para luego redirigir a la lista de personajes.
val startDestination: Any = remember {
    if (userName.isEmpty()) LoginDestination else CharactersGraph
}

val navController = rememberNavController()
// ... Scaffold + NavHost(navController, startDestination = startDestination) { ... }
```

- **`?: return`**: si `userName` es `null`, el composable termina ahí y no dibuja nada. No importa, porque el splash del sistema está encima.
- **`startDestination`**: cuando ya sabemos si hay sesión, el `NavHost` empieza **directamente** en Login o en Characters. Ya no existe `SplashDestination`.

## Preguntas que suelen salir

### ¿Por qué no empezar siempre en el Login y redirigir a Characters si hay sesión?

Porque se vería el Login por un instante. El splash se quita en cuanto `userName` deja de ser `null`, pero la navegación del `LaunchedEffect` ocurre **un frame después**. En ese frame se dibujaría el Login. Decidiendo el `startDestination` antes de crear el `NavHost`, no hay nada que redirigir.

### ¿Para qué el `remember` en `startDestination`?

Para que se calcule **una sola vez**. Sin `remember`, al iniciar sesión `userName` cambiaría a `"Rick"`, el `startDestination` cambiaría a `CharactersGraph` y el `NavHost` recibiría un graph distinto. Cambiar el `startDestination` de un `NavHost` que ya está en uso no es la forma de navegar: para eso está el `LaunchedEffect`, que navega con `popUpTo(0)`.

### Entonces, ¿ya no hace falta revisar `isInSplash`?

No, porque ya no hay pantalla Splash. Pero `isInLogin` sí sigue haciendo falta, por las razones de [Sesión con DataStore](06-sesion-datastore.md#por-qué-revisar-isinlogin): sin ella, rotar el teléfono volvería a navegar.

### ¿Qué pasa si roto el teléfono?

El splash del sistema solo aparece al abrir la app, no al rotar. Al rotar, el ViewModel ya tiene el `userName`, así que `AppNavHost` dibuja de inmediato y Navigation restaura el backstack que tenías.

### ¿Cómo lo pruebo en el emulador?

Leer DataStore tarda muy poco, así que el splash casi no se nota: es normal que solo se vea un instante.

El splash solo sale en un **arranque en frío**: si la app ya estaba abierta en segundo plano, no aparece. Para verlo otra vez, cierra la app desde el selector de apps recientes y vuelve a abrirla.
