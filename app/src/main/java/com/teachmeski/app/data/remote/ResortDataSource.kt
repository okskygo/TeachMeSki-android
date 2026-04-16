package com.teachmeski.app.data.remote

import com.teachmeski.app.data.model.RegionDto
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ResortDataSource @Inject constructor(
    private val supabaseClient: SupabaseClient,
) {
    suspend fun getResortsWithRegions(): List<RegionDto> =
        supabaseClient.postgrest
            .from("regions")
            .select(
                columns = Columns.raw(
                    "id, name_zh, name_en, prefecture_zh, prefecture_en, country, sort_order, ski_resorts(id, name_zh, name_en, name_ja, region_id, is_other, sort_order)"
                )
            ) {
                order("sort_order", Order.ASCENDING)
            }
            .decodeList<RegionDto>()
}
