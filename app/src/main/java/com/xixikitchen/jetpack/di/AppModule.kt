package com.xixikitchen.jetpack.di

import android.content.Context
import com.xixikitchen.jetpack.data.ApiClient
import com.xixikitchen.jetpack.data.KitchenRepository
import com.xixikitchen.jetpack.data.SessionStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideSessionStore(@ApplicationContext context: Context): SessionStore = SessionStore(context)

    @Provides
    @Singleton
    fun provideRepository(apiClient: ApiClient, sessionStore: SessionStore): KitchenRepository =
        KitchenRepository(apiClient, sessionStore)
}
