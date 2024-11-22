package ca.solostudios.reposilite.mvnindexer.util

import org.apache.lucene.document.Document
import org.apache.maven.index.ArtifactInfo
import org.apache.maven.index.IndexerField

internal val ArtifactInfo.versionNotation: String
    get() = buildString {
        if (groupId != null) {
            append(groupId)
        }

        if (name != null) {
            if (isNotEmpty())
                append(':')
            append(name)
        }
        if (version != null) {
            if (isNotEmpty())
                append(':')
            append(version)
        }

        if (classifier != null) {
            if (isNotEmpty())
                append(':')
            append(classifier)
        }
        if (fileExtension != null && fileExtension != "jar") {
            if (isNotEmpty())
                append('@')
            append(fileExtension)
        }
    }

private const val SHA512_ATTRIBUTE = "sha512"
private const val LICENSE_ATTRIBUTE = "license"
private const val ORGANIZATION_ATTRIBUTE = "organization"
private const val URL_ATTRIBUTE = "url"

internal var ArtifactInfo.sha512: String?
    get() = attributes[SHA512_ATTRIBUTE]
    set(value) {
        attributes[SHA512_ATTRIBUTE] = value
    }

internal var ArtifactInfo.license: String?
    get() = attributes[LICENSE_ATTRIBUTE]
    set(value) {
        attributes[LICENSE_ATTRIBUTE] = value
    }

internal var ArtifactInfo.organization: String?
    get() = attributes[ORGANIZATION_ATTRIBUTE]
    set(value) {
        attributes[ORGANIZATION_ATTRIBUTE] = value
    }


internal var ArtifactInfo.url: String?
    get() = attributes[URL_ATTRIBUTE]
    set(value) {
        attributes[URL_ATTRIBUTE] = value
    }

internal operator fun Document.get(field: IndexerField): String? = this[field.key]
