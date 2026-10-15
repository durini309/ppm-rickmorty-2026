package com.gt.uvg.rickandmorty.login.ui

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.gt.uvg.rickandmorty.R
import com.gt.uvg.rickandmorty.core.ui.theme.RickAndMortyTheme
import kotlinx.serialization.Serializable

@Serializable
data object LoginDestination

fun NavController.navigateToLogin(navOptions: NavOptions? = null) {
    navigate(LoginDestination, navOptions)
}

fun NavGraphBuilder.loginScreen() {
    composable<LoginDestination> {
        LoginRoute()
    }
}

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

@Composable
private fun LoginScreen(
    state: LoginState,
    onEvent: (LoginEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.rickmorty_logo),
                    contentDescription = stringResource(R.string.login_logo_description),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(32.dp))
                OutlinedTextField(
                    value = state.name,
                    onValueChange = { name ->
                        onEvent(LoginEvent.NameChange(name))
                    },
                    label = { Text(text = stringResource(R.string.login_name_label)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        onEvent(LoginEvent.LoginClick)
                    },
                    // Si el nombre está vacío no dejamos iniciar sesión
                    enabled = state.name.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = stringResource(R.string.login_start))
                }
            }
            Text(
                text = stringResource(R.string.login_student),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PreviewLoginScreen_Empty() {
    RickAndMortyTheme {
        LoginScreen(
            state = LoginState(),
            onEvent = {}
        )
    }
}

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PreviewLoginScreen_WithName() {
    RickAndMortyTheme {
        LoginScreen(
            state = LoginState(name = "Rick Sanchez"),
            onEvent = {}
        )
    }
}
