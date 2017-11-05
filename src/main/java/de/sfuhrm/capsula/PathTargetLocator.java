package de.sfuhrm.capsula;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class PathTargetLocator implements TargetLocator {

    /** Path to targets that are available. */
    private final Path targets;

    public PathTargetLocator(Path myTargets) {
        this.targets = Objects.requireNonNull(myTargets);
    }

    @Override
    public Path extractTargetToTmp(Path tempParent, String target) throws IOException {
        Path source = targets.resolve(target);
        Path tempTarget = tempParent.resolve(target);

        return null;
    }

    @Override
    public Set<String> getTargets() throws IOException {
        return Files.list(targets)
                .filter(p -> Files.isDirectory(p))
                .map(p -> p.getFileName().toString())
                .collect(Collectors.toSet());
    }
}
