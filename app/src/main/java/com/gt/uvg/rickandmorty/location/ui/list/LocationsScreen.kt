package com.gt.uvg.rickandmorty.location.ui.list

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
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
import com.gt.uvg.rickandmorty.core.ui.components.ErrorLayout
import com.gt.uvg.rickandmorty.core.ui.components.LoadingLayout
import com.gt.uvg.rickandmorty.core.ui.theme.RickAndMortyTheme
import com.gt.uvg.rickandmorty.location.data.Location
import com.gt.uvg.rickandmorty.location.data.LocationDb
import kotlinx.serialization.Serializable

@Serializable
data object LocationsDestination

fun NavController.navigateToLocations(navOptions: NavOptions? = null) {
    navigate(LocationsDestination, navOptions)
}

fun NavGraphBuilder.locationsScreen(onLocationClick: (locationId: Int) -> Unit) {
    composable<LocationsDestination> {
        LocationsRoute(onLocationClick = onLocationClick)
    }
}

@Composable
fun LocationsRoute(
    onLocationClick: (locationId: Int) -> Unit,
    viewModel: LocationsViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LocationsScreen(
        state = state,
        onEvent = { event ->
            viewModel.onEvent(event)
        },
        onLocationClick = onLocationClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LocationsScreen(
    state: LocationsState,
    onEvent: (LocationsEvent) -> Unit,
    onLocationClick: (locationId: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.locations_title)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        when {
            state.isLoading -> {
                LoadingLayout(
                    onClick = {
                        onEvent(LocationsEvent.LoadingClick)
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                )
            }

            state.hasError -> {
                ErrorLayout(
                    onRetryClick = {
                        onEvent(LocationsEvent.RetryClick)
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                )
            }

            else -> {
                LazyColumn(
                    contentPadding = innerPadding,
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(state.locations, key = { it.id }) { location ->
                        LocationItem(
                            location = location,
                            onClick = { onLocationClick(location.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LocationItem(
    location: Location,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        Text(
            text = location.name,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = location.type,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PreviewLocationsScreen_Loading() {
    RickAndMortyTheme {
        LocationsScreen(
            state = LocationsState(
                isLoading = true
            ),
            onEvent = {},
            onLocationClick = {}
        )
    }
}

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PreviewLocationsScreen_Error() {
    RickAndMortyTheme {
        LocationsScreen(
            state = LocationsState(
                isLoading = false,
                hasError = true
            ),
            onEvent = {},
            onLocationClick = {}
        )
    }
}

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PreviewLocationsScreen_Success() {
    RickAndMortyTheme {
        LocationsScreen(
            state = LocationsState(
                locations = LocationDb().getAllLocations(),
                isLoading = false
            ),
            onEvent = {},
            onLocationClick = {}
        )
    }
}
