package ca.solostudios.reposilite.mvnindexer.infrastructure

import ca.solostudios.reposilite.mvnindexer.MavenIndexerComponents
import ca.solostudios.reposilite.mvnindexer.MavenIndexerSettings
import ca.solostudios.reposilite.mvnindexer.api.MavenIndexerSearchRequest
import ca.solostudios.reposilite.mvnindexer.api.MavenIndexerSearchResponse
import com.reposilite.journalist.Journalist
import com.reposilite.journalist.Logger
import com.reposilite.maven.MavenFacade
import com.reposilite.maven.Repository
import com.reposilite.shared.ErrorResponse
import com.reposilite.shared.badRequestError
import com.reposilite.status.FailureFacade
import com.reposilite.storage.StorageFacade
import com.reposilite.storage.filesystem.FileSystemStorageProvider
import org.apache.maven.index.Indexer
import panda.std.Result
import panda.std.reactive.Reference
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit
import kotlin.time.DurationUnit.MILLISECONDS


internal class MavenIndexerService(
    private val indexer: Indexer,
    private val journalist: Journalist,
    private val mavenFacade: MavenFacade,
    private val failureFacade: FailureFacade,
    private val storageFacade: StorageFacade,
    private val mavenIndexerSettings: Reference<MavenIndexerSettings>,
    private val components: MavenIndexerComponents,
    private val scheduler: ScheduledThreadPoolExecutor
                                  ) : Journalist {
    
    private var indexerTask: ScheduledFuture<*>? = null
    
    init {
        mavenIndexerSettings.subscribeDetailed(::settingsUpdate, true)
    }
    
    private fun settingsUpdate(oldSettings: MavenIndexerSettings, newSettings: MavenIndexerSettings) {
        indexerTask?.cancel(false)
        scheduler.purge()
        
        val durationMillis = newSettings.mavenIndexInterval.duration.toLong(MILLISECONDS)
        indexerTask = scheduler.scheduleAtFixedRate(::scheduledIndex, durationMillis, durationMillis, TimeUnit.MILLISECONDS)
    }
    
    private fun scheduledIndex() {
        TODO("Process scheduled index")
    }
    
    fun incrementalIndex(repository: Repository): Result<Unit, ErrorResponse> {
        if (repository.storageProvider !is FileSystemStorageProvider)
            return badRequestError("Repository must be located on the file system")
        
        TODO("Incremental index")
    }
    
    fun rebuildIndex(repository: Repository): Result<Unit, ErrorResponse> {
        if (repository.storageProvider !is FileSystemStorageProvider)
            return badRequestError("Repository must be located on the file system")
        
        TODO("Rebuild index")
    }
    
    private fun legacyWarning(settings: MavenIndexerSettings) {
        if (!settings.legacy)
            return
        
        logger.warn("!!! Warning !!!")
        logger.warn("Using legacy .zip maven index format")
        logger.warn("It is recommended to not use the legacy index format.")
        
    }
    
    fun search(searchRequest: MavenIndexerSearchRequest): Result<MavenIndexerSearchResponse, ErrorResponse> {
        TODO("Finish search impl")
    }
    
    fun contains(searchRequest: MavenIndexerSearchRequest): Result<Unit, ErrorResponse> {
        TODO("Finish search impl")
    }
    
    override fun getLogger(): Logger = journalist.logger
}
