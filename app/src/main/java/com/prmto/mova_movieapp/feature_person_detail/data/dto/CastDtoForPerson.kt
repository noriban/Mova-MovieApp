package com.prmto.mova_movieapp.feature_person_detail.data.dto

import com.prmto.mova_movieapp.feature_person_detail.domain.model.CastForPerson
import com.squareup.moshi.Json

data class CastDtoForPerson(
    val adult: Boolean,
    @Json(name = "backdrop_path") val backdropPath: String?,
    val character: String,
    @Json(name = "credit_id") val creditId: String,
    @Json(name = "episode_count") val episodeCount: Int?,
    @Json(name = "first_air_date") val firstAirDate: String?,
    @Json(name = "genre_ids") val genreIds: List<Int>,
    val id: Int,
    @Json(name = "media_type") val mediaType: String,
    val name: String?,
    val order: Int?,
    @Json(name = "origin_country") val originCountry: List<String>?,
    @Json(name = "original_language") val originalLanguage: String,
    @Json(name = "original_name") val originalName: String?,
    @Json(name = "original_title") val originalTitle: String?,
    val overview: String,
    val popularity: Double,
    @Json(name = "poster_path") val posterPath: String?,
    @Json(name = "release_date") val releaseDate: String?,
    val title: String?,
    val video: Boolean?,
    @Json(name = "vote_average") val voteAverage: Double,
    @Json(name = "vote_count") val voteCount: Int
)

fun CastDtoForPerson.toCastForPerson(): CastForPerson {
    return CastForPerson(
            id = id,
            name = name,
            originalName = originalName,
            originalTitle = originalTitle,
            character = character,
            firstAirDate = firstAirDate,
            mediaType = mediaType,
            overview = overview,
            posterPath = posterPath,
            popularity = popularity,
            title = title,
            releaseDate = releaseDate,
            voteAverage = voteAverage,
            voteCount = voteCount
    )
}