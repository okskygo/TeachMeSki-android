package com.teachmeski.app.data.remote

import com.teachmeski.app.data.model.IapProductDto
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IapProductsDataSource @Inject constructor(
    private val supabaseClient: SupabaseClient,
) {
    suspend fun listActive(): List<IapProductDto> =
        supabaseClient.postgrest.from("iap_products")
            .select {
                filter { eq("is_active", true) }
                order("sort_order", Order.ASCENDING)
            }
            .decodeList<IapProductDto>()
}
