# Backstack: qué pasa al navegar

El **backstack** es la pila de pantallas que Navigation recuerda. Cada `navigate(...)` pone una pantalla encima; cada "back" saca la de arriba. Cuando se saca la última, la app se cierra.

En los ejemplos, el backstack se escribe de abajo hacia arriba: `[Characters, CharacterDetail]` significa que estás viendo `CharacterDetail` y que back te regresa a `Characters`.

## Los casos de la app

Todos estos casos se probaron en el emulador:

| Qué haces | Backstack después | Qué hace back |
|---|---|---|
| Abres la app | `[Login]` | Cierra la app |
| Presionas "Entrar" | `[Characters]` | Cierra la app (Login ya no está) |
| Tocas un personaje | `[Characters, CharacterDetail]` | Regresa a la lista |
| Tocas la pestaña Locations | `[Characters, Locations]` | Regresa a Characters |
| Tocas una location | `[Characters, Locations, LocationDetail]` | Regresa a la lista de locations |
| Tocas Profile y luego Locations | `[Characters, Locations, LocationDetail]` | Regresa a la lista de locations: el detalle se **restauró** |
| Tocas Locations estando en Locations | No cambia | Lo mismo que antes; no se duplicó |
| Tocas la pestaña Characters | `[Characters]` | Cierra la app |
| En Profile, presionas "Cerrar sesión" | `[Login]` | Cierra la app |

## Login → Characters: sacar Login

```kotlin
loginScreen(
    onStartClick = {
        navController.navigateToCharactersGraph(
            navOptions {
                popUpTo(LoginDestination) { inclusive = true }
            }
        )
    }
)
```

- **`popUpTo(LoginDestination)`**: antes de navegar, saca del backstack todo lo que esté encima de Login.
- **`inclusive = true`**: y saca también a Login.

Resultado: `[Login]` → `[Characters]`. Si el usuario presiona back en Characters, no regresa al Login: la app se cierra.

## Cerrar sesión: vaciar todo el backstack

```kotlin
profileScreen(
    onLogoutClick = {
        navController.navigateToLogin(
            navOptions {
                popUpTo(navController.graph.id) { inclusive = true }
            }
        )
    }
)
```

Es la misma idea, pero en vez de sacar hasta una pantalla, saca hasta **`navController.graph`**, que es el graph raíz del `NavHost`: el que contiene a todos los demás. Con `inclusive = true` no queda **nada**, y luego se agrega Login.

Resultado: sin importar cuántas pantallas había (`[Characters, Locations, LocationDetail, Profile]`), queda `[Login]`, y back cierra la app.

## Cambiar de pestaña

```kotlin
navController.navigate(
    destination.route,
    navOptions {
        popUpTo(CharactersDestination) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
)
```

Cada opción resuelve un problema distinto. Si no estuvieran, cada toque en el BottomNavigation pondría una pantalla nueva encima: después de ir y venir 10 veces habría que presionar back 10 veces para salir.

### `popUpTo(CharactersDestination)`: que no se acumulen pestañas

Antes de abrir la pestaña nueva, saca todo lo que esté encima de la lista de personajes. Como Characters es la primera pantalla después del login, el backstack siempre queda como **`[Characters, pestaña actual]`**, y back desde cualquier pestaña regresa a Characters.

### `saveState = true`: recordar dónde estabas

Las pantallas que `popUpTo` saca no se pierden: se **guardan**. Si estabas en el detalle de "Earth (C-137)" y te vas a Profile, Navigation guarda `[Locations, LocationDetail]` aparte.

### `restoreState = true`: regresar a donde estabas

Al volver a una pestaña, si hay algo guardado con `saveState`, lo pone de vuelta. Por eso al regresar a Locations aparece otra vez el detalle de "Earth (C-137)" y no la lista.

### `launchSingleTop = true`: no duplicar

Si ya estás en Locations y tocas Locations otra vez, no agrega una segunda copia encima.

### Por qué `popUpTo(CharactersDestination)` y no `popUpTo(CharactersGraph)`

Parecería más lógico hacer `popUpTo(CharactersGraph)`, pero **eso tiene un bug**: desde Locations, back cierra la app en lugar de regresar a Characters.

Para entenderlo hay que saber que el backstack también guarda los **graphs**, no solo las pantallas. Por ejemplo, estando en la lista de personajes, el backstack real es:

```
[raíz, CharactersGraph, Characters]
```

- Con **`popUpTo(CharactersGraph)`** se saca todo lo que está encima del graph, **incluida la pantalla `Characters`**. Al abrir Locations queda `[raíz, CharactersGraph, LocationsGraph, Locations]`. Al presionar back se saca `Locations` y quedan solo graphs, sin ninguna pantalla para mostrar. Navigation no permite que un graph quede hasta arriba, así que los saca todos, y la app se cierra.
- Con **`popUpTo(CharactersDestination)`** la pantalla `Characters` se queda. Al abrir Locations queda `[raíz, CharactersGraph, Characters, LocationsGraph, Locations]`, y back regresa a `Characters`.

Regla: **`popUpTo` debe apuntar a una pantalla que quieres que se quede en el backstack.**

### Una limitación: la pestaña Characters no recuerda el detalle

Si estás en el detalle de Morty y te vas a Locations, al regresar a Characters ves la lista y no el detalle de Morty. Las otras pestañas sí recuerdan dónde estabas.

Pasa porque Characters es la "base" de todo el backstack: `popUpTo(CharactersDestination)` saca lo que está encima de la lista (el detalle de Morty), pero no hay forma de "guardar" la lista misma, porque nunca sale del backstack. Es una limitación conocida de este patrón: la pestaña que es la base del backstack no guarda su estado.
