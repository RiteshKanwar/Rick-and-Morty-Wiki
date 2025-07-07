package com.ritesh.rickmortywiki.repositories

import com.ritesh.network.ApiOperation
import com.ritesh.network.KtorClient
import com.ritesh.network.models.domain.Character
import com.ritesh.network.models.domain.CharacterPage
import javax.inject.Inject

class CharacterRepository @Inject constructor(private val ktorClient: KtorClient){
    suspend fun fetchCharacterPage(page: Int, queryParams: Map<String, String> = emptyMap()): ApiOperation<CharacterPage>{
        return ktorClient.getCharacterByPage(pageNumber = page, queryParams = queryParams)
    }

    suspend fun fetchCharacter(characterId: Int): ApiOperation<Character>{
        return ktorClient.getCharacter(characterId)
    }
    suspend fun fetchAllCharactersByName(searchQuery:String): ApiOperation<List<Character>>{
        return ktorClient.searchAllCharactersByName((searchQuery)) as ApiOperation<List<Character>>
    }
}