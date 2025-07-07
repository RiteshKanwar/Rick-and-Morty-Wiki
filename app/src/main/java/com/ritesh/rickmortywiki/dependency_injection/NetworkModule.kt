package com.ritesh.rickmortywiki.dependency_injection

import com.ritesh.network.KtorClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class NetworkModule {
    @Provides
    @Singleton
    fun providesKtorClient(): KtorClient{
        return KtorClient()
    }

}