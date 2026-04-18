package com.anekon.ci.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.room.Room
import com.anekon.ci.data.api.GitHubApiService
import com.anekon.ci.data.local.AnekonDatabase
import com.anekon.ci.data.local.PreferencesManager
import com.anekon.ci.data.local.dao.*
import com.anekon.ci.data.repository.AIRepository
import com.anekon.ci.data.repository.GitHubRepository
import com.anekon.ci.domain.usecase.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    private const val GITHUB_BASE_URL = "https://api.github.com/"

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return context.dataStore
    }

    @Provides
    @Singleton
    fun providePreferencesManager(
        @ApplicationContext context: Context
    ): PreferencesManager {
        return PreferencesManager(context)
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(GITHUB_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideGitHubApiService(retrofit: Retrofit): GitHubApiService {
        return retrofit.create(GitHubApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AnekonDatabase {
        return Room.databaseBuilder(
            context,
            AnekonDatabase::class.java,
            "anekon_database"
        ).fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideProjectDao(database: AnekonDatabase): ProjectDao {
        return database.projectDao()
    }

    @Provides
    fun provideBuildDao(database: AnekonDatabase): BuildDao {
        return database.buildDao()
    }

    @Provides
    fun provideChatMessageDao(database: AnekonDatabase): ChatMessageDao {
        return database.chatMessageDao()
    }

    @Provides
    fun provideGeneratedAppDao(database: AnekonDatabase): GeneratedAppDao {
        return database.generatedAppDao()
    }

    @Provides
    fun provideGitHubAccountDao(database: AnekonDatabase): GitHubAccountDao {
        return database.githubAccountDao()
    }

    @Provides
    fun provideAISettingsDao(database: AnekonDatabase): AISettingsDao {
        return database.aiSettingsDao()
    }

    @Provides
    @Singleton
    fun provideGitHubRepository(
        apiService: GitHubApiService,
        projectDao: ProjectDao,
        buildDao: BuildDao
    ): GitHubRepository {
        return GitHubRepository(apiService, projectDao, buildDao)
    }

    @Provides
    @Singleton
    fun provideAIRepository(
        aiSettingsDao: AISettingsDao,
        chatMessageDao: ChatMessageDao,
        generatedAppDao: GeneratedAppDao
    ): AIRepository {
        return AIRepository(aiSettingsDao, chatMessageDao, generatedAppDao)
    }

    // ============ USE CASES ============

    @Provides
    fun provideAnalyzeBuildErrorUseCase(
        gitHubRepository: GitHubRepository,
        aiRepository: AIRepository
    ): AnalyzeBuildErrorUseCase {
        return AnalyzeBuildErrorUseCase(gitHubRepository, aiRepository)
    }

    @Provides
    fun provideApplyFixUseCase(
        gitHubRepository: GitHubRepository,
        aiRepository: AIRepository
    ): ApplyFixUseCase {
        return ApplyFixUseCase(gitHubRepository, aiRepository)
    }

    @Provides
    fun provideGenerateAppUseCase(
        aiRepository: AIRepository
    ): GenerateAppUseCase {
        return GenerateAppUseCase(aiRepository)
    }

    @Provides
    fun provideChatWithAIUseCase(
        aiRepository: AIRepository
    ): ChatWithAIUseCase {
        return ChatWithAIUseCase(aiRepository)
    }

    @Provides
    fun provideGetProjectsUseCase(
        gitHubRepository: GitHubRepository
    ): GetProjectsUseCase {
        return GetProjectsUseCase(gitHubRepository)
    }

    @Provides
    fun provideExportAppUseCase(
        aiRepository: AIRepository
    ): ExportAppUseCase {
        return ExportAppUseCase(aiRepository)
    }

    @Provides
    fun provideReviewCodeUseCase(
        aiRepository: AIRepository
    ): ReviewCodeUseCase {
        return ReviewCodeUseCase(aiRepository)
    }
}