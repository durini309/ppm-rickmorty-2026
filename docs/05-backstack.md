# Backstack: qué pasa al navegar

El **backstack** es la pila de pantallas que Navigation recuerda. Cada `navigate(...)` pone una pantalla encima; cada "back" saca la de arriba. Cuando se saca la última, la app se cierra.

En los ejemplos, el backstack se escribe de abajo hacia arriba: `[Characters, CharacterDetail]` significa que estás viendo `CharacterDetail` y que back te regresa a `Characters`.

## Los casos de la app

| Qué haces | Backstack después | Qué hace back |
|---|---|---|
| Abres la app sin sesión | `[Login]` | Cierra la app |
| Abres la app con sesión guardada | `[Characters]` | Cierra la app |
| Escribes tu nombre y presionas "Entrar" | `[Characters]` | Cierra la app (Login ya no está) |
| Tocas un personaje | `[Characters, CharacterDetail]` | Regresa a la lista |
| Tocas la pestaña Locations | `[Characters, Locations]` | Regresa a Characters |
| Tocas una location | `[Characters, Locations, LocationDetail]` | Regresa a la lista de locations |
| Tocas Profile y luego Locations | `[Characters, Locations, LocationDetail]` | Regresa a la lista de locations: el detalle se **restauró** |
| Tocas Locations estando en Locations | No cambia | Lo mismo que antes; no se duplicó |
| Tocas la pestaña Characters | `[Characters]` | Cierra la app |
| En Profile, presionas "Cerrar sesión" | `[Login]` | Cierra la app (no regresa a Profile ni a Characters) |

## Entrar y cerrar sesión: `popUpTo(0)`

Al entrar y al cerrar sesión se navega desde `AppNavHost` (el porqué está en [Sesión con DataStore](06-sesion-datastore.md)). En los dos casos se usa la misma opción:

```kotlin
navController.navigateToCharactersGraph(   // o navigateToLogin(...) al cerrar sesión
    navOptions {
        popUpTo(0) { inclusive = true }
    }
)
```

- **`popUpTo(...)`**: antes de navegar, saca del backstack todo lo que esté encima de lo que se indica.
- **`0`**: es el ID del graph raíz del `NavHost`, el que contiene a todos los demás. Navigation le da ID `0` a un graph que no tiene ruta propia, y nuestro `NavHost` no le pone ruta. Por eso aquí es lo mismo que escribir `popUpTo(navController.graph.id)`. Ojo: si a un `NavHost` le pasas `route = ...`, su ID ya no es `0` y `popUpTo(0)` no saca nada (Navigation lo ignora en silencio).
- **`inclusive = true`**: saca también el graph raíz. No queda **nada**, y luego se agrega la pantalla a la que navegamos.

Resultado:

- **Entrar:** `[Login]` → `[Characters]`. Back en Characters cierra la app; no regresa al Login.
- **Cerrar sesión:** sin importar cuántas pantallas había (`[Characters, Locations, LocationDetail, Profile]`), queda `[Login]`, y back cierra la app.

Si en lugar de `popUpTo(0)` hubiéramos navegado sin `navOptions`, al cerrar sesión quedaría `[Characters, ..., Profile, Login]` y back desde el Login regresaría a Profile, con la sesión ya cerrada.

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
