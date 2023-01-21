package ca.solostudios.reposilite.mvnindexer

import ca.solostudios.reposilite.mvnindexer.infrastructure.MavenIndexerService
import com.reposilite.journalist.Journalist
import com.reposilite.journalist.Logger
import com.reposilite.maven.MavenFacade
import com.reposilite.maven.Repository
import com.reposilite.plugin.api.Facade

public class MavenIndexerFacade internal constructor(
    public val journalist: Journalist,
    public val mavenFacade: MavenFacade,
    private val mavenIndexerService: MavenIndexerService,
                                                    ) : Journalist, Facade {

    public fun indexRepository(repository: Repository) {
        // TODO
    }

    override fun getLogger(): Logger =
            journalist.logger
}
