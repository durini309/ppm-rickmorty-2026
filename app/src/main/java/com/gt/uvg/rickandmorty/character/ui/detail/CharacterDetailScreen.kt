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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.gt.uvg.rickandmorty.R
import com.gt.uvg.rickandmorty.character.data.Character
import com.gt.uvg.rickandmorty.character.data.CharacterDb
import com.gt.uvg.rickandmorty.character.ui.CharacterImage
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
        val destination = backStackEntry.toRoute<CharacterDetailDestination>()
        CharacterDetailRoute(
            characterId = destination.characterId,
            onBackClick = onBackClick
        )
    }
}

@Composable
fun CharacterDetailRoute(
    characterId: Int,
    onBackClick: () -> Unit
) {
    val character = remember(characterId) { CharacterDb().getCharacterById(characterId) }
    CharacterDetailScreen(
        character = character,
        onBackClick = onBackClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CharacterDetailScreen(
    character: Character,
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 48.dp, vertical = 16.dp),
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
}

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PreviewCharacterDetailScreen() {
    RickAndMortyTheme {
        CharacterDetailScreen(
            character = CharacterDb().getCharacterById(2),
            onBackClick = {}
        )
    }
}
