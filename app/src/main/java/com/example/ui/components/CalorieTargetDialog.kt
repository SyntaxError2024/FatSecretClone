package com.example.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun CalorieTargetDialog(
    currentTarget: Int,
    onDismiss: () -> Unit,
    onSaveTarget: (Int) -> Unit
) {
    var targetText by remember { mutableStateOf(currentTarget.toString()) }
    val newTarget = targetText.toIntOrNull() ?: 0
    val isValid = newTarget in 500..10000

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Objectif calorique journalier",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Fixez votre apport calorique cible par jour en kcal.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = targetText,
                    onValueChange = { newValue ->
                        if (newValue.all { it.isDigit() }) {
                            targetText = newValue
                        }
                    },
                    label = { Text("Calories quotidiennes (kcal)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    supportingText = {
                        if (!isValid && targetText.isNotBlank()) {
                            Text("Veuillez saisir une valeur entre 500 et 10 000 kcal")
                        }
                    },
                    isError = !isValid && targetText.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("target_calories_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (isValid) {
                        onSaveTarget(newTarget)
                    }
                },
                enabled = isValid,
                modifier = Modifier.testTag("save_target_button")
            ) {
                Text("Enregistrer")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}
