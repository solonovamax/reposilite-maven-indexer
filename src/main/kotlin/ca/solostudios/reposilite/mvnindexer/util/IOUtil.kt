package ca.solostudios.reposilite.mvnindexer.util

import korlibs.crypto.MD5
import korlibs.crypto.SHA1
import korlibs.crypto.SHA256
import korlibs.crypto.SHA512
import korlibs.io.hash.hash
import java.io.InputStream
import java.nio.file.Path
import kotlin.io.path.inputStream

internal fun InputStream.md5() = hash(MD5)
internal fun InputStream.sha1() = hash(SHA1)
internal fun InputStream.sha256() = hash(SHA256)
internal fun InputStream.sha512() = hash(SHA512)

internal fun Path.md5() = inputStream().md5()
internal fun Path.sha1() = inputStream().sha1()
internal fun Path.sha256() = inputStream().sha256()
internal fun Path.sha512() = inputStream().sha512()
