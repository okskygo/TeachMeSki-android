package com.teachmeski.app.data.repository

import com.teachmeski.app.R
import com.teachmeski.app.data.model.toDomain
import com.teachmeski.app.data.remote.ResortDataSource
import com.teachmeski.app.domain.model.Region
import com.teachmeski.app.domain.repository.ResortRepository
import com.teachmeski.app.util.Resource
import com.teachmeski.app.util.UiText
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ResortRepositoryImpl @Inject constructor(
    private val resortDataSource: ResortDataSource,
) : ResortRepository {

    override suspend fun getResortsWithRegions(): Resource<List<Region>> =
        try {
            val regions = resortDataSource.getResortsWithRegions().map { it.toDomain() }
            Resource.Success(regions)
        } catch (e: Exception) {
            Resource.Error(UiText.StringResource(R.string.error_load_resorts))
        }
}
