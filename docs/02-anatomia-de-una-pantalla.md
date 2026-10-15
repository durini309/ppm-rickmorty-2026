# Anatomía de una pantalla

Cada pantalla tiene un archivo principal (`...Screen.kt`) y, si maneja estado, tres archivos más a su lado: `...State.kt`, `...Event.kt` y `...ViewModel.kt`. El sufijo de cada nombre dice qué es cada cosa.

## El archivo `...Screen.kt`

| Sufijo | Ejemplo | Qué es | Visibilidad |
|---|---|---|---|
| `...Destination` | `LoginDestination` | A dónde navego (la clase `@Serializable`) | pública |
| `navigateTo...` | `navigateToLogin()` | Extension de `NavController` para ir a la pantalla | pública |
| `...Screen()` (minúscula) | `loginScreen()` | Extension de `NavGraphBuilder` que registra la pantalla en el `NavHost` | pública |
| `...Route` | `LoginRoute` | Punto de entrada de la pantalla: obtiene el ViewModel y le pasa su estado a la UI | pública |
| `...Screen` | `LoginScreen` | La UI pura: recibe el estado y avisa eventos, no sabe de navegación ni de ViewModels | `private` |
| `Preview...Screen` | `PreviewLoginScreen_Empty` | Preview en modo claro y oscuro | `private` |

## Los archivos de estado

| Archivo | Ejemplo | Qué es |
|---|---|---|
| `...State.kt` | `LoginState(name = "")` | `data class` con **todo** lo que la pantalla necesita para dibujarse |
| `...Event.kt` | `LoginEvent.NameChange`, `LoginEvent.LoginClick` | `sealed interface` con todo lo que el usuario puede **hacer** en la pantalla |
| `...ViewModel.kt` | `LoginViewModel` | Recibe los eventos, hace el trabajo y actualiza el estado |

Regla para recordar: **el sufijo te dice qué es; la funcionalidad te dice de quién es.**

Todas las pantallas del proyecto siguen esta forma: Login, Characters, CharacterDetail, Locations, LocationDetail y Profile. Si entiendes una, entiendes todas.

## Cómo se conectan

```kotlin
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
```

El estado baja y los eventos suben:

```
             state (LoginState)
ViewModel  ───────────────────────►  LoginScreen
           ◄───────────────────────
             onEvent (LoginEvent)
```

1. El usuario escribe en el campo de nombre → `LoginScreen` llama `onEvent(LoginEvent.NameChange("Rick"))`.
2. `LoginViewModel.onEvent` actualiza su `MutableStateFlow` → `state.name = "Rick"`.
3. `collectAsStateWithLifecycle()` ve el cambio → Compose vuelve a dibujar `LoginScreen` con el nuevo estado.

## Por qué separar `Route` y `Screen`

- `LoginRoute` es la parte "con conexiones": obtiene el ViewModel, recolecta su estado y se lo pasa a la UI.
- `LoginScreen` es la parte "tonta": solo dibuja lo que recibe. Por eso se puede mostrar en un `@Preview` pasándole un `LoginState(...)` a mano, sin necesitar un ViewModel, DataStore ni internet.

## Por qué el estado vive en el ViewModel

- **Sobrevive a la rotación.** Si el usuario escribió su nombre y gira el teléfono, la Activity se recrea pero el ViewModel no: el texto sigue ahí.
- **La lógica no está en la UI.** Validar que el nombre no esté vacío, guardar en DataStore o simular la carga de personajes son trabajo del ViewModel, no del composable.

## `ViewModel` vs `AndroidViewModel`

La mayoría de pantallas usa `ViewModel`. Login, Profile y `AppNavigationViewModel` usan **`AndroidViewModel`**, porque necesitan un `Context` para acceder a DataStore. `AndroidViewModel` recibe el `Application` en su constructor. Se explica en [Sesión con DataStore](06-sesion-datastore.md).
