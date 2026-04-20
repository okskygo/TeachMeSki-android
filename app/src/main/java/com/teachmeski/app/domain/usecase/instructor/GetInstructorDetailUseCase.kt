package com.teachmeski.app.domain.usecase.instructor

import com.teachmeski.app.data.model.toDomain
import com.teachmeski.app.data.remote.InstructorDataSource
import com.teachmeski.app.data.remote.ResortDataSource
import com.teachmeski.app.domain.model.DetailError
import com.teachmeski.app.domain.model.InstructorDetailBundle
import com.teachmeski.app.domain.model.Region
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException

@Singleton
class GetInstructorDetailUseCase @Inject constructor(
    private val instructorDataSource: InstructorDataSource,
    private val resortDataSource: ResortDataSource,
) {
    suspend operator fun invoke(shortId: String): DetailLoadResult<InstructorDetailBundle, DetailError> {
        val dto = try {
            instructorDataSource.getProfileByShortId(shortId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            return DetailLoadResult.Err(DetailError.Generic(e))
        } ?: return DetailLoadResult.Err(DetailError.NotFound)

        val allRegions = try {
            resortDataSource.getResortsWithRegions().map { it.toDomain() }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            return DetailLoadResult.Err(DetailError.Generic(e))
        }

        val grouped = groupResortsByRegion(allRegions, dto.resortIds)
        val resortNames = grouped.flatMap { it.resorts }.map { "${it.nameZh} (${it.nameEn})" }

        return DetailLoadResult.Ok(
            InstructorDetailBundle(
                profile = dto.toDomain(resortNames = resortNames),
                resortsByRegion = grouped,
            ),
        )
    }

    companion object {
        fun groupResortsByRegion(
            allRegions: List<Region>,
            instructorResortIds: List<String>,
        ): List<Region> {
            if (instructorResortIds.isEmpty()) return emptyList()
            val idSet = instructorResortIds.toSet()
            return allRegions
                .mapNotNull { region ->
                    val matched = region.resorts.filter { it.id in idSet }
                    if (matched.isEmpty()) null
                    else region.copy(resorts = matched)
                }
                .sortedBy { it.sortOrder }
        }
    }
}

sealed interface DetailLoadResult<out T, out E> {
    data class Ok<T>(val value: T) : DetailLoadResult<T, Nothing>
    data class Err<E>(val error: E) : DetailLoadResult<Nothing, E>
}
