package com.sol.feature.send.impl.domain.usecase

import com.sol.feature.send.impl.domain.model.SendSolResult
import com.sol.feature.send.impl.domain.repository.TransferRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class SendSolUseCaseTest {

    private val repository: TransferRepository = mockk()
    private val sendSol = SendSolUseCase(repository)

    @Test
    fun `empty amount returns InvalidAmount and does not transfer`() = runTest {
        assertEquals(SendSolResult.InvalidAmount, sendSol("recipient", ""))
        coVerify(exactly = 0) { repository.transfer(any(), any()) }
    }

    @Test
    fun `zero amount returns InvalidAmount and does not transfer`() = runTest {
        assertEquals(SendSolResult.InvalidAmount, sendSol("recipient", "0"))
        coVerify(exactly = 0) { repository.transfer(any(), any()) }
    }

    @Test
    fun `negative amount returns InvalidAmount and does not transfer`() = runTest {
        assertEquals(SendSolResult.InvalidAmount, sendSol("recipient", "-1"))
        coVerify(exactly = 0) { repository.transfer(any(), any()) }
    }

    @Test
    fun `non-numeric amount returns InvalidAmount and does not transfer`() = runTest {
        assertEquals(SendSolResult.InvalidAmount, sendSol("recipient", "abc"))
        coVerify(exactly = 0) { repository.transfer(any(), any()) }
    }

    @Test
    fun `success trims input and transfers lamports`() = runTest {
        coEvery { repository.transfer("recipient", 1_500_000_000L) } returns Result.success("sig123")

        assertEquals(SendSolResult.Success("sig123"), sendSol("  recipient  ", " 1.5 "))
        coVerify(exactly = 1) { repository.transfer("recipient", 1_500_000_000L) }
    }

    @Test
    fun `comma amount is parsed`() = runTest {
        coEvery { repository.transfer("recipient", 1_500_000_000L) } returns Result.success("sig")

        assertEquals(SendSolResult.Success("sig"), sendSol("recipient", "1,5"))
    }

    @Test
    fun `failure with message returns Failed with reason`() = runTest {
        coEvery { repository.transfer(any(), any()) } returns Result.failure(RuntimeException("insufficient funds"))

        assertEquals(SendSolResult.Failed("insufficient funds"), sendSol("recipient", "1"))
    }

    @Test
    fun `failure with blank message returns Failed without reason`() = runTest {
        coEvery { repository.transfer(any(), any()) } returns Result.failure(RuntimeException(""))

        assertEquals(SendSolResult.Failed(null), sendSol("recipient", "1"))
    }
}
