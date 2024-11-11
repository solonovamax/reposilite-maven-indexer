package ca.solostudios.reposilite.mvnindexer

import ca.solostudios.kspservice.annotation.Service
import ca.solostudios.reposilite.mvnindexer.infrastructure.MavenIndexerApiEndpoints
import ca.solostudios.reposilite.mvnindexer.infrastructure.MavenIndexerEndpoints
import ca.solostudios.reposilite.mvnindexer.infrastructure.MavenIndexerSearchEndpoints
import com.reposilite.configuration.shared.SharedConfigurationFacade
import com.reposilite.plugin.api.Facade
import com.reposilite.plugin.api.Plugin
import com.reposilite.plugin.api.ReposiliteDisposeEvent
import com.reposilite.plugin.api.ReposilitePlugin
import com.reposilite.plugin.event
import com.reposilite.plugin.facade
import com.reposilite.plugin.reposilite
import com.reposilite.web.api.RoutingSetupEvent

@Plugin(
    name = "maven-indexer",
    dependencies = [
        "maven",
        "failure",
        "local-configuration",
        "shared-configuration",
        "access-token",
        "storage"
    ],
    version = "0.0.0",
    settings = MavenIndexerSettings::class,
)
@Service(ReposilitePlugin::class)
public class MavenIndexerPlugin : ReposilitePlugin() {
    override fun initialize(): Facade {
        logger.info("")
        logger.info("--- Maven Indexer Plugin")
        logger.info("Maven indexer plugin loaded")

        val mavenIndexerFacade = MavenIndexerComponents(
            reposilite = reposilite(),
            journalist = this,
            failureFacade = facade(),
            storageFacade = facade(),
            mavenFacade = facade(),
            mavenIndexerSettings = facade<SharedConfigurationFacade>().getDomainSettings()
        ).mavenIndexerFacade()


        event { event: RoutingSetupEvent ->
            event.registerRoutes(MavenIndexerEndpoints(mavenIndexerFacade))
            event.registerRoutes(MavenIndexerApiEndpoints(mavenIndexerFacade))
            event.registerRoutes(MavenIndexerSearchEndpoints(mavenIndexerFacade))
        }

        event<ReposiliteDisposeEvent> {
            mavenIndexerFacade.shutdown()
        }

        return mavenIndexerFacade
    }
}
