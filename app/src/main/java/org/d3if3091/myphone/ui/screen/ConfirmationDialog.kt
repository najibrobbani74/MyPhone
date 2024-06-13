package org.d3if3091.myphone.ui.screen

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import org.d3if3091.myphone.R

@Composable
fun ConfirmatinDialog(
    text:String,
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
){
    AlertDialog(
        text = { Text(text = text) },
        confirmButton = {
            TextButton(onClick = { onConfirmation() }) {
                Text(text = stringResource(id = R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = { onDismissRequest() }) {
                Text(text = stringResource(id = R.string.cancel))
            }
        },
        onDismissRequest = { onDismissRequest() }
    )
}