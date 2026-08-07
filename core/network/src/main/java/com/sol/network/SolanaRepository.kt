package com.sol.network

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import org.p2p.solanaj.core.Account
import org.p2p.solanaj.core.PublicKey
import org.p2p.solanaj.core.Transaction
import org.p2p.solanaj.programs.SystemProgram
import org.p2p.solanaj.rpc.RpcClient

class SolanaRepository @AssistedInject constructor(
    @Assisted private val rpcEndpoint: String,
) {
    private val client = RpcClient(rpcEndpoint)

    fun getBalanceLamports(publicKeyBase58: String): Result<Long> = runCatching {
        client.api.getBalance(PublicKey(publicKeyBase58))
    }

    fun transferSol(
        signer: Account,
        recipientBase58: String,
        lamports: Long,
    ): Result<String> = runCatching {
        val transaction = Transaction()
        transaction.addInstruction(
            SystemProgram.transfer(signer.publicKey, PublicKey(recipientBase58), lamports),
        )
        client.api.sendTransaction(transaction, signer)
    }

    @AssistedFactory
    interface Factory {
        fun create(rpcEndpoint: String): SolanaRepository
    }
}
