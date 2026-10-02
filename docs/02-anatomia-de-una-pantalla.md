# Anatomía de un archivo de pantalla

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

Todas las pantallas del proyecto siguen esta forma: `LoginScreen.kt`, `CharactersScreen.kt`, `CharacterDetailScreen.kt`, `LocationsScreen.kt`, `LocationDetailScreen.kt` y `ProfileScreen.kt`. Si entiendes una, entiendes todas.

## Por qué separar `Route` y `Screen`

- `LoginRoute` es la parte "con conexiones": más adelante obtendrá el ViewModel, recolectará su estado y se lo pasará a la UI. Hoy es donde se leen los datos de `CharacterDb` o `LocationDb`.
- `LoginScreen` es la parte "tonta": solo dibuja lo que recibe. Por eso se puede mostrar en un `@Preview` sin necesitar un ViewModel, una base de datos ni internet.
