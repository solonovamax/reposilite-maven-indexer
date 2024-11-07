package ca.solostudios.reposilite.mvnindexer.infrastructure

import com.reposilite.maven.MavenFacade
import com.reposilite.maven.Repository
import com.reposilite.maven.infrastructure.MavenRoutes
import com.reposilite.shared.ContextDsl
import com.reposilite.storage.api.Location
import com.reposilite.storage.api.toLocation

internal abstract class MavenIndexerRoutes(mavenFacade: MavenFacade) : MavenRoutes(mavenFacade) {
    fun <R> ContextDsl<R>.optionalRepository(block: (Repository?) -> Unit) {
        val repository = parameter("repository")

        block(repository?.let { mavenFacade.getRepository(it) })
    }

    fun <R> ContextDsl<R>.optionalGav(block: (Location) -> Unit) {
        val gav = parameter("gav")

        block(gav.toLocation())
    }
}
