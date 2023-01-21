package ca.solostudios.reposilite.mvnindexer

import ca.solostudios.reposilite.mvnindexer.infrastructure.MavenIndexerService
import com.reposilite.Reposilite
import com.reposilite.ReposiliteParameters
import com.reposilite.journalist.Journalist
import com.reposilite.maven.MavenFacade
import com.reposilite.maven.Repository
import com.reposilite.plugin.api.PluginComponents
import com.reposilite.shared.extensions.NamedThreadFactory
import com.reposilite.status.FailureFacade
import com.reposilite.storage.StorageFacade
import com.reposilite.storage.filesystem.FileSystemStorageProvider
import org.apache.maven.index.DefaultIndexer
import org.apache.maven.index.DefaultIndexerEngine
import org.apache.maven.index.DefaultQueryCreator
import org.apache.maven.index.DefaultSearchEngine
import org.apache.maven.index.Indexer
import org.apache.maven.index.IndexerEngine
import org.apache.maven.index.QueryCreator
import org.apache.maven.index.SearchEngine
import org.apache.maven.index.context.IndexCreator
import org.apache.maven.index.context.IndexingContext
import org.apache.maven.index.creator.JarFileContentsIndexCreator
import org.apache.maven.index.creator.MavenArchetypeArtifactInfoIndexCreator
import org.apache.maven.index.creator.MavenPluginArtifactInfoIndexCreator
import org.apache.maven.index.creator.MinimalArtifactInfoIndexCreator
import org.apache.maven.index.creator.OsgiArtifactIndexCreator
import panda.std.reactive.Reference
import java.util.concurrent.ScheduledThreadPoolExecutor
import kotlin.io.path.Path

internal class MavenIndexerComponents(
    private val reposilite: Reposilite,
    private val parameters: ReposiliteParameters,
    private val journalist: Journalist,
    private val failureFacade: FailureFacade,
    private val storageFacade: StorageFacade,
    private val mavenFacade: MavenFacade,
    private val mavenIndexerSettings: Reference<MavenIndexerSettings>
                                     ) : PluginComponents {
    private val indexPath = mavenIndexerSettings
            .computed { it.indexPath ?: ".maven-index/" }
            .computed { Path(it) }
    
    private val indexCreators = listOf(
            MinimalArtifactInfoIndexCreator(),
            JarFileContentsIndexCreator(),
            MavenArchetypeArtifactInfoIndexCreator(),
            MavenPluginArtifactInfoIndexCreator(),
            OsgiArtifactIndexCreator(),
                                      ).associateBy { it.id }
    
    private fun searchEngine(): SearchEngine {
        return DefaultSearchEngine()
    }
    
    private fun indexerEngine(): IndexerEngine {
        return DefaultIndexerEngine()
    }
    
    private fun queryCreator(): QueryCreator {
        return DefaultQueryCreator()
    }
    
    private fun mavenIndexer(): Indexer {
        return DefaultIndexer(
                searchEngine(),
                indexerEngine(),
                queryCreator(),
                             )
    }
    
    private fun Repository.indexPath() = indexPath.get().resolve(this.name)
    
    private fun indexCreators(indexersString: String): List<IndexCreator> {
        return when (indexersString) {
            // full includes all index creators
            "full"    -> return indexCreators.values.toList()
            // default includes 'min' and 'jarContent' creators
            "default" -> return listOf(MinimalArtifactInfoIndexCreator(), JarFileContentsIndexCreator())
            
            else      -> {
                indexersString.split(',').mapNotNull { indexCreators[it] }
            }
        }
    }
    
    internal fun indexingContext(
        repository: Repository,
        fsProvider: FileSystemStorageProvider,
        indexer: Indexer,
        settings: MavenIndexerSettings,
                                ): IndexingContext {
        return indexer.createIndexingContext(
                "${repository.name}-ctx",
                repository.name,
                fsProvider.rootDirectory.toFile(),
                repository.indexPath().toFile(),
                null,
                null,
                settings.searchable,
                false,
                indexCreators(settings.indexers),
                                            )
    }
    
    private fun scheduler(): ScheduledThreadPoolExecutor = ScheduledThreadPoolExecutor(1, NamedThreadFactory("Maven Indexer (1) - "))
    
    private fun mavenIndexerService(): MavenIndexerService =
            MavenIndexerService(
                    indexer = mavenIndexer(),
                    journalist = journalist,
                    mavenFacade = mavenFacade,
                    failureFacade = failureFacade,
                    storageFacade = storageFacade,
                    mavenIndexerSettings = mavenIndexerSettings,
                    components = this,
                    scheduler = scheduler(),
                               )
    
    fun mavenIndexerFacade(
        mavenIndexerService: MavenIndexerService = mavenIndexerService(),
                          ): MavenIndexerFacade =
            MavenIndexerFacade(
                    journalist = journalist,
                    mavenFacade = mavenFacade,
                    mavenIndexerService = mavenIndexerService,
                              )
}
