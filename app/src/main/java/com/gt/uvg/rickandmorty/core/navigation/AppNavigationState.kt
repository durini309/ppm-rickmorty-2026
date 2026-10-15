package com.gt.uvg.rickandmorty.core.navigation

/**
 * [userName] tiene tres posibles valores:
 * - null: todavía no hemos leído DataStore (se queda el splash del sistema)
 * - "": no hay sesión (va al Login)
 * - "Rick": hay sesión (va a la lista de personajes)
 */
data class AppNavigationState(
    val userName: String? = null
)
