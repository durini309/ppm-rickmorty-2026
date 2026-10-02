package com.gt.uvg.rickandmorty.location.ui.detail

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.gt.uvg.rickandmorty.R
import com.gt.uvg.rickandmorty.core.ui.components.PropertyRow
import com.gt.uvg.rickandmorty.core.ui.theme.RickAndMortyTheme
import com.gt.uvg.rickandmorty.location.data.Location
import com.gt.uvg.rickandmorty.location.data.LocationDb
import kotlinx.serialization.Serializable

// Solo viaja el ID; la pantalla busca la location en LocationDb
@Serializable
data class LocationDetailDestination(val locationId: Int)

fun NavController.navigateToLocationDetail(
    locationId: Int,
    navOptions: NavOptions? = null
) {
    navigate(LocationDetailDestination(locationId), navOptions)
}

fun NavGraphBuilder.locationDetailScreen(onBackClick: () -> Unit) {
    composable<LocationDetailDestination> { backStackEntry ->
        val destination = backStackEntry.toRoute<LocationDetailDestination>()
        LocationDetailRoute(
            locationId = destination.locationId,
            onBackClick = onBackClick
        )
    }
}

@Composable
fun LocationDetailRoute(
    locationId: Int,
    onBackClick: () -> Unit
) {
    val location = remember(locationId) { LocationDb().getLocationById(locationId) }
    LocationDetailScreen(
        location = location,
        onBackClick = onBackClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LocationDetailScreen(
    location: Location,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.location_detail_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_back),
                            contentDescription = stringResource(R.string.navigate_back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 48.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = location.name,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(32.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                PropertyRow(
                    label = stringResource(R.string.location_detail_id),
                    value = location.id.toString()
                )
                PropertyRow(
                    label = stringResource(R.string.location_detail_type),
                    value = location.type
                )
                PropertyRow(
                    label = stringResource(R.string.location_detail_dimension),
                    value = location.dimension
                )
            }
        }
    }
}

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PreviewLocationDetailScreen() {
    RickAndMortyTheme {
        LocationDetailScreen(
            location = LocationDb().getLocationById(1),
            onBackClick = {}
        )
    }
}
