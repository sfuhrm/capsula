/*
 * Copyright (C) 2017 Stephan Fuhrmann
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package de.sfuhrm.capsula;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import de.sfuhrm.capsula.targetbuilder.BuildException;
import de.sfuhrm.capsula.targetbuilder.TargetBuilder;
import de.sfuhrm.capsula.yaml.Capsula;
import de.sfuhrm.capsula.yaml.PropertyInheritance;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import javax.validation.ConstraintViolation;
import lombok.extern.slf4j.Slf4j;

/**
 * The main class that gets executed from command line.
 *
 * @author Stephan Fuhrmann
 */
@Slf4j
public final class Main {

    /** The parameters from the command line. */
    private final Params params;

    /** Locator object for targets. */
    private final TargetLocator targetLocator;

    /** Creates a new instance.
     * @param myParams the parameters from the command line.
     * */
    public Main(final Params myParams) {
        this.params = Objects.requireNonNull(myParams);

        if (myParams.getTargetLayouts() != null) {
            log.debug("Using path target locator in {}",
                    myParams.getTargetLayouts());
            this.targetLocator = new PathTargetLocator(
                    myParams.getTargetLayouts());
        } else {
            log.debug("Using class path target locator");
            this.targetLocator = new ClassPathTargetLocator();
        }
    }

    /**
     * Reads the descriptor and fills auto-generated fields in it.
     * @return the yet unvalidated build descriptor.
     * @throws IOException if something goes wrong while reading.
     */
    private Capsula readDescriptor() throws IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        if (params.getDescriptor() == null) {
            throw new IllegalArgumentException("Need a descriptor"
                    + " set in the command line options.");
        }
        Capsula build = mapper.readValue(params.getDescriptor().toFile(),
                Capsula.class);
        build.calculateReleaseNumbers();
        PropertyInheritance.inherit(build, build.getDebian());
        PropertyInheritance.inherit(build, build.getRedhat());
        PropertyInheritance.inherit(build, build.getArchlinux());
        build.getVersions().stream().forEach(v -> {
            if (v.getMaintainer() == null) {
                v.setMaintainer(build.getMaintainer());
            }
        });
        // if in debug mode, write the expanded capsula.yaml
        if (params.isDebug()) {
            mapper.writeValue(params.getOut().resolve("capsula.yaml").toFile(),
                    build);
        }
        return build;
    }

    /** Read and validate the build descriptor.
     * @return the optional build descriptor or {@link Optional#empty()}
     * if the program may not
     * go on (validation error, descriptor validation option given).
     * @throws IOException if something goes wrong while reading.
     * */
    public Optional<Capsula> readAndValidateDescriptor() throws IOException {
        log.debug("Stage entered: {}", Stage.READ_DESCRIPTOR);
        final Capsula build = readDescriptor();
        ValidationDelegate validationDelegate = new ValidationDelegate();
        Set<ConstraintViolation<Capsula>> constraintViolations =
                validationDelegate.validate(build);
        if (!constraintViolations.isEmpty() || params.isValidate()) {
            if (constraintViolations.isEmpty()) {
                System.err.println("YAML descriptor contains no errors.");
            }
            return Optional.empty();
        }
        log.debug("Stage passed: {}", Stage.READ_DESCRIPTOR);
        return Optional.of(build);
    }

    /** Build for one target.
     * @param target the name of the target to build.
     * @param build the build descriptor to use (from the YAML descriptor).
     * @param myBuildDir the build directory to put the temporary build
     *                   files in.
     * @throws BuildException if something goes wrong while building.
     * */
    private void buildTarget(final String target, final Capsula build,
                             final Path myBuildDir) throws BuildException {
        try {
            log.debug("Target {}", target);

            final Path targetPath = targetLocator.extractTargetToTmp(
                    myBuildDir, target);

            TargetBuilder builder = new TargetBuilder(build, myBuildDir,
                    target, targetPath,
                    params.getStopAfter(), params.isVerbose());

            try {
                builder.call();
                if (params.getStopAfter().compareTo(Stage.COPY_RESULT) >= 0) {
                    log.debug("Stage entered: {}", Stage.COPY_RESULT);
                    builder.copyPackageFilesTo(params.getOut());
                    log.debug("Stage passed: {}", Stage.COPY_RESULT);
                }
            } finally {
                if (params.isDebug()) {
                    System.err.println("DEBUG: Target directory: "
                            + builder.getTargetPath());
                }
            }
        } catch (Exception ex) {
            throw new BuildException("Problem in builder " + target, ex);
        }
    }

    /** Delete temporary directory.
     * @param myBuildDir the directory used for building that contains the
     *                   temporary files. Will be recursively
     *                   deleted.
     * */
    private void cleanup(final Path myBuildDir) {
        if (!params.isDebug()
                && params.getStopAfter().compareTo(Stage.CLEANUP) >= 0) {
            log.debug("Stage entered: {}", Stage.CLEANUP);
            FileUtils.deleteRecursive(myBuildDir);
            log.debug("Stage passed: {}", Stage.CLEANUP);
        }
    }

    /** The main method.
     * @param args the command line parameters as a String array.
     * @throws IOException if something goes wrong while reading the
     * descriptor.
     * */
    public static void main(final String[] args) throws IOException {
        final Params params = Params.parse(args);
        if (params == null) {
            return;
        }
        final Main main = new Main(params);

        Path myBuildDir;
        if (params.getBuildDirectory() != null) {
            myBuildDir = params.getBuildDirectory().toAbsolutePath();
            Files.list(myBuildDir).forEach(p -> FileUtils.deleteRecursive(p));
        } else {
            myBuildDir = Files.createTempDirectory("capsula").toAbsolutePath();
        }
        if (params.isListTargets()) {
            System.out.println(main.targetLocator.getTargets());
            return;
        }
        log.debug("Stop after: {}", params.getStopAfter());
        main.buildTargets(myBuildDir);
    }

    /** Creates a target stream.
     * @param build the build to create the stream for.
     * @return a serial or parallel target stream.
     * */
    private Stream<String> getTargetStream(final Capsula build) {
        final Stream<String> targetStream;
        if (params.isParallel()) {
            targetStream = build.getTargets().parallelStream();
        } else {
            targetStream = build.getTargets().stream();
        }
        return targetStream;
    }

    /** Build all targets.
     * @param myBuildDir the directory to use for
     *                  {@link Params#getBuildDirectory() building}, may be a
     *                   temporary directory. This is the directory
     *                   for all temporary files. The final production
     *                   files will be written to the
     *                   {@link Params#getOut()} directory.
     * @throws IOException if something goes wrong while reading the
     * descriptor.
     * */
    private void buildTargets(final Path myBuildDir) throws IOException {
        Optional<Capsula> buildOptional = readAndValidateDescriptor();
        if (!buildOptional.isPresent()
                || params.getStopAfter().compareTo(Stage.READ_DESCRIPTOR)
                <= 0) {
            return;
        }
        Capsula build = buildOptional.get();
        final Stream<String> targetStream = getTargetStream(build);

        targetStream
                .filter(t -> params.getTargets() == null
                        || params.getTargets().contains(t))
                .forEach(t -> buildTarget(t, build, myBuildDir)
                );

        log.debug("Cleaning up");
        cleanup(myBuildDir);
    }
}
