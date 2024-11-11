package ca.solostudios.reposilite.mvnindexer

import com.reposilite.configuration.shared.api.Doc
import com.reposilite.configuration.shared.api.Min
import com.reposilite.configuration.shared.api.SharedSettings
import io.javalin.openapi.JsonSchema
import java.io.Serial
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

@JsonSchema(requireNonNulls = false)
@Doc(
    title = "Maven Indexer",
    description = """
        Maven Indexer module configuration.
    """
)
public data class MavenIndexerSettings(
    @get:Doc(
        title = "Enabled",
        description = """
            If building the maven indexes is enabled. Defaults to false.
        """
    )
    val enabled: Boolean = false,
    @get:Doc(
        title = "Searchable",
        description = """
            Enable searching via Maven Indexer. Defaults to false.
        """
    )
    val searchable: Boolean = false,
    @get:Doc(
        title = "Index Path",
        description = """
            The path for the maven index. Defaults to './.maven-index/'.
        """
    )
    val indexPath: String = ".maven-index/",
    @get:Doc(
        title = "Incremental Chunks",
        description = """
            Create incremental index chunks. Defaults to true.
        """
    )
    val incrementalChunks: Boolean = true,
    @Min(min = 1)
    @get:Doc(
        title = "Incremental Chunks Count",
        description = """
            The number of incremental chunks to keep. Defaults to 32.
        """
    )
    val incrementalChunksCount: Int = 32,
    @get:Doc(
        title = "Create Checksum Files",
        description = """
            Create checksums for all files (sha1, md5, etc.). Defaults to true.
        """
    )
    val createChecksumFiles: Boolean = false,
    @get:Doc(
        title = "Indexers",
        description = """
            A list of indexers used to index the maven repository. Defaults to the minimal and jar indexers.<br><br>

            The available indexers are:
            <ul>
                <li>JAR_CONTENT (Indexes class names)</li>
                <li>MAVEN_ARCHETYPE (Indexes maven archetypes)</li>
                <li>MAVEN_PLUGIN (Indexes maven plugins)</li>
                <li>MINIMAL (Indexes group id, artifact id, version, packaging type, classifier, name, description, last modified, sha1 hash)</li>
                <li>OSGI_METADATAS (Indexes OSGi metadata)</li>
                <li>FULL (All indexers)</li>
            </ul>
        """,
    )
    val indexers: List<MavenIndexer> = listOf(MavenIndexer.MINIMAL, MavenIndexer.JAR_CONTENT),
    @get:Doc(
        title = "Full Indexing Scan Interval",
        description = """
            How often Reposilite should attempt a full scan to re-index the maven repository.<br>
            With smaller durations the index is updated sooner, but it can significantly increase server load.<br>
            For smaller instances, this should ideally be kept low, but on more powerful servers it can be increased appropriately.<br>
            Defaults to daily.
        """
    )
    val mavenIndexFullScanInterval: MavenIndexInterval = MavenIndexInterval.DAILY,
    @get:Doc(
        title = "Continuous Index Updates",
        description = """
            Continuously updates the index, as new artifacts are uploaded.<br>
            The full scan will still run in the background.<br>
            However, the artifacts indexed by this will not need to be re-indexed.<br>
            Defaults to false.
        """
    )
    val continuousIndexUpdates: Boolean = false, // TODO: 2024-11-06 Implement this
    @Min(min = 1)
    @get:Doc(
        title = "Max Parallel Indexing Repositories",
        // language=HTML
        description = """
            Maximum number of repositories that can be indexed in parallel.<br>
            This setting only takes effect after a restart.<br>
            Defaults to 1.
        """
    )
    val maxParallelIndexRepositories: Int = 1,
    @get:Doc(
        title = "Development",
        description = """
            Enables development.<br>
            This setting is purely for testing and will be removed in the future.
        """
    )
    val development: Boolean = false, // TODO: 2024-11-05 Remove
) : SharedSettings {
    public enum class MavenIndexer {
        JAR_CONTENT,
        MAVEN_ARCHETYPE,
        MAVEN_PLUGIN,
        MINIMAL,
        OSGI_METADATA,
        FULL,
    }

    public enum class MavenIndexInterval(public val duration: Duration) {
        TWICE_HOURLY(30.minutes),
        HOURLY(1.hours),
        BI_HOURLY(2.hours),
        DAILY(1.days),
        WEEKLY(7.days),
        MONTHLY(30.days),
    }

    private companion object {
        @Serial
        private const val serialVersionUID: Long = -2129710622647039295L
    }
}
