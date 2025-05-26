package com.pocketwriter.app.network

import com.pocketwriter.app.Article
import com.pocketwriter.app.Template
import com.pocketwriter.app.ArticleCreateRequest
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.DELETE
import retrofit2.http.Multipart
import retrofit2.http.Part
import okhttp3.MultipartBody
import retrofit2.Response

private const val BASE_URL = "http://10.0.2.2:8080/" // Use 10.0.2.2 for localhost in Android emulator

// Data class for upload response
data class UploadResponse(val url: String)

// Retrofit interface for API endpoints
interface ApiService {
    // Article CRUD
    @GET("api/articles")
    suspend fun getArticles(): List<Article>

    @GET("api/articles/{id}")
    suspend fun getArticleById(@Path("id") id: Long): Article

    @POST("api/articles")
    suspend fun createArticle(@Body article: ArticleCreateRequest)

    @PUT("api/articles/{id}")
    suspend fun updateArticle(@Path("id") id: Long, @Body article: ArticleCreateRequest)

    @DELETE("api/articles/{id}")
    suspend fun deleteArticle(@Path("id") id: Long): Response<Unit>

    // Template CRUD
    @GET("api/templates")
    suspend fun getTemplates(): List<Template>

    @GET("api/templates/{id}")
    suspend fun getTemplateById(@Path("id") id: Long): Template

    @POST("api/templates")
    suspend fun createTemplate(@Body template: Template): Template

    @PUT("api/templates/{id}")
    suspend fun updateTemplate(@Path("id") id: Long, @Body template: Template): Template

    @DELETE("api/templates/{id}")
    suspend fun deleteTemplate(@Path("id") id: Long): Response<Unit>

    // Image upload endpoint
    @Multipart
    @POST("api/upload")
    suspend fun uploadImage(
        @Part file: MultipartBody.Part
    ): UploadResponse
}

// Singleton Retrofit instance
object ApiClient {
    val retrofitService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
