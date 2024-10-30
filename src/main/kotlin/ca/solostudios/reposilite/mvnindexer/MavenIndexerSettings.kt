package ca.solostudios.reposilite.mvnindexer

import ca.solostudios.reposilite.mvnindexer.MavenIndexerSettings.MavenIndexInterval.DAILY
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
@Doc(title = "Maven Indexer", description = "Maven Indexer module configuration.")
public data class MavenIndexerSettings(
    @get:Doc(title = "Searchable", description = "Enable searching via Maven Indexer.")
    var searchable: Boolean = false,
    @get:Doc(title = "Index Path", description = "The path for the maven index. (optional, by default it's './.maven-index/')")
    val indexPath: String? = null,
    @get:Doc(title = "Incremental Chunks", description = "Create incremental index chunks.")
    val incrementalChunks: Boolean = true,
    @get:Doc(title = "Incremental Chunks Count", description = "The number of incremental chunks to keep. (default is 32)")
    @Min(min = 0)
    val incrementalChunksCount: Int = 32,
    @get:Doc(title = "Create Checksum Files", description = "Create checksums for all files (sha1, md5, etc.)")
    val createChecksumFiles: Boolean = false,
    @get:Doc(title = "Legacy Index Format", description = "Build legacy .zip index file.")
    val legacy: Boolean = false,
    @get:Doc(
        title = "Indexers",
        description = """
                The indexers used to index the maven repository. Defaults to 'default'.
                Comma separated list of indexers. Options: 'jarContent', 'maven-archetype', 'maven-plugin', 'min', and 'osgi-metadatas'.
                Shortcuts: 'full' (All indexers), 'default' ('min' and 'jarContent')
            """,
    )
    val indexers: String = "default",
    @get:Doc(
        title = "Indexing interval",
        description = """
                How often Reposilite should re-index the maven repository.
                With smaller durations the index is updated sooner, but it'll increase drastically increase server load.
                For smaller instances, this should ideally be kept low, but on more powerful servers it can be increased appropriately.
            """
    )
    val mavenIndexInterval: MavenIndexInterval = DAILY,
) : SharedSettings {
    public enum class MavenIndexer {
        JAR_CONTENT,
        MAVEN_ARCHETYPE,
        MAVEN_PLUGIN,
        MIN,
        OSGI_METADATAS,
        FULL,
        DEFAULT,
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
        private const val serialVersionUID: Long = 5464972743493076525L
    }
}
