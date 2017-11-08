package de.sfuhrm.capsula.targetlocator;

import de.sfuhrm.capsula.FileUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/** Locator for targets based on a file system path.
 * */
@Slf4j
final class PathTargetLocator implements TargetLocator {

    /** Path to targets that are available. */
    private final Path targets;

    /** Creates a locator that extracts its targets from
     * the given path.
     * @param myTargets path to extract the targets from.
     * */
    PathTargetLocator(final Path myTargets) {
        this.targets = Objects.requireNonNull(myTargets);
    }

    @Override
    public Path extractTargetToTmp(final Path tempParent,
                                   final String target) throws IOException {

        log.debug("Extracting target {} to {}",
                target, tempParent);

        Path includeSource = targets.resolve(INCLUDE_DIRECTORY);
        Path tempTarget = tempParent.resolve(target);

        Files.list(includeSource).forEach(p ->
                FileUtils.copyRecursive(
                        p, tempTarget.resolve(p.getFileName()), q -> { }));

        Path targetSource = targets.resolve(TARGETS_DIRECTORY).resolve(target);

        Files.list(targetSource).forEach(p ->
                FileUtils.copyRecursive(
                        p, tempTarget.resolve(p.getFileName()), q -> { }));

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
