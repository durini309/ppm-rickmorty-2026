# Estructura del proyecto: Screaming Architecture

```
com.gt.uvg.rickandmorty
├── MainActivity.kt                  ← instala el splash del sistema y muestra AppNavHost
├── core
│   ├── data
│   │   └── SessionPreferences.kt    ← DataStore: guardar, leer y borrar el nombre del usuario
│   ├── navigation
│   │   ├── AppNavHost.kt            ← NavHost principal + Scaffold con el BottomNavigation
│   │   ├── AppNavigationViewModel.kt← revisa en DataStore si hay sesión
│   │   ├── AppNavigationState.kt
│   │   ├── AppBottomBar.kt          ← UI del BottomNavigation
│   │   ├── TopLevelDestination.kt   ← las 3 opciones del BottomNavigation
│   │   └── TopLevelRoute.kt         ← interface que implementan las rutas de esas opciones
│   └── ui
│       ├── components
│       │   ├── ErrorLayout.kt
│       │   ├── LoadingLayout.kt
│       │   └── PropertyRow.kt       ← fila "etiqueta ... valor"
│       └── theme
├── login
│   └── ui
│       ├── LoginScreen.kt
│       ├── LoginState.kt
│       ├── LoginEvent.kt
│       └── LoginViewModel.kt
├── character
│   ├── data
│   │   ├── Character.kt
│   │   └── CharacterDb.kt
│   └── ui
│       ├── CharacterImage.kt
│       ├── navigation
│       │   └── CharactersGraph.kt   ← nested graph: lista -> detalle
│       ├── list
│       │   ├── CharactersScreen.kt
│       │   ├── CharactersState.kt
│       │   ├── CharactersEvent.kt
│       │   └── CharactersViewModel.kt
│       └── detail
│           └── CharacterDetail...   ← Screen, State, Event y ViewModel
├── location
│   ├── data
│   │   ├── Location.kt
│   │   └── LocationDb.kt
│   └── ui
│       ├── navigation
│       │   └── LocationsGraph.kt    ← nested graph: lista -> detalle
│       ├── list
│       │   └── Locations...         ← Screen, State, Event y ViewModel
│       └── detail
│           └── LocationDetail...    ← Screen, State, Event y ViewModel
└── profile
    └── ui
        ├── ProfileScreen.kt
        ├── ProfileState.kt
        ├── ProfileEvent.kt
        └── ProfileViewModel.kt
```

## Qué es

"Screaming Architecture" es un término de Robert C. Martin (Uncle Bob). La idea es que, al abrir la carpeta de un proyecto, la estructura te "grite" **de qué trata la aplicación** y no **con qué tecnología está hecha**.

Compara estas dos formas de organizar el mismo código:

```
Organizado por capa técnica          Organizado por funcionalidad
-------------------------------      -------------------------------
data/                                login/
    Character.kt                         ui/
    CharacterDb.kt                   character/
    Location.kt                          data/
    LocationDb.kt                        ui/
ui/                                  location/
    LoginScreen.kt                       data/
    CharactersScreen.kt                  ui/
    CharacterDetailScreen.kt         profile/
    LocationsScreen.kt                   ui/
    LocationDetailScreen.kt          core/
    ProfileScreen.kt                     navigation/
viewmodel/                               ui/
    ...
```

A la izquierda solo sabes que la app "tiene data, ui y viewmodels", igual que cualquier otra app. A la derecha sabes de inmediato que la app trata de **login**, **personajes**, **locations** y **perfil**.

## Reglas que seguimos

1. **Cada funcionalidad grande tiene su propia carpeta** en la raíz del paquete (`login`, `character`, `location`, `profile`).
2. **Adentro de cada funcionalidad** se separa por capa: `data`, `domain` y `ui`. Una capa solo se crea cuando hace falta; por ejemplo, `login` y `profile` por ahora solo tienen `ui`, y ninguna funcionalidad tiene `domain` todavía porque no hay lógica de negocio.
3. **Lo que es compartido entre funcionalidades va en `core`**: el tema de la app, el `NavHost` principal, el BottomNavigation, componentes como `PropertyRow` (lo usan el detalle de personaje, el detalle de location y el perfil) y `SessionPreferences` (lo usan la navegación, el Login y el Profile, por eso está en `core/data` y no en `login/data`).
4. Si algo se comparte **solo dentro de una funcionalidad**, se queda en esa funcionalidad. Por ejemplo, `CharacterImage.kt` lo usan la lista y el detalle, pero ambos son parte de `character`, entonces vive en `character/ui` y no en `core`.
5. **Cada funcionalidad es dueña de su navegación.** El nested graph de personajes (`CharactersGraph.kt`) vive en `character/ui/navigation`, no en `core`.

## Por qué se usa

- **Se encuentra todo más rápido.** Si hay un error en el detalle de una location, sabes que está en `location/`. No tienes que buscar en tres carpetas técnicas distintas.
- **Los cambios quedan localizados.** Agregar Locations en este laboratorio fue crear la carpeta `location/` y agregar una línea en `AppNavHost` y otra en `TopLevelDestination`. No se tocó nada de `character/`.
- **Escala mejor.** Con 20 pantallas, una carpeta `ui/` con 20 archivos se vuelve inmanejable; con carpetas por funcionalidad cada una sigue siendo pequeña.
- **Facilita dividir en módulos después.** Cada carpeta de funcionalidad ya está casi aislada, así que convertirla en un módulo de Gradle (`:feature:location`, `:feature:character`) es mucho más sencillo.
- **Facilita trabajar en equipo.** Dos personas trabajando en funcionalidades distintas casi no tocan los mismos archivos.
