package ca.solostudios.reposilite.mvnindexer.index.creator

import ca.solostudios.reposilite.mvnindexer.index.MavenExtraFields
import ca.solostudios.reposilite.mvnindexer.index.locator.Md5Locator
import ca.solostudios.reposilite.mvnindexer.index.locator.Sha256Locator
import ca.solostudios.reposilite.mvnindexer.index.locator.Sha512Locator
import ca.solostudios.reposilite.mvnindexer.util.formattedString
import ca.solostudios.reposilite.mvnindexer.util.get
import ca.solostudios.reposilite.mvnindexer.util.license
import ca.solostudios.reposilite.mvnindexer.util.md5
import ca.solostudios.reposilite.mvnindexer.util.organization
import ca.solostudios.reposilite.mvnindexer.util.sha256
import ca.solostudios.reposilite.mvnindexer.util.sha512
import ca.solostudios.reposilite.mvnindexer.util.url
import ca.solostudios.reposilite.mvnindexer.util.versionNotation
import org.apache.lucene.document.Document
import org.apache.maven.index.ArtifactContext
import org.apache.maven.index.ArtifactInfo
import org.apache.maven.index.IndexerField
import org.apache.maven.index.IndexerFieldVersion
import org.apache.maven.index.creator.AbstractIndexCreator
import org.apache.maven.index.creator.JarFileContentsIndexCreator
import org.apache.maven.index.creator.MinimalArtifactInfoIndexCreator
import java.io.IOException
import kotlin.io.path.exists
import kotlin.io.path.isReadable
import kotlin.io.path.isRegularFile
import kotlin.io.path.readText

public class MavenExtraArtifactInfoIndexCreator : AbstractIndexCreator(ID, listOf(MinimalArtifactInfoIndexCreator.ID)) {
    override fun populateArtifactInfo(context: ArtifactContext) {
        val artifact = context.artifact?.toPath() ?: return
        val info = context.artifactInfo

        if (!artifact.isRegularFile())
            return

        val realMd5 = artifact.md5().hexLower
        val md5File = Md5Locator.locate(artifact)
        if (md5File.exists() && md5File.isRegularFile() && md5File.isReadable()) {
            try {
                val md5 = md5File.readText().trim().lowercase()
                if (realMd5 == md5) // md5 is invalid, do not index by md5
                    info.md5 = md5
            } catch (e: IOException) {
                context.addError(e)
            }
        } else {
            info.md5 = realMd5
        }

        val realSha256 = artifact.sha256().hexLower
        val sha256File = Sha256Locator.locate(artifact)
        if (sha256File.exists() && sha256File.isRegularFile() && sha256File.isReadable()) {
            try {
                val sha256 = sha256File.readText().trim().lowercase()
                if (realSha256 == sha256) // sha256 is invalid, do not index by sha256
                    info.sha256 = sha256
            } catch (e: IOException) {
                context.addError(e)
            }
        } else {
            info.sha256 = realSha256
        }

        val realSha512 = artifact.sha512().hexLower
        val sha512File = Sha512Locator.locate(artifact)
        if (sha512File.exists() && sha512File.isRegularFile() && sha512File.isReadable()) {
            try {
                val sha512 = sha512File.readText().trim().lowercase()
                if (sha512 == realSha512) // sha256 is invalid, do not index by sha256
                    info.sha512 = sha512
            } catch (e: IOException) {
                context.addError(e)
            }
        } else {
            info.sha512 = realSha512
        }

        val pom = context.pomModel ?: return

        if (pom.licenses.isNotEmpty())
            info.license = pom.licenses.joinToString { it.formattedString() }

        if (pom.organization != null)
            info.organization = pom.organization.formattedString()

        if (pom.url != null)
            info.url = pom.url
    }

    override fun updateDocument(info: ArtifactInfo, document: Document) {
        val gavString = info.versionNotation
        document.add(GAV.toField(gavString))
        document.add(GAV_KEYWORD.toField(gavString))

        val md5 = info.md5
        if (md5 != null)
            document.add(MD5.toField(md5))

        val sha256 = info.sha256
        if (sha256 != null)
            document.add(SHA256.toField(sha256))

        val sha512 = info.sha512
        if (sha512 != null)
            document.add(SHA512.toField(sha512))

        val license = info.license
        if (license != null) {
            document.add(LICENSE.toField(license))
            document.add(LICENSE_KEYWORD.toField(license))
        }

        val organization = info.organization
        if (organization != null)
            document.add(ORGANIZATION.toField(organization))

        val url = info.url
        if (url != null) {
            document.add(URL.toField(url))
            document.add(URL_KEYWORD.toField(url))
        }
    }

    override fun updateArtifactInfo(document: Document, info: ArtifactInfo): Boolean {
        var updated = false

        val md5 = document[MD5]
        if (md5 != null) {
            info.md5 = md5
            updated = true
        }

        val sha256 = document[SHA256]
        if (sha256 != null) {
            info.sha256 = sha256
            updated = true
        }

        val sha512 = document[SHA512]
        if (sha512 != null) {
            info.sha512 = sha512
            updated = true
        }

        val license = document[LICENSE]
        if (license != null) {
            info.license = license
            updated = true
        }

        val organization = document[ORGANIZATION]
        if (organization != null) {
            info.organization = organization
            updated = true
        }

        val url = document[URL]
        if (url != null) {
            info.url = url
            updated = true
        }

        return updated
    }

    override fun toString(): String {
        return JarFileContentsIndexCreator.ID
    }

    override fun getIndexerFields(): Collection<IndexerField> {
        return listOf(
            GAV,
            GAV_KEYWORD,
            URL,
            URL_KEYWORD,
            LICENSE,
            LICENSE_KEYWORD,
            ORGANIZATION,
            MD5,
            SHA256,
            SHA512,
        )
    }

    public companion object {
        public const val ID: String = "maven-extra"

        public val GAV: IndexerField = IndexerField(
            MavenExtraFields.GAV,
            IndexerFieldVersion.V4,
            "gav",
            "GAV version identifier (tokenized, not stored)",
            IndexerField.ANALYZED_NOT_STORED
        )

        public val GAV_KEYWORD: IndexerField = IndexerField(
            MavenExtraFields.GAV,
            IndexerFieldVersion.V4,
            "gav-keyword",
            "GAV version identifier (as keyword, not stored)",
            IndexerField.KEYWORD_NOT_STORED
        )

        public val URL: IndexerField = IndexerField(
            MavenExtraFields.URL,
            IndexerFieldVersion.V4,
            "url",
            "GAV version identifier (tokenized, stored)",
            IndexerField.ANALYZED_STORED
        )

        public val URL_KEYWORD: IndexerField = IndexerField(
            MavenExtraFields.URL,
            IndexerFieldVersion.V4,
            "url-keyword",
            "GAV version identifier (as keyword, not stored)",
            IndexerField.KEYWORD_NOT_STORED
        )

        public val LICENSE: IndexerField = IndexerField(
            MavenExtraFields.LICENSE,
            IndexerFieldVersion.V4,
            "license",
            "SPDX license identifier (as keyword, stored)",
            IndexerField.ANALYZED_STORED
        )

        public val LICENSE_KEYWORD: IndexerField = IndexerField(
            MavenExtraFields.LICENSE,
            IndexerFieldVersion.V4,
            "license-keyword",
            "GAV version identifier (as keyword, not stored)",
            IndexerField.KEYWORD_NOT_STORED
        )

        public val ORGANIZATION: IndexerField = IndexerField(
            MavenExtraFields.ORGANIZATION,
            IndexerFieldVersion.V4,
            "organization",
            "Organization name (as keyword, stored)",
            IndexerField.KEYWORD_STORED
        )

        public val MD5: IndexerField = IndexerField(
            MavenExtraFields.MD5,
            IndexerFieldVersion.V4,
            "md5",
            "Artifact MD5 checksum (as keyword, stored)",
            IndexerField.KEYWORD_STORED
        )

        public val SHA256: IndexerField = IndexerField(
            MavenExtraFields.SHA256,
            IndexerFieldVersion.V4,
            "sha256",
            "Artifact SHA256 checksum (as keyword, stored)",
            IndexerField.KEYWORD_STORED
        )

        public val SHA512: IndexerField = IndexerField(
            MavenExtraFields.SHA512,
            IndexerFieldVersion.V4,
            "sha512",
            "Artifact SHA512 checksum (as keyword, stored)",
            IndexerField.KEYWORD_STORED
        )
    }
}
