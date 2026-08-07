package com.sol.feature.receive.impl

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
internal fun ReceiveScreen(
    onBack: () -> Unit,
    onShowMessage: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ReceiveViewModel = hiltViewModel(),
) {
    val publicKey by viewModel.publicKey.collectAsStateWithLifecycle()
    val clipboardManager = LocalClipboardManager.current
    val copiedText = stringResource(R.string.receive_copied)

    ReceiveContent(
        publicKey = publicKey,
        onBack = onBack,
        onCopy = {
            clipboardManager.setText(AnnotatedString(publicKey))
            onShowMessage(copiedText)
        },
        modifier = modifier,
    )
}

@Composable
internal fun ReceiveContent(
    publicKey: String,
    onBack: () -> Unit,
    onCopy: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(stringResource(R.string.receive_title), style = MaterialTheme.typography.titleLarge)
        Text(stringResource(R.string.receive_body), style = MaterialTheme.typography.bodyMedium)
        SelectionContainer {
            Text(
                text = publicKey,
                style = MaterialTheme.typography.bodyLarge,
                fontFamily = FontFamily.Monospace,
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = onBack) { Text(stringResource(R.string.receive_back)) }
            Button(onClick = onCopy) { Text(stringResource(R.string.receive_copy)) }
        }
    }
}
