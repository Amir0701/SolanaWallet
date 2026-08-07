package com.sol.cryprowallet.wallet.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.sol.core.wallet.SolFormatting
import com.sol.cryprowallet.R

@Composable
fun HomeScreen(
    publicKey: String,
    balanceLamports: Long?,
    isLoadingBalance: Boolean,
    onRefresh: () -> Unit,
    onSend: () -> Unit,
    onReceive: () -> Unit,
    onShowMessage: (String) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val clipboardManager = LocalClipboardManager.current
    val copiedText = stringResource(R.string.copied)

    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    stringResource(R.string.balance_label),
                    style = MaterialTheme.typography.labelLarge,
                )
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (isLoadingBalance) {
                        CircularProgressIndicator(modifier = Modifier.height(24.dp))
                    } else {
                        Text(
                            text = if (balanceLamports == null) "—" else "${SolFormatting.formatLamportsAsSol(balanceLamports)} SOL",
                            style = MaterialTheme.typography.headlineSmall,
                        )
                    }
                    TextButton(onClick = onRefresh) { Text(stringResource(R.string.refresh)) }
                }
            }
        }

        Text(stringResource(R.string.your_address), style = MaterialTheme.typography.labelLarge)
        SelectionContainer {
            Text(
                text = publicKey,
                style = MaterialTheme.typography.bodyLarge,
                fontFamily = FontFamily.Monospace,
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = {
                    clipboardManager.setText(AnnotatedString(publicKey))
                    onShowMessage(copiedText)
                },
            ) { Text(stringResource(R.string.copy)) }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onSend, modifier = Modifier.weight(1f)) { Text(stringResource(R.string.send)) }
            OutlinedButton(onClick = onReceive, modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.receive))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        TextButton(onClick = onDelete) {
            Text(
                stringResource(R.string.delete_wallet),
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
}
