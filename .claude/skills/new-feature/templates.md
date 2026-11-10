# Шаблоны слоёв

Пример — фича send в целевой структуре из `docs/architecture.md`. Для новой фичи замени `send` / `Send` / `SendSol` / `Transfer` на свои имена. Импорты опущены, кроме тех, что важны для правил слоёв.

## domain/model/SendSolResult.kt

```kotlin
package com.sol.feature.send.impl.domain.model

sealed interface SendSolResult {
    data class Success(val signature: String) : SendSolResult
    data object InvalidAmount : SendSolResult
    data class Failed(val reason: String?) : SendSolResult
}
```

## domain/repository/TransferRepository.kt

```kotlin
package com.sol.feature.send.impl.domain.repository

interface TransferRepository {
    /** Main-safe. Возвращает сигнатуру транзакции. */
    suspend fun transfer(recipient: String, lamports: Long): Result<String>
}
```

## domain/usecase/SendSolUseCase.kt

```kotlin
package com.sol.feature.send.impl.domain.usecase

import com.sol.core.wallet.SolFormatting
import javax.inject.Inject

class SendSolUseCase @Inject constructor(
    private val repository: TransferRepository,
) {
    suspend operator fun invoke(recipient: String, amountSol: String): SendSolResult {
        // parseSolToLamports не suspend, поэтому runCatching здесь не глотает отмену
        val lamports = runCatching { SolFormatting.parseSolToLamports(amountSol) }.getOrNull()
        if (lamports == null || lamports <= 0L) return SendSolResult.InvalidAmount

        return repository.transfer(recipient.trim(), lamports).fold(
            onSuccess = { SendSolResult.Success(it) },
            onFailure = { SendSolResult.Failed(it.message?.takeIf(String::isNotBlank)) },
        )
    }
}
```

Use case для наблюдения:

```kotlin
class ObservePublicKeyUseCase @Inject constructor(
    private val repository: AddressRepository,
) {
    operator fun invoke(): Flow<String> = repository.observePublicKey()
}
```

## data/TransferRepositoryImpl.kt

```kotlin
package com.sol.feature.send.impl.data

import com.sol.core.wallet.WalletSessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class TransferRepositoryImpl @Inject constructor(
    private val sessionManager: WalletSessionManager,
) : TransferRepository {
    override suspend fun transfer(recipient: String, lamports: Long): Result<String> =
        withContext(Dispatchers.IO) { sessionManager.transfer(recipient, lamports) }
}
```

Наблюдение за сессией:

```kotlin
override fun observePublicKey(): Flow<String> =
    sessionManager.session.map { it.publicKey.orEmpty() }.distinctUntilChanged()
```

## di/SendModule.kt

```kotlin
package com.sol.feature.send.impl.di

@Module
@InstallIn(ViewModelComponent::class)
abstract class SendModule {
    @Binds
    abstract fun bindTransferRepository(impl: TransferRepositoryImpl): TransferRepository
}
```

## presentation/SendViewModel.kt

```kotlin
package com.sol.feature.send.impl.presentation

sealed interface SendMessage {
    data object Success : SendMessage
    data object BadAmount : SendMessage
    data object Failed : SendMessage
    data class Plain(val text: String) : SendMessage
}

@HiltViewModel
class SendViewModel @Inject constructor(
    private val sendSol: SendSolUseCase,
) : ViewModel() {

    data class UiState(
        val recipient: String = "",
        val amountSol: String = "",
        val isSending: Boolean = false,
        val lastSignature: String? = null,
    )

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()

    private val _messages = Channel<SendMessage>(capacity = Channel.BUFFERED)
    val messages = _messages.receiveAsFlow()

    fun onRecipientChange(value: String) = _state.update { it.copy(recipient = value) }

    fun onAmountChange(value: String) = _state.update { it.copy(amountSol = value) }

    fun send() {
        val current = _state.value
        if (current.isSending) return
        _state.update { it.copy(isSending = true) }
        viewModelScope.launch {
            when (val result = sendSol(current.recipient, current.amountSol)) {
                is SendSolResult.Success -> {
                    _state.update { UiState(lastSignature = result.signature) }
                    _messages.trySend(SendMessage.Success)
                }
                SendSolResult.InvalidAmount -> {
                    _state.update { it.copy(isSending = false) }
                    _messages.trySend(SendMessage.BadAmount)
                }
                is SendSolResult.Failed -> {
                    _state.update { it.copy(isSending = false) }
                    _messages.trySend(result.reason?.let(SendMessage::Plain) ?: SendMessage.Failed)
                }
            }
        }
    }
}
```

## presentation/SendScreen.kt

```kotlin
package com.sol.feature.send.impl.presentation

@Composable
internal fun SendScreen(
    onBack: () -> Unit,
    onShowMessage: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SendViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val successText = stringResource(R.string.send_success)
    // …остальные строки
    LaunchedEffect(Unit) {
        viewModel.messages.collect { message ->
            onShowMessage(
                when (message) {
                    SendMessage.Success -> successText
                    // …
                },
            )
        }
    }

    SendScreenContent(
        state = state,
        onRecipientChange = viewModel::onRecipientChange,
        onAmountChange = viewModel::onAmountChange,
        onSend = viewModel::send,
        onBack = onBack,
        modifier = modifier,
    )
}

@Composable
fun SendScreenContent(
    state: SendViewModel.UiState,
    onRecipientChange: (String) -> Unit,
    onAmountChange: (String) -> Unit,
    onSend: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // OutlinedTextField(value = state.recipient, onValueChange = onRecipientChange, …)
}
```

## SendNavGraph.kt (корневой пакет)

```kotlin
package com.sol.feature.send.impl

import com.sol.feature.send.impl.presentation.SendScreen

fun NavGraphBuilder.sendScreen(
    onBack: () -> Unit,
    onShowMessage: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    composable(SendFeature.ROUTE) {
        SendScreen(onBack = onBack, onShowMessage = onShowMessage, modifier = modifier)
    }
}
```

## Тесты

### domain

```kotlin
class SendSolUseCaseTest {

    private val repository: TransferRepository = mockk()
    private val sendSol = SendSolUseCase(repository)

    @Test
    fun `zero amount returns InvalidAmount and does not transfer`() = runTest {
        assertEquals(SendSolResult.InvalidAmount, sendSol("recipient", "0"))
        coVerify(exactly = 0) { repository.transfer(any(), any()) }
    }

    @Test
    fun `comma amount is parsed and recipient is trimmed`() = runTest {
        coEvery { repository.transfer("recipient", 1_500_000_000L) } returns Result.success("sig")
        assertEquals(SendSolResult.Success("sig"), sendSol("  recipient  ", "1,5"))
    }

    @Test
    fun `blank error message becomes Failed without reason`() = runTest {
        coEvery { repository.transfer(any(), any()) } returns Result.failure(RuntimeException(""))
        assertEquals(SendSolResult.Failed(null), sendSol("recipient", "1"))
    }
}
```

### data

```kotlin
class TransferRepositoryImplTest {

    private val sessionManager: WalletSessionManager = mockk()
    private val repository = TransferRepositoryImpl(sessionManager)

    @Test
    fun `transfer delegates to session`() = runTest {
        every { sessionManager.transfer("recipient", 1L) } returns Result.success("sig")
        assertEquals(Result.success("sig"), repository.transfer("recipient", 1L))
    }
}
```

### presentation

```kotlin
class SendViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val sendSol: SendSolUseCase = mockk()
    private val viewModel = SendViewModel(sendSol)

    @Test
    fun `success emits Success and resets the form`() = runTest {
        coEvery { sendSol("recipient", "1") } returns SendSolResult.Success("sig")
        viewModel.onRecipientChange("recipient")
        viewModel.onAmountChange("1")

        viewModel.messages.test {
            viewModel.send()
            assertEquals(SendMessage.Success, awaitItem())
        }
        assertEquals(SendViewModel.UiState(lastSignature = "sig"), viewModel.state.value)
    }
}
```

`MainDispatcherRule` лежит в корневом тестовом пакете `com.sol.feature.<name>.impl`. Для новой фичи скопируй его из `feature/send/impl/src/test`.
