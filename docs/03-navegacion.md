# Navegación con extension functions y nested graphs

## El problema

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

## La solución: dos extension functions por pantalla

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

// 3. Cómo se registra Login en el NavHost. Login no recibe callbacks: no navega por su cuenta,
//    solo guarda el nombre en DataStore y AppNavHost reacciona (ver Sesión con DataStore).
fun NavGraphBuilder.loginScreen() {
    composable<LoginDestination> {
        LoginRoute()
    }
}

// 4. Punto de entrada público de la pantalla: conecta el ViewModel con la UI.
@Composable
fun LoginRoute(viewModel: LoginViewModel = viewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LoginScreen(
        state = state,
        onEvent = { event ->
            viewModel.onEvent(event)
        }
    )
}

// 5. La UI como tal. Es privada: nadie fuera de este archivo la usa directamente.
@Composable
private fun LoginScreen(state: LoginState, onEvent: (LoginEvent) -> Unit, modifier: Modifier = Modifier) {
    // Logo, campo "Nombre", botón "Entrar" y nombre del estudiante
}
```

Las pantallas que sí navegan por su cuenta reciben un callback. Por ejemplo, la lista de personajes avisa qué personaje se tocó:

```kotlin
fun NavGraphBuilder.charactersScreen(onCharacterClick: (characterId: Int) -> Unit) {
    composable<CharactersDestination> {
        CharactersRoute(onCharacterClick = onCharacterClick)
    }
}
```

### Pantallas con parámetros: el detalle

Cuando una pantalla necesita datos, el destino pasa de `data object` a `data class`. Solo mandamos el **ID**, nada más; la pantalla de detalle se encarga de buscar el resto. Así funcionan tanto `CharacterDetailDestination` como `LocationDetailDestination`:

```kotlin
@Serializable
data class LocationDetailDestination(val locationId: Int)

fun NavController.navigateToLocationDetail(locationId: Int, navOptions: NavOptions? = null) {
    navigate(LocationDetailDestination(locationId), navOptions)
}

fun NavGraphBuilder.locationDetailScreen(onBackClick: () -> Unit) {
    composable<LocationDetailDestination> {
        LocationDetailRoute(onBackClick = onBackClick)
    }
}
```

¿Y dónde se lee el ID? En el ViewModel. Navigation le entrega los argumentos del destino en un `SavedStateHandle`, y `toRoute()` los reconstruye ya tipados:

```kotlin
class LocationDetailViewModel(
    savedStateHandle: SavedStateHandle
): ViewModel() {
    private val destination = savedStateHandle.toRoute<LocationDetailDestination>()
    private val locationId = destination.locationId
    // Con el ID buscamos la location completa en LocationDb
}
```

Quien navega solo escribe `navController.navigateToLocationDetail(1)`. No necesita saber que existe `LocationDetailDestination` ni cómo se leen sus argumentos.

**¿Por qué solo el ID y no el objeto `Location` completo?** Porque los argumentos de navegación se guardan en el estado de la app (para sobrevivir, por ejemplo, a que Android mate el proceso). Un `Int` es pequeño y siempre se puede guardar; un objeto completo puede ser grande, quedar desactualizado, y obliga a que sea serializable. La "fuente de verdad" sigue siendo `LocationDb`.

## Nested graphs: agrupar pantallas de una funcionalidad

Un **nested graph** es un grupo de pantallas que se registra en el `NavHost` como si fuera una sola. Characters y Locations tienen cada uno el suyo, porque cada uno tiene dos pantallas (lista y detalle) que siempre van juntas.

En `location/ui/navigation/LocationsGraph.kt`:

```kotlin
// El "nombre" del grupo. Es una ruta igual que las demás.
@Serializable
data object LocationsGraph : TopLevelRoute

fun NavController.navigateToLocationsGraph(navOptions: NavOptions? = null) {
    navigate(LocationsGraph, navOptions)
}

fun NavGraphBuilder.locationsGraph(navController: NavController) {
    // navigation<...> en lugar de composable<...>: registra un grupo de pantallas.
    // startDestination es la pantalla que se muestra al navegar a LocationsGraph.
    navigation<LocationsGraph>(startDestination = LocationsDestination) {
        locationsScreen(onLocationClick = navController::navigateToLocationDetail)
        locationDetailScreen(onBackClick = navController::navigateUp)
    }
}
```

(`: TopLevelRoute` se explica en [Bottom Navigation](04-bottom-navigation.md).)

Lo que ganamos:

- **Desde afuera se navega al grupo, no a una pantalla.** El BottomNavigation hace `navigate(LocationsGraph)` y el graph decide que la primera pantalla es `LocationsDestination`.
- **La navegación interna queda dentro de la funcionalidad.** `AppNavHost` no sabe que Locations tiene un detalle; eso solo lo sabe `LocationsGraph.kt`.
- **Se puede preguntar "¿estoy dentro de Locations?"** sin importar si estás en la lista o en el detalle. Eso es lo que usa el BottomNavigation para saber qué opción marcar.

**Profile no tiene nested graph** porque es una sola pantalla: no hay nada que agrupar. Se registra directo con `profileScreen(...)`.

## Cómo quedó `AppNavHost`

Con todo esto, el `NavHost` se lee como un índice de la app:

```kotlin
NavHost(navController, startDestination = startDestination) {
    loginScreen()
    charactersGraph(navController)
    locationsGraph(navController)
    profileScreen()
}
```

`startDestination` es Login o Characters según si hay sesión guardada. Login y Profile no reciben callbacks porque la navegación de la sesión (entrar y cerrar sesión) se maneja en un solo lugar de `AppNavHost`; se explica en [Sesión con DataStore](06-sesion-datastore.md).

## Ventajas

- **El `NavHost` se lee como un índice** de las funcionalidades de la app.
- **Cada pantalla es dueña de su navegación**: su destino, sus argumentos y su composable viven en el mismo archivo.
- **Cada funcionalidad es dueña de su graph**: agregar una pantalla a Locations no toca `AppNavHost`.
- **La UI no conoce el `NavController`**: las pantallas solo reciben callbacks (`onLocationClick`, `onBackClick`). Eso las hace fáciles de previsualizar y de probar.
- **Cambiar los argumentos de una pantalla** solo toca su archivo y los lugares que llaman a `navigateTo...`.
