package de.sfuhrm.capsula;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/** Locator for targets based on a file system path.
 * */
public final class PathTargetLocator implements TargetLocator {

    /** Path to targets that are available. */
    private final Path targets;

    /** Creates a locator that extracts its targets from
     * the given path.
     * @param myTargets path to extract the targets from.
     * */
    public PathTargetLocator(final Path myTargets) {
        this.targets = Objects.requireNonNull(myTargets);
    }

    @Override
    public Path extractTargetToTmp(final Path tempParent,
                                   final String target) throws IOException {
        Path source = targets.resolve(target);
        Path tempTarget = tempParent.resolve(target);

        FileUtils.copyRecursive(source, tempParent, p -> { });

        return tempTarget;
    }

    @Override
    public Set<String> getTargets() throws IOException {
        return Files.list(targets)
                .filter(p -> Files.isDirectory(p))
                .map(p -> p.getFileName().toString())
                .collect(Collectors.toSet());
    }
}
