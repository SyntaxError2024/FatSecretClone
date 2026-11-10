package com.example.data.remote

import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url

interface OpenFoodFactsApi {
    @GET
    suspend fun searchProductsWithUrl(
        @Url url: String,
        @Query("search_terms") searchTerms: String? = null,
        @Query("search_simple") searchSimple: Int = 1,
        @Query("action") action: String = "process",
        @Query("json") json: Int = 1,
        @Query("page_size") pageSize: Int = 40,
        @Query("tagtype_0") tagtype0: String? = null,
        @Query("tag_contains_0") tagContains0: String? = null,
        @Query("tag_0") tag0: String? = null,
        @Query("tagtype_1") tagtype1: String? = null,
        @Query("tag_contains_1") tagContains1: String? = null,
        @Query("tag_1") tag1: String? = null,
        @Query("tagtype_2") tagtype2: String? = null,
        @Query("tag_contains_2") tagContains2: String? = null,
        @Query("tag_2") tag2: String? = null,
        @Query("fields") fields: String = "code,product_name,product_name_fr,brands,stores,nutriments,serving_size,categories_tags"
    ): OffSearchResponse

    @GET("cgi/search.pl")
    suspend fun searchProducts(
        @Query("search_terms") searchTerms: String? = null,
        @Query("search_simple") searchSimple: Int = 1,
        @Query("action") action: String = "process",
        @Query("json") json: Int = 1,
        @Query("page_size") pageSize: Int = 40,
        @Query("tagtype_0") tagtype0: String? = null,
        @Query("tag_contains_0") tagContains0: String? = null,
        @Query("tag_0") tag0: String? = null,
        @Query("tagtype_1") tagtype1: String? = null,
        @Query("tag_contains_1") tagContains1: String? = null,
        @Query("tag_1") tag1: String? = null,
        @Query("tagtype_2") tagtype2: String? = null,
        @Query("tag_contains_2") tagContains2: String? = null,
        @Query("tag_2") tag2: String? = null,
        @Query("fields") fields: String = "code,product_name,product_name_fr,brands,stores,nutriments,serving_size,categories_tags"
    ): OffSearchResponse
}
