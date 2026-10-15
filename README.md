# Rick and Morty

Laboratorio de Android con Jetpack Compose. Un solo `Activity`, navegación entre composables con Navigation Compose y rutas type-safe (clases `@Serializable`), ViewModels con `State` y `Event`, y sesión guardada en DataStore Preferences.

## Pantallas

| Pantalla | Qué muestra |
|---|---|
| **Splash** | El splash del sistema (SplashScreen API) mientras se revisa si hay sesión |
| **Login** | Logo, campo "Nombre" y botón "Entrar" (deshabilitado si el nombre está vacío) |
| **Characters** | Lista de personajes (imagen, nombre, especie y estado) y su detalle |
| **Locations** | Lista de locations (nombre y tipo) y su detalle (ID, nombre, tipo y dimensión) |
| **Profile** | Foto, el nombre que se escribió en el Login, carné y botón "Cerrar sesión" |

Después del login, un **BottomNavigation** permite moverse entre Characters, Locations y Profile. Characters y Locations tienen cada uno su propio **nested graph** (lista → detalle).

El nombre del usuario se guarda en **DataStore**: si la app se cierra y se vuelve a abrir, entra directo a Characters sin pasar por el Login. "Cerrar sesión" borra el nombre y regresa al Login.

## Documentación

Se recomienda leer en orden; cada documento usa conceptos del anterior.

1. **[Estructura del proyecto](docs/01-arquitectura.md):** Screaming Architecture: por qué las carpetas son `character/`, `location/`, `profile/` y no `ui/`, `data/`.
2. **[Anatomía de una pantalla](docs/02-anatomia-de-una-pantalla.md):** las piezas que tiene cada pantalla (`Destination`, `navigateTo...`, `Route`, `Screen`, `State`, `Event`, `ViewModel`) y por qué.
3. **[Navegación](docs/03-navegacion.md):** extension functions para no llenar el `NavHost`, pantallas con parámetros (solo el ID) y nested graphs.
4. **[Bottom Navigation](docs/04-bottom-navigation.md):** `TopLevelRoute`, `TopLevelDestination`, `AppBottomBar`, cómo se sabe qué opción está seleccionada (`hierarchy`) y los insets.
5. **[Backstack](docs/05-backstack.md):** qué hace back en cada caso, `popUpTo`, `saveState`, `restoreState`, `launchSingleTop` y cómo se vacía el backstack con `popUpTo(0)`.
6. **[Sesión con DataStore](docs/06-sesion-datastore.md):** `SessionPreferences`, `AndroidViewModel`, `Flow` vs `first()`, y cómo `AppNavHost` navega solo cuando cambia la sesión.
7. **[Splash Screen](docs/07-splash-screen.md):** la SplashScreen API, por qué es mejor que una pantalla de splash propia y cómo se conecta con la sesión.
