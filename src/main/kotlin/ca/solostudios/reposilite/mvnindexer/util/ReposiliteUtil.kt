package ca.solostudios.reposilite.mvnindexer.util

import com.reposilite.shared.extensions.acceptsBody
import com.reposilite.shared.extensions.contentDisposition
import com.reposilite.shared.extensions.contentLength
import com.reposilite.shared.extensions.lastModified
import com.reposilite.shared.extensions.silentClose
import com.reposilite.storage.api.DocumentInfo
import io.javalin.http.Context
import io.javalin.http.Header.CACHE_CONTROL
import io.javalin.http.Header.CONTENT_SECURITY_POLICY
import java.io.InputStream
import java.net.URLEncoder
import kotlin.time.Duration.Companion.hours

private val MAX_CACHE_AGE = 1.hours.inWholeSeconds

internal fun Context.resultAttachment(
    info: DocumentInfo,
    compressionStrategy: String,
    data: InputStream
) {
    header(CONTENT_SECURITY_POLICY, "sandbox")

    if (!info.contentType.isHumanReadable)
        contentDisposition("""attachment; filename="${info.name}"; filename*=utf-8''${URLEncoder.encode(info.name, "utf-8")}""")

    if (compressionStrategy == "none" && info.contentLength > 0)
        contentLength(info.contentLength) // Using this with GZIP ends up with "Premature end of Content-Length delimited message body".

    if (info.lastModifiedTime != null)
        lastModified(info.lastModifiedTime!!)

    header(CACHE_CONTROL, "public, max-age=$MAX_CACHE_AGE")

    when {
        acceptsBody() -> result(data)
        else          -> data.silentClose()
    }

    contentType(info.contentType)
}
