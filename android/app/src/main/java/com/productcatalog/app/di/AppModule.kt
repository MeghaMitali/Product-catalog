package com.productcatalog.app.di

import android.content.Context
import androidx.room.Room
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.productcatalog.app.BuildConfig
import com.productcatalog.app.data.local.ProductDao
import com.productcatalog.app.data.local.ProductDatabase
import com.productcatalog.app.data.remote.EnquiryApi
import com.productcatalog.app.data.remote.ProductApi
import com.productcatalog.app.domain.repository.EnquiryRepository
import com.productcatalog.app.domain.repository.ProductRepository
import com.productcatalog.app.domain.repository.impl.EnquiryRepositoryImpl
import com.productcatalog.app.domain.repository.impl.ProductRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): ProductDatabase =
        Room.databaseBuilder(
            context,
            ProductDatabase::class.java,
            "product_catalog.db"
        ).fallbackToDestructiveMigration().build()

    @Provides
    @Singleton
    fun provideProductDao(database: ProductDatabase): ProductDao = database.productDao()

    @Provides
    @Singleton
    fun provideRetrofit(json: Json): Retrofit {
        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(
                okhttp3.OkHttpClient.Builder()
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .readTimeout(15, TimeUnit.SECONDS)
                    .writeTimeout(15, TimeUnit.SECONDS)
                    .build()
            )
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }

    @Provides
    @Singleton
    fun provideProductApi(retrofit: Retrofit): ProductApi = retrofit.create(ProductApi::class.java)

    @Provides
    @Singleton
    fun provideEnquiryApi(retrofit: Retrofit): EnquiryApi = retrofit.create(EnquiryApi::class.java)

    @Provides
    @Singleton
    fun provideProductRepository(api: ProductApi, dao: ProductDao): ProductRepository =
        ProductRepositoryImpl(api, dao)

    @Provides
    @Singleton
    fun provideEnquiryRepository(api: EnquiryApi): EnquiryRepository = EnquiryRepositoryImpl(api)
}
