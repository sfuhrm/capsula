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
import java.util.Set;
import java.util.stream.Stream;
import javax.validation.ConstraintViolation;
import javax.xml.bind.JAXBException;
import lombok.extern.slf4j.Slf4j;
import org.xml.sax.SAXException;

/**
 * The main class that gets executed from command line.
 *
 * @author Stephan Fuhrmann
 */
@Slf4j
public class Main {

    private final Params params;

    public Main(final Params myParams) {
        this.params = Objects.requireNonNull(myParams);
    }

    /**
     * Reads the descriptor and fills auto-generated fields in it.
     */
    private Capsula readDescriptor() throws IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        Capsula build = mapper.readValue(params.getDescriptor().toFile(), Capsula.class);
        build.calculateReleaseNumbers();
        PropertyInheritance.inherit(build, build.getDebian());
        PropertyInheritance.inherit(build, build.getRedhat());
        build.getVersions().stream().forEach(v -> {
            if (v.getMaintainer() == null) {
                v.setMaintainer(build.getMaintainer());
            }
        });
        // if in debug mode, write the expanded capsula.yaml
        if (params.isDebug()) {
            mapper.writeValue(params.getOut().resolve("capsula.yaml").toFile(), build);
        }
        return build;
    }

    public static void main(final String[] args) throws IOException, JAXBException, SAXException {
        final Params params = Params.parse(args);
        if (params == null) {
            return;
        }
        final Main main = new Main(params);
        log.debug("Stage entered: {}", Stage.READ_DESCRIPTOR);
        final Capsula build = main.readDescriptor();
        ValidationDelegate validationDelegate = new ValidationDelegate();
        Set<ConstraintViolation<Capsula>> constraintViolations = validationDelegate.validate(build);
        if (!constraintViolations.isEmpty() || params.isValidate()) {
            if (constraintViolations.isEmpty()) {
                System.err.println("YAML descriptor contains no errors.");
            }
            return;
        }
        log.debug("Stage passed: {}", Stage.READ_DESCRIPTOR);


        Path myBuildDir = params.getBuildDirectory() != null ?
                params.getBuildDirectory() :
                Files.createTempDirectory("capsula");
        final TargetLocator targetLocator = new TargetLocator();
        if (params.isListTargets()) {
            System.out.println(targetLocator.getTargets());
            return;
        }
        log.debug("Stop after: {}", params.getStopAfter());
        if (params.getStopAfter().compareTo(Stage.READ_DESCRIPTOR) <= 0) {
            return;
        }
        final Stream<String> targetStream = params.isParallel() ?
                build.getTargets().parallelStream() :
                build.getTargets().stream();

        targetStream
                .filter(t -> params.getTargets() == null || params.getTargets().contains(t))
                .forEach(t -> {
                    try {
                        log.debug("Target {}", t);
                        final Path targetPath = targetLocator.extractTargetToTmp(myBuildDir, t);
                        TargetBuilder builder = new TargetBuilder(build, myBuildDir, t, targetPath, params.getStopAfter());
                        try {
                            builder.call();
                            if (params.getStopAfter().compareTo(Stage.COPY_RESULT) >= 0) {
                                log.debug("Stage entered: {}", Stage.COPY_RESULT);
                                builder.copyPackageFilesTo(params.getOut());
                                log.debug("Stage passed: {}", Stage.COPY_RESULT);
                            }
                        }
                        finally {
                            if (params.isDebug()) {
                                System.err.println("DEBUG: Target directory: " + builder.getTargetPath());
                            }
                        }
                    }
                    catch (Exception ex) {
                        throw new BuildException("Problem in builder " + t, ex);
                    }
                });

        log.debug("Cleaning up");
        if (!params.isDebug() && params.getStopAfter().compareTo(Stage.CLEANUP) >= 0) {
            log.debug("Stage entered: {}", Stage.CLEANUP);
            FileUtils.deleteRecursive(myBuildDir);
            log.debug("Stage passed: {}", Stage.CLEANUP);
        }
    }
}
