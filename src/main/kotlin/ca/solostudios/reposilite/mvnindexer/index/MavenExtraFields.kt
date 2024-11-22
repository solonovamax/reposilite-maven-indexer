package ca.solostudios.reposilite.mvnindexer.index

import org.apache.maven.index.Field

public object MavenExtraFields {
    private const val MAVEN_EXTRA_NAMESPACE = "urn:maven-extra#"

    public val GAV: Field = Field(null, MAVEN_EXTRA_NAMESPACE, "gav", "GAV version identifier")

    public val URL: Field = Field(null, MAVEN_EXTRA_NAMESPACE, "url", "Project url")

    public val LICENSE: Field = Field(null, MAVEN_EXTRA_NAMESPACE, "license", "License SPDX identifier")

    public val ORGANIZATION: Field = Field(null, MAVEN_EXTRA_NAMESPACE, "organization", "Organization name")

    public val MD5: Field = Field(null, MAVEN_EXTRA_NAMESPACE, "md5", "MD5 checksum")

    public val SHA256: Field = Field(null, MAVEN_EXTRA_NAMESPACE, "sha256", "SHA-256 checksum")

    public val SHA512: Field = Field(null, MAVEN_EXTRA_NAMESPACE, "sha512", "SHA-512 checksum")
}
