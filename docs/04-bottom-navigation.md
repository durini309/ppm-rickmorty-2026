# Bottom Navigation

El BottomNavigation está hecho con cuatro piezas, todas en `core/navigation/`. Cada una tiene un solo trabajo:

| Archivo | Qué hace |
|---|---|
| `TopLevelRoute.kt` | Marca qué rutas pueden ser una opción del BottomNavigation |
| `TopLevelDestination.kt` | Lista las 3 opciones: a dónde van, su ícono y su texto |
| `AppBottomBar.kt` | Dibuja la barra. Solo UI, no sabe nada de Navigation |
| `AppNavHost.kt` | Decide qué opción está seleccionada y qué pasa al tocar una |

## 1. `TopLevelRoute`: qué rutas pueden ir en el BottomNavigation

```kotlin
interface TopLevelRoute
```

Es una interface vacía. No tiene funciones; solo sirve como **etiqueta**. Las rutas que pueden ser una opción del BottomNavigation la implementan:

```kotlin
@Serializable
data object CharactersGraph : TopLevelRoute      // character/ui/navigation

@Serializable
data object LocationsGraph : TopLevelRoute       // location/ui/navigation

@Serializable
data object ProfileDestination : TopLevelRoute   // profile/ui
```

Fíjate que dos son graphs y una es una pantalla. Por eso se llama `TopLevelRoute` y no `TopLevelGraph`.

**¿Para qué sirve si está vacía?** Para que el compilador nos cuide. En `TopLevelDestination` la ruta es de tipo `TopLevelRoute` y no `Any`; así, si alguien intenta poner `LoginDestination` como opción del BottomNavigation, el código no compila.

## 2. `TopLevelDestination`: las opciones de la barra

```kotlin
enum class TopLevelDestination(
    val route: TopLevelRoute,     // a dónde navega
    @DrawableRes val icon: Int,   // R.drawable...
    @StringRes val label: Int     // R.string...
) {
    CHARACTERS(
        route = CharactersGraph,
        icon = R.drawable.ic_group,
        label = R.string.characters_title
    ),
    LOCATIONS(
        route = LocationsGraph,
        icon = R.drawable.ic_public,
        label = R.string.locations_title
    ),
    PROFILE(
        route = ProfileDestination,
        icon = R.drawable.ic_person,
        label = R.string.profile_title
    )
}
```

**¿Por qué un `enum`?** Porque las opciones son fijas y conocidas. Con un enum:

- `TopLevelDestination.entries` nos da la lista en orden, lista para recorrerla con `forEach` y dibujar la barra.
- Agregar una opción nueva es agregar una entrada aquí. La barra se actualiza sola.
- Se puede comparar con `==` (`destination == selectedDestination`), y Compose lo considera un tipo **estable** (más abajo se explica por qué importa).

## 3. `AppBottomBar`: la UI

```kotlin
@Composable
fun AppBottomBar(
    selectedDestination: TopLevelDestination,
    onDestinationClick: (TopLevelDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(...) {
        TopLevelDestination.entries.forEach { destination ->
            val selected = destination == selectedDestination
            NavigationBarItem(
                selected = selected,
                onClick = { onDestinationClick(destination) },
                icon = { Icon(painterResource(destination.icon), contentDescription = null) },
                label = { Text(stringResource(destination.label)) }
            )
        }
    }
}
```

Es la misma idea que `Route` / `Screen` en las pantallas (ver [Anatomía de una pantalla](02-anatomia-de-una-pantalla.md)): **recibe datos y avisa eventos**, nada más. No recibe el `NavController` ni el `NavDestination`; solo le decimos cuál opción está seleccionada y nos avisa cuando tocan una.

Gracias a eso:

- **Tiene `@Preview`**, porque para mostrarla basta con `selectedDestination = TopLevelDestination.CHARACTERS`. No hace falta crear un `NavController`.
- **Compose la puede saltar al recomponer.** Si le pasáramos el `NavDestination`, Android Studio muestra el warning *"Unstable Parameter"*: `NavDestination` es una clase que Compose no puede garantizar que no cambie por dentro, así que tendría que redibujar la barra "por si acaso". Un enum siempre es estable.

`contentDescription = null` en el ícono es a propósito: el texto de abajo ya dice qué es la opción, y si el ícono también lo dijera, TalkBack lo leería dos veces.

## 4. `AppNavHost`: ¿qué opción está seleccionada?

```kotlin
val backStackEntry by navController.currentBackStackEntryAsState()
val currentDestination = backStackEntry?.destination

// Buscamos qué opción del BottomNavigation corresponde a la pantalla actual.
// hierarchy incluye los graphs padre, así "Characters" sigue marcado en el detalle.
// En Login no hay ninguna (null) y por eso no se muestra el BottomNavigation.
val selectedDestination = TopLevelDestination.entries.firstOrNull { destination ->
    currentDestination?.hierarchy?.any { it.hasRoute(destination.route::class) } == true
}
```

Vamos por partes:

- **`currentBackStackEntryAsState()`** es un `State`: cada vez que se navega, cambia y Compose recalcula `selectedDestination`.
- **`destination.route::class`** es la clase de la ruta (`CharactersGraph::class`). `hasRoute(...)` pregunta "¿este destino es de esa clase?".
- **`hierarchy`** es la parte importante. Ver abajo.
- **`firstOrNull`** devuelve la primera opción que coincide, o `null` si ninguna coincide.

### `hierarchy`: el destino y todos sus padres

`hierarchy` **no** son todos los destinos del graph. Es la cadena que va **desde la pantalla actual hacia arriba**: la pantalla, el graph que la contiene, el graph que contiene a ese, hasta la raíz.

| Pantalla actual | `hierarchy` |
|---|---|
| `CharacterDetailDestination` | `CharacterDetailDestination` → `CharactersGraph` → graph raíz |
| `LocationsDestination` | `LocationsDestination` → `LocationsGraph` → graph raíz |
| `ProfileDestination` | `ProfileDestination` → graph raíz |
| `LoginDestination` | `LoginDestination` → graph raíz |

El `any { ... }` pregunta: **¿alguno de esos eslabones es la ruta de esta opción?**

- **Detalle de un personaje:** la pantalla no es `CharactersGraph`, pero su padre sí → "Characters" queda marcado aunque estés en el detalle.
- **Profile:** coincide la pantalla misma, porque Profile no tiene graph.
- **Login:** ningún eslabón coincide con ninguna opción → `null` → no se muestra la barra:

```kotlin
bottomBar = {
    if (selectedDestination != null) {
        AppBottomBar(selectedDestination = selectedDestination, onDestinationClick = { ... })
    }
}
```

Si en lugar de `hierarchy` comparáramos solo la pantalla actual, "Characters" se desmarcaría al entrar al detalle, porque `CharacterDetailDestination` no es `CharactersGraph`.

## 5. `AppNavHost`: ¿qué pasa al tocar una opción?

```kotlin
onDestinationClick = { destination ->
    navController.navigate(
        destination.route,
        navOptions {
            popUpTo(CharactersDestination) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    )
}
```

Esas tres opciones controlan el backstack y tienen su propia explicación en [Backstack](05-backstack.md).

## 6. Los insets: dos `Scaffold` uno dentro del otro

`AppNavHost` tiene un `Scaffold` con el BottomNavigation, y adentro cada pantalla tiene **su propio** `Scaffold` con su TopAppBar. Hay que evitar que los dos dejen espacio para lo mismo:

```kotlin
Scaffold(
    // El Scaffold de afuera no deja espacio para la status bar;
    // de eso se encarga el Scaffold de cada pantalla (su TopAppBar).
    contentWindowInsets = WindowInsets(0),
    bottomBar = { ... }
) { innerPadding ->
    NavHost(
        modifier = Modifier
            .padding(innerPadding)              // deja espacio para el BottomNavigation
            .consumeWindowInsets(innerPadding)  // y le avisa a las pantallas que ese espacio ya se usó
    ) { ... }
}
```

Sin `consumeWindowInsets`, el Scaffold de cada pantalla volvería a dejar espacio para la barra de navegación de Android (la de gestos), aunque el BottomNavigation ya la cubre: ese espacio se contaría dos veces.
