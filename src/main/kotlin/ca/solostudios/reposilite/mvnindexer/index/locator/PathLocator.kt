package ca.solostudios.reposilite.mvnindexer.index.locator

import java.nio.file.Path

public interface PathLocator {
    public fun locate(source: Path): Path?
}
