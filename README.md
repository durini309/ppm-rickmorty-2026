# Rick and Morty

Laboratorio de Android con Jetpack Compose. La app tiene tres pantallas (Login, listado de personajes y detalle de un personaje), un solo `Activity` y navegación entre composables con Navigation Compose y rutas type-safe (clases `@Serializable`).

## Estructura del proyecto: Screaming Architecture

```
com.gt.uvg.rickandmorty
├── MainActivity.kt
├── core
│   ├── navigation
│   │   └── AppNavHost.kt
│   └── ui
│       └── theme
├── login
│   └── ui
│       └── LoginScreen.kt
└── character
    ├── data
    │   ├── Character.kt
    │   └── CharacterDb.kt
    └── ui
        ├── CharacterImage.kt
        ├── list
        │   └── CharactersScreen.kt
        └── detail
            └── CharacterDetailScreen.kt
```

### Qué es

"Screaming Architecture" es un término de Robert C. Martin (Uncle Bob). La idea es que, al abrir la carpeta de un proyecto, la estructura te "grite" **de qué trata la aplicación** y no **con qué tecnología está hecha**.

Compara estas dos formas de organizar el mismo código:

```
Organizado por capa técnica          Organizado por funcionalidad
-------------------------------      -------------------------------
data/                                login/
    Character.kt                         ui/
    CharacterDb.kt                   character/
ui/                                      data/
    LoginScreen.kt                       ui/
    CharactersScreen.kt              core/
    CharacterDetailScreen.kt             navigation/
viewmodel/                               ui/theme/
    ...
```

A la izquierda solo sabes que la app "tiene data, ui y viewmodels", igual que cualquier otra app. A la derecha sabes de inmediato que la app trata de **login** y de **personajes**.

### Reglas que seguimos

1. **Cada funcionalidad grande tiene su propia carpeta** en la raíz del paquete (`login`, `character`).
2. **Adentro de cada funcionalidad** se separa por capa: `data`, `domain` y `ui`. Una capa solo se crea cuando hace falta; por ejemplo, `login` por ahora solo tiene `ui`, y ninguna funcionalidad tiene `domain` todavía porque no hay lógica de negocio.
3. **Lo que es compartido entre funcionalidades va en `core`** (el tema de la app y el `NavHost` principal).
4. Si algo se comparte **solo dentro de una funcionalidad**, se queda en esa funcionalidad. Por ejemplo, `CharacterImage.kt` lo usan la lista y el detalle, pero ambos son parte de `character`, entonces vive en `character/ui` y no en `core`.

### Por qué se usa

- **Se encuentra todo más rápido.** Si hay un error en el detalle de un personaje, sabes que está en `character/`. No tienes que buscar en tres carpetas técnicas distintas.
- **Los cambios quedan localizados.** Agregar o modificar una funcionalidad toca casi siempre una sola carpeta.
- **Escala mejor.** Con 20 pantallas, una carpeta `ui/` con 20 archivos se vuelve inmanejable; con carpetas por funcionalidad cada una sigue siendo pequeña.
- **Facilita dividir en módulos después.** Cada carpeta de funcionalidad ya está casi aislada, así que convertirla en un módulo de Gradle (`:feature:login`, `:feature:character`) es mucho más sencillo.
- **Facilita trabajar en equipo.** Dos personas trabajando en funcionalidades distintas casi no tocan los mismos archivos.

## Anatomía de un archivo de pantalla

Cada pantalla vive en **un solo archivo** y siempre tiene las mismas piezas. El sufijo de cada nombre dice qué es cada cosa:

| Sufijo | Ejemplo | Qué es | Visibilidad |
|---|---|---|---|
| `...Destination` | `LoginDestination` | A dónde navego (la clase `@Serializable`) | pública |
| `navigateTo...` | `navigateToLogin()` | Extension de `NavController` para ir a la pantalla | pública |
| `...Screen()` (minúscula) | `loginScreen()` | Extension de `NavGraphBuilder` que registra la pantalla en el `NavHost` | pública |
| `...Route` | `LoginRoute` | Punto de entrada de la pantalla. Aquí se conectará el ViewModel | pública |
| `...Screen` | `LoginScreen` | La UI pura: recibe datos y callbacks, no sabe de navegación | `private` |
| `Preview...Screen` | `PreviewLoginScreen` | Preview en modo claro y oscuro | `private` |

Regla para recordar: **el sufijo te dice qué es; la funcionalidad te dice de quién es.**

### Por qué separar `Route` y `Screen`

- `LoginRoute` es la parte "con conexiones": más adelante obtendrá el ViewModel, recolectará su estado y se lo pasará a la UI.
- `LoginScreen` es la parte "tonta": solo dibuja lo que recibe. Por eso se puede mostrar en un `@Preview` sin necesitar un ViewModel, una base de datos ni internet.

## Navegación con extension functions

### El problema

La forma "normal" de escribir un `NavHost` es poner toda la configuración de cada pantalla adentro:

```kotlin
NavHost(navController, startDestination = LoginDestination) {
    composable<LoginDestination> {
        LoginRoute(
            onStartClick = {
                navController.navigate(CharactersDestination) {
                    popUpTo(LoginDestination) { inclusive = true }
                }
            }
        )
    }
    composable<CharactersDestination> {
        CharactersRoute(
            onCharacterClick = { id ->
                navController.navigate(CharacterDetailDestination(id))
            }
        )
    }
    composable<CharacterDetailDestination> { backStackEntry ->
        val destination = backStackEntry.toRoute<CharacterDetailDestination>()
        CharacterDetailRoute(
            characterId = destination.characterId,
            onBackClick = { navController.navigateUp() }
        )
    }
}
```

Funciona, pero con cada pantalla nueva el `NavHost` crece, y los detalles internos de cada pantalla (cómo se leen sus argumentos, qué composable se llama) quedan mezclados en un solo lugar.

### La solución: dos extension functions por pantalla

Cada archivo de pantalla define:

1. Una extension de **`NavGraphBuilder`** que registra la pantalla (reemplaza el bloque `composable<...> { ... }`).
2. Una extension de **`NavController`** que sabe cómo navegar hacia ella (reemplaza el `navController.navigate(...)`).

### Ejemplo completo: Login

En `login/ui/LoginScreen.kt`:

```kotlin
// 1. El destino: a dónde navego. Es un data object porque Login no recibe parámetros.
@Serializable
data object LoginDestination

// 2. Cómo se navega hacia Login. Quien la llame no necesita conocer LoginDestination.
fun NavController.navigateToLogin(navOptions: NavOptions? = null) {
    navigate(LoginDestination, navOptions)
}

// 3. Cómo se registra Login en el NavHost. Solo expone lo que la pantalla
//    necesita del exterior: qué hacer cuando se presiona "Entrar".
fun NavGraphBuilder.loginScreen(onStartClick: () -> Unit) {
    composable<LoginDestination> {
        LoginRoute(onStartClick = onStartClick)
    }
}

// 4. Punto de entrada público de la pantalla.
@Composable
fun LoginRoute(onStartClick: () -> Unit) {
    LoginScreen(onStartClick = onStartClick)
}

// 5. La UI como tal. Es privada: nadie fuera de este archivo la usa directamente.
@Composable
private fun LoginScreen(onStartClick: () -> Unit, modifier: Modifier = Modifier) {
    // Logo, botón "Entrar" y nombre del estudiante
}
```

Y en `core/navigation/AppNavHost.kt` cada pantalla ocupa básicamente una línea:

```kotlin
NavHost(navController, startDestination = LoginDestination) {
    loginScreen(
        onStartClick = {
            navController.navigateToCharacters(
                navOptions { popUpTo(LoginDestination) { inclusive = true } }
            )
        }
    )
    charactersScreen(onCharacterClick = navController::navigateToCharacterDetail)
    characterDetailScreen(onBackClick = navController::navigateUp)
}
```

### Pantallas con parámetros: el detalle

Cuando una pantalla necesita datos, el destino pasa de `data object` a `data class`. Solo mandamos el **ID** del personaje, nada más; la pantalla de detalle se encarga de buscar el resto.

```kotlin
@Serializable
data class CharacterDetailDestination(val characterId: Int)

fun NavController.navigateToCharacterDetail(characterId: Int, navOptions: NavOptions? = null) {
    navigate(CharacterDetailDestination(characterId), navOptions)
}

fun NavGraphBuilder.characterDetailScreen(onBackClick: () -> Unit) {
    composable<CharacterDetailDestination> { backStackEntry ->
        // toRoute() reconstruye el destino con sus argumentos ya tipados
        val destination = backStackEntry.toRoute<CharacterDetailDestination>()
        CharacterDetailRoute(
            characterId = destination.characterId,
            onBackClick = onBackClick
        )
    }
}
```

Quien navega solo escribe `navController.navigateToCharacterDetail(2)`. No necesita saber que existe `CharacterDetailDestination` ni cómo se leen sus argumentos.

### Ventajas

- **El `NavHost` se lee como un índice** de las pantallas de la app.
- **Cada pantalla es dueña de su navegación**: su destino, sus argumentos y su composable viven en el mismo archivo.
- **La UI no conoce el `NavController`**: las pantallas solo reciben callbacks (`onStartClick`, `onBackClick`). Eso las hace fáciles de previsualizar y de probar.
- **Cambiar los argumentos de una pantalla** solo toca su archivo y los lugares que llaman a `navigateTo...`.

