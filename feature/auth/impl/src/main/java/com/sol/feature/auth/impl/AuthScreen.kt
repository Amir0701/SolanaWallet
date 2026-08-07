package com.sol.feature.auth.impl

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
internal fun AuthScreen(
    onAuthenticated: () -> Unit,
    onShowMessage: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.authenticated.collect { onAuthenticated() }
    }

    val error = state.error
    val invalidMnemonicText = stringResource(R.string.auth_error_invalid_mnemonic)
    LaunchedEffect(error) {
        if (error != null) {
            val text = when (error) {
                AuthError.InvalidMnemonic -> invalidMnemonicText
                is AuthError.Plain -> error.text
            }
            onShowMessage(text)
            viewModel.clearError()
        }
    }

    OnboardingContent(
        importText = state.importText,
        onImportTextChange = viewModel::onImportTextChange,
        onCreate = viewModel::createWallet,
        onImport = viewModel::importWallet,
        modifier = modifier,
    )

    val mnemonic = state.mnemonic
    if (mnemonic != null) {
        MnemonicSavedDialog(
            mnemonic = mnemonic,
            onConfirm = viewModel::confirmMnemonicSaved,
        )
    }
}

@Composable
internal fun OnboardingContent(
    importText: String,
    onImportTextChange: (String) -> Unit,
    onCreate: () -> Unit,
    onImport: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(R.string.auth_welcome_title),
            style = MaterialTheme.typography.headlineSmall,
        )
        Text(
            text = stringResource(R.string.auth_welcome_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Button(onClick = onCreate, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.auth_create_wallet))
        }
        Text(
            text = stringResource(R.string.auth_import_hint),
            style = MaterialTheme.typography.labelLarge,
        )
        OutlinedTextField(
            value = importText,
            onValueChange = onImportTextChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp),
            minLines = 4,
            placeholder = { Text(stringResource(R.string.auth_import_hint)) },
        )
        OutlinedButton(onClick = onImport, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.auth_import_action))
        }
    }
}

@Composable
internal fun MnemonicSavedDialog(
    mnemonic: List<String>,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = { },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.auth_mnemonic_confirm))
            }
        },
        title = { Text(stringResource(R.string.auth_mnemonic_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.auth_mnemonic_body))
                Text(
                    text = mnemonic.joinToString(" "),
                    style = MaterialTheme.typography.bodyLarge,
                    fontFamily = FontFamily.Monospace,
                )
            }
        },
    )
}
