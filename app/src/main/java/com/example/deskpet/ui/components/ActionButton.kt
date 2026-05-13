package com.example.deskpet.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    outlined: Boolean = false,
    enabled: Boolean = true
) {
    val buttonModifier = modifier
        .fillMaxWidth()
        .heightIn(min = 48.dp)

    if (outlined) {
        OutlinedButton(
            onClick = onClick,
            modifier = buttonModifier,
            enabled = enabled
        ) {
            Text(text = text)
        }
    } else {
        Button(
            onClick = onClick,
            modifier = buttonModifier,
            enabled = enabled
        ) {
            Text(text = text)
        }
    }
}
