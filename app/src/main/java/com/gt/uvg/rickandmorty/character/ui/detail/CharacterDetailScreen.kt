package com.gt.uvg.rickandmorty.character.ui.detail

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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.gt.uvg.rickandmorty.R
import com.gt.uvg.rickandmorty.character.data.Character
import com.gt.uvg.rickandmorty.character.data.CharacterDb
import com.gt.uvg.rickandmorty.character.ui.CharacterImage
import com.gt.uvg.rickandmorty.core.ui.components.ErrorLayout
import com.gt.uvg.rickandmorty.core.ui.components.LoadingLayout
import com.gt.uvg.rickandmorty.core.ui.components.PropertyRow
import com.gt.uvg.rickandmorty.core.ui.theme.RickAndMortyTheme
import kotlinx.serialization.Serializable

@Serializable
data class CharacterDetailDestination(val characterId: Int)

fun NavController.navigateToCharacterDetail(
    characterId: Int,
    navOptions: NavOptions? = null
) {
    navigate(CharacterDetailDestination(characterId), navOptions)
}

fun NavGraphBuilder.characterDetailScreen(onBackClick: () -> Unit) {
    composable<CharacterDetailDestination> { backStackEntry ->
        CharacterDetailRoute(
            onBackClick = onBackClick
        )
    }
}

@Composable
fun CharacterDetailRoute(
    onBackClick: () -> Unit,
    viewModel: CharacterDetailViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    CharacterDetailScreen(
        state = state,
        onEvent = { event ->
            viewModel.onEvent(event)
        },
        onBackClick = onBackClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CharacterDetailScreen(
    state: CharacterDetailState,
    onEvent: (CharacterDetailEvent) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.character_detail_title)) },
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
        when {
            state.isLoading -> {
                LoadingLayout(
                    onClick = {
                        onEvent(CharacterDetailEvent.LoadingClick)
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                )
            }

            state.hasError -> {
                ErrorLayout(
                    onRetryClick = {
                        onEvent(CharacterDetailEvent.RetryClick)
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                )
            }

            else -> {
                CharacterDetailSuccessState(
                    character = state.character!!,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(horizontal = 48.dp, vertical = 16.dp)
                )
            }
        }

    }
}

@Composable
fun CharacterDetailSuccessState(
    character: Character,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CharacterImage(
            imageUrl = character.image,
            contentDescription = character.name,
            size = 200.dp
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = character.name,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(32.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            PropertyRow(
                label = stringResource(R.string.character_detail_species),
                value = character.species
            )
            PropertyRow(
                label = stringResource(R.string.character_detail_status),
                value = character.status
            )
            PropertyRow(
                label = stringResource(R.string.character_detail_gender),
                value = character.gender
            )
        }
    }
}

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PreviewCharacterDetailScreen_Loading() {
    RickAndMortyTheme {
        CharacterDetailScreen(
            state = CharacterDetailState(
                character = null,
                isLoading = true
            ),
            onEvent = {},
            onBackClick = {},
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PreviewCharacterDetailScreen_Error() {
    RickAndMortyTheme {
        CharacterDetailScreen(
            state = CharacterDetailState(
                character = null,
                hasError = true,
                isLoading = false
            ),
            onEvent = {},
            onBackClick = {},
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PreviewCharacterDetailScreen_Success() {
    RickAndMortyTheme {
        CharacterDetailScreen(
            state = CharacterDetailState(
                character = Character(
                    id = 1,
                    name = "Rick Sanchez",
                    status = "Alive",
                    species = "Human",
                    gender = "M",
                    image = ""
                ),
                isLoading = false
            ),
            onEvent = {},
            onBackClick = {},
            modifier = Modifier.fillMaxSize()
        )
    }
}
