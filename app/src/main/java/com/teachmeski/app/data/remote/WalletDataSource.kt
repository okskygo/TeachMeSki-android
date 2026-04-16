package com.teachmeski.app.data.remote

import com.teachmeski.app.data.model.TokenTransactionDto
import com.teachmeski.app.data.model.TokenWalletDto
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Count
import io.github.jan.supabase.postgrest.query.Order
import javax.inject.Inject
import javax.inject.Singleton

private const val PAGE_SIZE = 20

@Singleton
class WalletDataSource @Inject constructor(
    private val supabaseClient: SupabaseClient,
) {
    suspend fun getWallet(instructorId: String): TokenWalletDto? =
        supabaseClient.postgrest.from("token_wallets")
            .select {
                filter { eq("instructor_id", instructorId) }
            }
            .decodeSingleOrNull<TokenWalletDto>()

    suspend fun getTransactions(instructorId: String, page: Int): Pair<List<TokenTransactionDto>, Int> {
        val safePage = maxOf(1, page)
        val from = (safePage - 1) * PAGE_SIZE
        val to = from + PAGE_SIZE - 1

        val query = supabaseClient.postgrest.from("token_transactions")
            .select(
                columns = Columns.raw("id, amount, type, balance_after, created_at"),
                request = {
                    filter { eq("instructor_id", instructorId) }
                    order("created_at", Order.DESCENDING)
                    range(from.toLong(), to.toLong())
                    count(Count.EXACT)
                },
            )

        val list = query.decodeList<TokenTransactionDto>()
        val total = query.countOrNull()?.toInt() ?: list.size
        return Pair(list, total)
    }
}
