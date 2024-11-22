package ca.solostudios.reposilite.mvnindexer.index.locator

import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString

internal object Sha512Locator : PathLocator {
    override fun locate(source: Path) = Path(source.absolutePathString() + ".sha512")
}
