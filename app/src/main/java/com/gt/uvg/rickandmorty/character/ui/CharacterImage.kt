package com.gt.uvg.rickandmorty.character.ui

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import coil3.compose.AsyncImage

/**
 * Imagen circular de un personaje, cargada desde internet con Coil.
 * La comparten la lista y el detalle para que se vean igual.
 */
@Composable
fun CharacterImage(
    imageUrl: String,
    contentDescription: String?,
    size: Dp,
    modifier: Modifier = Modifier
) {
    val placeholder = ColorPainter(MaterialTheme.colorScheme.surfaceVariant)
    AsyncImage(
        model = imageUrl,
        contentDescription = contentDescription,
        placeholder = placeholder,
        error = placeholder,
        contentScale = ContentScale.Crop,
        modifier = modifier
            .size(size)
            .clip(CircleShape)
    )
}
