package de.sfuhrm.capsula.targetlocator;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;

/**
 * Locates possible build targets.
 *
 * @author Stephan Fuhrmann
 */
public interface TargetLocator {

    /**
     * The directory of targets in the classpath.
     */
    String TARGETS_DIRECTORY = "targets";

    /**
     * The directory of includes in the classpath.
     */
    String INCLUDE_DIRECTORY = "include";

    /**
     * Extracts the target folder from the JAR archive to a temporary file on
     * disk.
     *
     * @param tempParent the directory to create the temporary files in.
     * @param target the target name to extract.
     * @return the name of the temporary directory where the target was
     * extracted to.
     * @throws IOException if an IO problem occurs.
     */
    Path extractTargetToTmp(Path tempParent,
                            String target) throws IOException;

    /**
     * Get the list of possible targets from the classpath.
     *
     * @return a set of target names.
     * @throws IOException if an IO problem occurs.
     */
    Set<String> getTargets() throws IOException;
}
