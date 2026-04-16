package com.teachmeski.app.domain.repository

import com.teachmeski.app.domain.model.Region
import com.teachmeski.app.util.Resource

interface ResortRepository {
    suspend fun getResortsWithRegions(): Resource<List<Region>>
}
