package com.teachmeski.app.domain.usecase.instructor

import com.teachmeski.app.domain.model.Region
import com.teachmeski.app.domain.model.SkiResort
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GetInstructorDetailUseCaseTest {

    private fun resort(id: String, regionId: String, name: String, sort: Int = 0) =
        SkiResort(id = id, nameZh = name, nameEn = name, nameJa = null, regionId = regionId, isOther = false, sortOrder = sort)

    private fun region(id: String, name: String, sort: Int, resorts: List<SkiResort>) =
        Region(id = id, nameZh = name, nameEn = name, prefectureZh = null, prefectureEn = null, country = "JP", sortOrder = sort, resorts = resorts)

    @Test
    fun `empty instructor resortIds produces empty grouping`() {
        val allRegions = listOf(region("r1", "Hokkaido", 1, listOf(resort("s1", "r1", "Niseko"))))
        val result = GetInstructorDetailUseCase.groupResortsByRegion(allRegions, emptyList())
        assertTrue(result.isEmpty())
    }

    @Test
    fun `only matching resort ids are included`() {
        val allRegions = listOf(
            region("r1", "Hokkaido", 1, listOf(resort("s1", "r1", "Niseko"), resort("s2", "r1", "Rusutsu"))),
            region("r2", "Nagano", 2, listOf(resort("s3", "r2", "Hakuba"))),
        )
        val result = GetInstructorDetailUseCase.groupResortsByRegion(allRegions, listOf("s1", "s3"))
        assertEquals(2, result.size)
        assertEquals("r1", result[0].id)
        assertEquals(listOf("s1"), result[0].resorts.map { it.id })
        assertEquals("r2", result[1].id)
        assertEquals(listOf("s3"), result[1].resorts.map { it.id })
    }

    @Test
    fun `regions with zero matched resorts are dropped`() {
        val allRegions = listOf(
            region("r1", "Hokkaido", 1, listOf(resort("s1", "r1", "Niseko"))),
            region("r2", "Nagano", 2, listOf(resort("s2", "r2", "Hakuba"))),
        )
        val result = GetInstructorDetailUseCase.groupResortsByRegion(allRegions, listOf("s2"))
        assertEquals(1, result.size)
        assertEquals("r2", result[0].id)
    }

    @Test
    fun `result is sorted by region sortOrder ascending`() {
        val allRegions = listOf(
            region("r2", "Nagano", 5, listOf(resort("s2", "r2", "Hakuba"))),
            region("r1", "Hokkaido", 1, listOf(resort("s1", "r1", "Niseko"))),
        )
        val result = GetInstructorDetailUseCase.groupResortsByRegion(allRegions, listOf("s1", "s2"))
        assertEquals(listOf("r1", "r2"), result.map { it.id })
    }
}
