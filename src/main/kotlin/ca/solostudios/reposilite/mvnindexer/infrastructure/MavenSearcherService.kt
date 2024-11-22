package ca.solostudios.reposilite.mvnindexer.infrastructure

import ca.solostudios.reposilite.mvnindexer.MavenIndexerComponents
import com.reposilite.journalist.Journalist
import com.reposilite.maven.MavenFacade
import com.reposilite.plugin.Extensions
import com.reposilite.status.FailureFacade
import com.reposilite.storage.StorageFacade
import org.apache.maven.index.Indexer

internal class MavenSearcherService(
    private val indexer: Indexer,
    private val journalist: Journalist,
    private val mavenFacade: MavenFacade,
    private val failureFacade: FailureFacade,
    private val storageFacade: StorageFacade,
    private val extensions: Extensions,
    private val components: MavenIndexerComponents,
) {
    private fun buildQuery(query: String) {

    }
}
