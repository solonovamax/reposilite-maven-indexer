package ca.solostudios.reposilite.mvnindexer.util

import org.apache.maven.model.License
import org.apache.maven.model.Organization


internal fun License.formattedString(): String {
    return buildString {
        if (name != null)
            append(name)

        if (url != null) {
            appendAdditional(url)
        }
    }
}

internal fun Organization.formattedString(): String {
    return buildString {
        if (name != null)
            append(name)

        if (url != null)
            appendAdditional(url)
    }
}

private fun StringBuilder.appendAdditional(string: String) {
    if (isEmpty()) {
        append(string)
    } else {
        append('(')
        append(string)
        append(')')
    }
}
