package ca.solostudios.reposilite.mvnindexer.api

import com.reposilite.storage.api.Location

public data class MavenIndexerSearchResponse(
    val results: List<Location>,
)
