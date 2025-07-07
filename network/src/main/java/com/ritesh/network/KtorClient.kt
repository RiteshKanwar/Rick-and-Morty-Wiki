package com.ritesh.network

import com.ritesh.network.models.domain.Character
import com.ritesh.network.models.domain.CharacterPage
import com.ritesh.network.models.domain.Episode
import com.ritesh.network.models.domain.EpisodePage
import com.ritesh.network.models.remote.RemoteCharacter
import com.ritesh.network.models.remote.RemoteCharacterPage
import com.ritesh.network.models.remote.RemoteEpisode
import com.ritesh.network.models.remote.RemoteEpisodePage
import com.ritesh.network.models.remote.toDomainCharacter
import com.ritesh.network.models.remote.toDomainCharacterPage
import com.ritesh.network.models.remote.toDomainEpisode
import com.ritesh.network.models.remote.toDomainEpisodePage
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.logging.SIMPLE
import io.ktor.client.request.get
import io.ktor.client.request.url
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json



class KtorClient {
    val client = HttpClient(OkHttp){
         defaultRequest { url("https://rickandmortyapi.com/api/")}

        install(Logging){
            logger = Logger.SIMPLE
        }

        install(ContentNegotiation){
            json(Json{
                ignoreUnknownKeys = true
            })
        }
    }

    //For Caching data
    private var characterCache = mutableMapOf<Int, Character>()


    // This function is making a network call parsing it into RemoteCharacter response
    suspend fun getCharacter(id:Int): ApiOperation<Character> {
        characterCache[id]?.let { return ApiOperation.Success(it) } // if data already available return success
        return safeApiCall {
                client.get("character/$id")
                .body<RemoteCharacter>()
                .toDomainCharacter()  // converting the Remote model into the Domain Model
                .also { characterCache[id] = it } //once safeApiCall runs this also run
        }
    }
    suspend fun  getCharacterByPage(pageNumber: Int, queryParams: Map<String, String>): ApiOperation<CharacterPage>{
        return safeApiCall {
            client.get("character"){
                url {
                    parameters.append("page", pageNumber.toString())
                    queryParams.forEach { parameters.append(it.key, it.value) }
                }
            }
                .body<RemoteCharacterPage>()
                .toDomainCharacterPage()
        }
    }

    suspend fun searchAllCharactersByName(searchQuery: String): ApiOperation<out Any> {
        val data = mutableListOf<Character>()
        try {
            val firstPageResult = getCharacterByPage(pageNumber = 1, queryParams = mapOf("name" to searchQuery))
            if (firstPageResult is ApiOperation.Failure) {
                return  firstPageResult // Return failure if the first page fetch fails
            }
            val firstPage = (firstPageResult as ApiOperation.Success).data
            data.addAll(firstPage.characters)

            // Fetch remaining pages
            val totalPageCount = firstPage.info.pages
            for (pageIndex in 2..totalPageCount) {
                val nextPageResult = getCharacterByPage(pageIndex, mapOf("name" to searchQuery))
                if (nextPageResult is ApiOperation.Failure) {
                    return nextPageResult // Stop processing further pages on error
                }

                val nextPage = (nextPageResult as ApiOperation.Success).data
                data.addAll(nextPage.characters)
            }

            return ApiOperation.Success(data)
        }catch (e: Exception) {
            return ApiOperation.Failure(e) // Return failure for any unexpected exception
        }
    }

    suspend fun getEpisode(episodeId: Int): ApiOperation<Episode>{
        return safeApiCall {
            client.get("episode/$episodeId")
                .body<RemoteEpisode>()
                .toDomainEpisode()
        }

    }

    suspend fun getEpisodes(episodeIds: List<Int>): ApiOperation<List<Episode>>{
        return  if (episodeIds.size == 1){
            getEpisode(episodeIds[0]).mapSuccess {
                listOf(it)
            }
        } else {
            val idsCommaSeparated = episodeIds.joinToString(separator = ",")
            return safeApiCall {
                client.get("episode/$idsCommaSeparated")
                    .body<List<RemoteEpisode>>()
                    .map{ it.toDomainEpisode() }
            }
        }

    }

    suspend fun getEpisodesByPage(pageIndex: Int): ApiOperation<EpisodePage> {
        return safeApiCall {
            client.get("episode") {
                url {
                    parameters.append("page", pageIndex.toString())
                }
            }
                .body<RemoteEpisodePage>()
                .toDomainEpisodePage()
        }
    }

    suspend fun getAllEpisodes(): ApiOperation<out Any> {
        val data = mutableListOf<Episode>()

        try {
            // Fetch the first page
            val firstPageResult = getEpisodesByPage(pageIndex = 1)
            if (firstPageResult is ApiOperation.Failure) {
                return firstPageResult // Return failure if the first page fetch fails
            }

            val firstPage = (firstPageResult as ApiOperation.Success).data
            data.addAll(firstPage.episodes)

            // Fetch remaining pages
            val totalPageCount = firstPage.info.pages
            for (pageIndex in 2..totalPageCount) {
                val nextPageResult = getEpisodesByPage(pageIndex)
                if (nextPageResult is ApiOperation.Failure) {
                    return nextPageResult // Stop processing further pages on error
                }

                val nextPage = (nextPageResult as ApiOperation.Success).data
                data.addAll(nextPage.episodes)
            }

            return ApiOperation.Success(data)
        } catch (e: Exception) {
            return ApiOperation.Failure(e) // Return failure for any unexpected exception
        }
    }



    private inline fun <T> safeApiCall(apiCall: () -> T): ApiOperation<T>{
        return try {
            ApiOperation.Success(data = apiCall())
            }catch (e: Exception){
            ApiOperation.Failure(exception = e)
            }
    }
}
sealed interface ApiOperation<T>{
    data class Success<T>(val data: T): ApiOperation<T>
    data class Failure<T>(val exception: Exception): ApiOperation<T>

    fun <R> mapSuccess(transform: (T) -> R): ApiOperation<R>{
        return when (this){
            is Success -> Success(transform(data))  // transforming T to R
            is Failure -> Failure(exception)
        }
    }

    fun onSuccess(block: (T) -> Unit): ApiOperation<T>{
        if (this is Success) block(data)
        return this
    }

    fun onFailure(block: (Exception) -> Unit): ApiOperation<T>{
        if (this is Failure) block(exception)
        return this
    }
}
