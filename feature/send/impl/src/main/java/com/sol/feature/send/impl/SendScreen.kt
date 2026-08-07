package com.sol.feature.send.impl

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
internal fun SendScreen(
    onBack: () -> Unit,
    onShowMessage: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SendViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val successText = stringResource(R.string.send_success)
    val badAmountText = stringResource(R.string.send_error_bad_amount)
    val failedText = stringResource(R.string.send_error_failed)
    LaunchedEffect(Unit) {
        viewModel.messages.collect { message ->
            val text = when (message) {
                SendMessage.Success -> successText
                SendMessage.BadAmount -> badAmountText
                SendMessage.Failed -> failedText
                is SendMessage.Plain -> message.text
            }
            onShowMessage(text)
        }
    }

    SendScreenContent(
        modifier = modifier,
        state = state,
        onBack = onBack,
        onRecipientChange = { viewModel.onRecipientChange(state.recipient) },
        onAmountChange = { viewModel.onAmountChange(state.amountSol) },
        send = viewModel::send
    )
}

@Composable
fun SendScreenContent(
    modifier: Modifier = Modifier,
    state: SendViewModel.UiState,
    onBack: () -> Unit,
    onRecipientChange: () -> Unit,
    onAmountChange: () -> Unit,
    send: () -> Unit,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(stringResource(R.string.send_title), style = MaterialTheme.typography.titleLarge)
        OutlinedTextField(
            value = state.recipient,
            onValueChange = {onRecipientChange()},
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.send_recipient)) },
            singleLine = false,
            minLines = 2,
        )
        OutlinedTextField(
            value = state.amountSol,
            onValueChange = { onAmountChange() },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.send_amount_sol)) },
            singleLine = true,
        )
        val signature = state.lastSignature
        if (signature != null) {
            Text(
                text = signature,
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace,
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = onBack) { Text(stringResource(R.string.send_back)) }
            Button(
                onClick = { send() },
                enabled = !state.isSending,
            ) {
                if (state.isSending) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .height(20.dp)
                            .padding(2.dp),
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text(stringResource(R.string.send_confirm))
                }
            }
        }
    }
}
