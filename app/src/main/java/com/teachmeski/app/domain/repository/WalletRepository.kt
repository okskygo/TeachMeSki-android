package com.teachmeski.app.domain.repository

import com.teachmeski.app.domain.model.TokenTransaction
import com.teachmeski.app.domain.model.TokenWallet
import com.teachmeski.app.util.Resource

interface WalletRepository {
    suspend fun getWallet(): Resource<TokenWallet>
    suspend fun getTransactions(page: Int): Resource<Pair<List<TokenTransaction>, Int>>
}
