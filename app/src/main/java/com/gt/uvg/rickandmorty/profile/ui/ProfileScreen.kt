package com.gt.uvg.rickandmorty.profile.ui

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.gt.uvg.rickandmorty.core.navigation.TopLevelRoute
import com.gt.uvg.rickandmorty.core.ui.components.PropertyRow
import com.gt.uvg.rickandmorty.core.ui.theme.RickAndMortyTheme
import kotlinx.serialization.Serializable

@Serializable
data object ProfileDestination : TopLevelRoute

fun NavController.navigateToProfile(navOptions: NavOptions? = null) {
    navigate(ProfileDestination, navOptions)
}

fun NavGraphBuilder.profileScreen() {
    composable<ProfileDestination> {
        ProfileRoute()
    }
}

@Composable
fun ProfileRoute(viewModel: ProfileViewModel = viewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ProfileScreen(
        state = state,
        onEvent = { event ->
            viewModel.onEvent(event)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileScreen(
    state: ProfileState,
    onEvent: (ProfileEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.profile_title)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 48.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ProfileImage()
            Spacer(modifier = Modifier.height(48.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                PropertyRow(
                    label = stringResource(R.string.profile_name_label),
                    value = state.name
                )
                PropertyRow(
                    label = stringResource(R.string.profile_student_id_label),
                    value = stringResource(R.string.profile_student_id)
                )
            }
            Spacer(modifier = Modifier.height(32.dp))
            OutlinedButton(
                onClick = {
                    onEvent(ProfileEvent.LogoutClick)
                }
            ) {
                Text(text = stringResource(R.string.profile_logout))
            }
        }
    }
}

@Composable
private fun ProfileImage(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(200.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_person_outline),
            contentDescription = stringResource(R.string.profile_image_description),
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.size(120.dp)
        )
    }
}

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PreviewProfileScreen() {
    RickAndMortyTheme {
        ProfileScreen(
            state = ProfileState(name = "Rick Sanchez"),
            onEvent = {}
        )
    }
}
