package ca.solostudios.reposilite.mvnindexer.api

import com.reposilite.maven.Repository
import com.reposilite.token.AccessTokenIdentifier

public data class MavenIndexerSearchRequest(
    val accessToken: AccessTokenIdentifier?,
    val repository: Repository,
    val query: String,
                                           )
