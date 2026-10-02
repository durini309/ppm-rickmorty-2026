# Rick and Morty

Laboratorio de Android con Jetpack Compose. Un solo `Activity`, navegación entre composables con Navigation Compose y rutas type-safe (clases `@Serializable`).

## Pantallas

| Pantalla | Qué muestra |
|---|---|
| **Login** | Logo y botón "Entrar" |
| **Characters** | Lista de personajes (imagen, nombre, especie y estado) y su detalle |
| **Locations** | Lista de locations (nombre y tipo) y su detalle (ID, nombre, tipo y dimensión) |
| **Profile** | Foto, nombre, carné y botón "Cerrar sesión" |

Después del login, un **BottomNavigation** permite moverse entre Characters, Locations y Profile. Characters y Locations tienen cada uno su propio **nested graph** (lista → detalle).

## Documentación

Se recomienda leer en orden; cada documento usa conceptos del anterior.

1. **[Estructura del proyecto](docs/01-arquitectura.md):** Screaming Architecture: por qué las carpetas son `character/`, `location/`, `profile/` y no `ui/`, `data/`.
2. **[Anatomía de una pantalla](docs/02-anatomia-de-una-pantalla.md):** las piezas que tiene cada archivo de pantalla (`Destination`, `navigateTo...`, `Route`, `Screen`) y por qué.
3. **[Navegación](docs/03-navegacion.md):** extension functions para no llenar el `NavHost`, pantallas con parámetros (solo el ID) y nested graphs.
4. **[Bottom Navigation](docs/04-bottom-navigation.md):** `TopLevelRoute`, `TopLevelDestination`, `AppBottomBar`, cómo se sabe qué opción está seleccionada (`hierarchy`) y los insets.
5. **[Backstack](docs/05-backstack.md):** qué hace back en cada caso, `popUpTo`, `saveState`, `restoreState`, `launchSingleTop` y cómo se vacía el backstack al cerrar sesión.
