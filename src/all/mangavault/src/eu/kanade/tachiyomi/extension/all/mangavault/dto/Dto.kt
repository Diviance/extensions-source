package eu.kanade.tachiyomi.extension.all.mangavault.dto

import eu.kanade.tachiyomi.source.model.SManga
import kotlinx.serialization.Serializable
import org.apache.commons.text.StringSubstitutor

interface ConvertibleToSManga {
    fun toSManga(baseUrl: String): SManga
}

@Serializable
class LibraryDto(
    val id: String,
    val name: String,
)

@Serializable
class MihonHealthDto(
    val name: String,
    val version: Int,
    val features: MihonFeaturesDto = MihonFeaturesDto(),
)

@Serializable
class MihonFeaturesDto(
    val directPageUrls: Boolean = false,
    val filterOptions: Boolean = false,
    val collections: Boolean = false,
    val readlists: Boolean = false,
    val conversion: Boolean = false,
)

@Serializable
class MihonFilterOptionsDto(
    val libraries: List<LibraryDto> = emptyList(),
    val collections: List<CollectionDto> = emptyList(),
    val genres: Set<String> = emptySet(),
    val tags: Set<String> = emptySet(),
    val publishers: Set<String> = emptySet(),
    val authors: List<AuthorDto> = emptyList(),
)

@Serializable
class SeriesDto(
    val id: String,
    val libraryId: String = "",
    val name: String,
    val created: String? = null,
    val lastModified: String? = null,
    val chapterCount: Int = 0,
    val pageCount: Int = 0,
    val coverUrl: String? = null,
    val metadata: SeriesMetadataDto = SeriesMetadataDto(),
    val chapterMetadata: ChapterMetadataAggregationDto = ChapterMetadataAggregationDto(),
) : ConvertibleToSManga {
    override fun toSManga(baseUrl: String) = SManga.create().apply {
        title = metadata.title.ifBlank { name }
        url = "$baseUrl/api/v1/mihon/series/$id"
        thumbnail_url = coverUrl ?: "$baseUrl/api/v1/mihon/series/$id/cover"
        status = when {
            metadata.status == "ENDED" && metadata.totalChapterCount != null && chapterCount < metadata.totalChapterCount -> SManga.PUBLISHING_FINISHED
            metadata.status == "ENDED" -> SManga.COMPLETED
            metadata.status == "ONGOING" -> SManga.ONGOING
            metadata.status == "ABANDONED" -> SManga.CANCELLED
            metadata.status == "HIATUS" -> SManga.ON_HIATUS
            else -> SManga.UNKNOWN
        }
        genre = (metadata.genres + metadata.tags + chapterMetadata.tags).sorted().distinct().joinToString(", ")
        description = metadata.summary.ifBlank { chapterMetadata.summary }
        chapterMetadata.authors.groupBy({ it.role }, { it.name }).let { map ->
            author = map["writer"]?.distinct()?.joinToString()
            artist = map["penciller"]?.distinct()?.joinToString()
        }
    }
}

@Serializable
class SeriesMetadataDto(
    val status: String = "",
    val title: String = "",
    val titleSort: String = "",
    val summary: String = "",
    val readingDirection: String = "",
    val publisher: String = "",
    val ageRating: String? = null,
    val language: String = "",
    val genres: Set<String> = emptySet(),
    val tags: Set<String> = emptySet(),
    val totalChapterCount: Int? = null,
)

@Serializable
class ChapterMetadataAggregationDto(
    val authors: List<AuthorDto> = emptyList(),
    val tags: Set<String> = emptySet(),
    val releaseDate: String? = null,
    val summary: String = "",
)

@Serializable
class MihonChapterDto(
    val id: String,
    val seriesId: String,
    val seriesTitle: String,
    val name: String,
    val chapterSuffix: String = "",
    val number: String = "",
    val numberSort: Float = 0F,
    val created: String? = null,
    val lastModified: String? = null,
    val fileLastModified: String? = null,
    val sizeBytes: Long = 0,
    val size: String = "",
    val coverUrl: String? = null,
    val media: MediaDto = MediaDto(),
    val metadata: ChapterMetadataDto = ChapterMetadataDto(),
) : ConvertibleToSManga {
    fun getChapterName(template: String, isFromReadList: Boolean): String {
        val values = hashMapOf(
            "title" to metadata.title.ifBlank { name },
            "seriesTitle" to seriesTitle,
            "suffix" to chapterSuffix,
            "number" to metadata.number,
            "createdDate" to created,
            "releaseDate" to metadata.releaseDate,
            "size" to size,
            "sizeBytes" to sizeBytes.toString(),
        )
        val sub = StringSubstitutor(values, "{", "}")

        return buildString {
            if (isFromReadList) {
                append(seriesTitle)
                append(" ")
            }

            append(sub.replace(template))
        }
    }

    override fun toSManga(baseUrl: String) = SManga.create().apply {
        title = metadata.title.ifBlank { name }
        url = "$baseUrl/api/v1/mihon/chapters/$id"
        thumbnail_url = coverUrl ?: "$baseUrl/api/v1/mihon/chapters/$id/cover"
        status = SManga.UNKNOWN
        genre = (metadata.genres + metadata.tags).distinct().joinToString(", ")
        description = metadata.summary
        author = metadata.authors.joinToString { it.name }
        artist = author
    }
}

@Serializable
class MediaDto(
    val status: String = "",
    val mediaType: String = "",
    val pagesCount: Int = 0,
    val mediaProfile: String = "DIVINA",
    val epubDivinaCompatible: Boolean = false,
)

@Serializable
class MihonPageDto(
    val number: Int,
    val fileName: String,
    val mediaType: String,
    val width: Int? = null,
    val height: Int? = null,
    val sizeBytes: Long? = null,
    val imageUrl: String? = null,
    val thumbnailUrl: String? = null,
)

@Serializable
class ChapterMetadataDto(
    val title: String = "",
    val summary: String = "",
    val number: String = "",
    val numberSort: Float = 0F,
    val releaseDate: String? = null,
    val authors: List<AuthorDto> = emptyList(),
    val genres: Set<String> = emptySet(),
    val tags: Set<String> = emptySet(),
)

@Serializable
class AuthorDto(
    val name: String,
    val role: String,
)

@Serializable
class CollectionDto(
    val id: String,
    val name: String,
    val ordered: Boolean = false,
    val seriesIds: List<String> = emptyList(),
    val createdDate: String = "",
    val lastModifiedDate: String = "",
    val filtered: Boolean = false,
)

@Serializable
class ReadListDto(
    val id: String,
    val name: String,
    val summary: String = "",
    val bookIds: List<String> = emptyList(),
    val createdDate: String = "",
    val lastModifiedDate: String = "",
    val filtered: Boolean = false,
) : ConvertibleToSManga {
    override fun toSManga(baseUrl: String) = SManga.create().apply {
        title = name
        description = summary
        url = "$baseUrl/api/v1/mihon/readlists/$id"
        status = SManga.UNKNOWN
    }
}
