package eu.kanade.tachiyomi.extension.all.mangavault

import eu.kanade.tachiyomi.source.Source
import eu.kanade.tachiyomi.source.SourceFactory

class MangaVaultFactory : SourceFactory {
    override fun createSources(): List<Source> {
        val firstMangaVault = MangaVault("")
        val mangaVaultCount = firstMangaVault.preferences
            .getString(MangaVault.PREF_EXTRA_SOURCES_COUNT, MangaVault.PREF_EXTRA_SOURCES_DEFAULT)!!
            .toInt()

        // MangaVault(""), MangaVault("2"), MangaVault("3"), ...
        return buildList(mangaVaultCount) {
            add(firstMangaVault)

            for (i in 0 until mangaVaultCount) {
                add(MangaVault("${i + 2}"))
            }
        }
    }
}
