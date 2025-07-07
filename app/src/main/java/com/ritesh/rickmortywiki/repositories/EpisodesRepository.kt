package com.ritesh.rickmortywiki.repositories

import com.ritesh.network.ApiOperation
import com.ritesh.network.KtorClient
import com.ritesh.network.models.domain.Episode
import javax.inject.Inject

class EpisodesRepository @Inject constructor(private val ktorClient: KtorClient)  {
    suspend fun fetchAllEpisode(): ApiOperation<List<Episode>> = ktorClient.getAllEpisodes() as ApiOperation<List<Episode>>
}