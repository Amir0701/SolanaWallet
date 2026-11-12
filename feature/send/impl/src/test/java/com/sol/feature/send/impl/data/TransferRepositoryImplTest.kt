package com.sol.feature.send.impl.data

import com.sol.core.wallet.WalletSessionManager
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class TransferRepositoryImplTest {

    private val sessionManager: WalletSessionManager = mockk()
    private val repository = TransferRepositoryImpl(sessionManager)

    @Test
    fun `transfer delegates to session and returns signature`() = runTest {
        every { sessionManager.transfer("recipient", 1L) } returns Result.success("sig")

        assertEquals(Result.success("sig"), repository.transfer("recipient", 1L))
        verify(exactly = 1) { sessionManager.transfer("recipient", 1L) }
    }

    @Test
    fun `transfer passes session failure through`() = runTest {
        val error = RuntimeException("insufficient funds")
        every { sessionManager.transfer(any(), any()) } returns Result.failure(error)

        assertEquals(error, repository.transfer("recipient", 1L).exceptionOrNull())
    }
}
