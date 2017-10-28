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
import com.google.common.io.ByteSource;
import com.google.common.reflect.ClassPath;
import de.sfuhrm.capsula.targetbuilder.BuildException;
import de.sfuhrm.capsula.targetbuilder.TargetBuilder;
import de.sfuhrm.capsula.yaml.Capsula;
import de.sfuhrm.capsula.yaml.PropertyInheritance;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
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
    private final Validator validator;

    public Main(Params params) {
        this.params = Objects.requireNonNull(params);
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }
    
    /** Reads the descriptor and fills auto-generated fields in it.
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

    /** Validates the given object. Prints all constraint violations.
     * @return the set of constraint violations detected.
     */
    private <T> Set<ConstraintViolation<T>> validate(T o) {
        final Set<ConstraintViolation<T>> violations = validator.validate(o);
        if (violations.size() > 0) {
            System.err.println("YAML config contains errors:");
            violations.forEach(u -> {
                log.error("Validation error for {} {}. ", u.getPropertyPath().toString(), u.getMessage());
                System.err.println("  \"" + u.getPropertyPath().toString() + "\"" + " " + u.getMessage()+" (value: "+u.getInvalidValue()+")");
            });

            System.err.printf("Got %d validation errors\n", violations.size());
        } else {
            log.debug("Object validated");
            if (params.isValidate() || params.isDebug()) {
                System.err.println("Got no validation errors");
            }
        }
        return violations;
    }    
    
    /** Extracts the target folder from the JAR archive to a temporary
     * file on disk.
     * @param target the target name to extract.
     * @return the name of the temporary directory where the target was extracted to.
     */
    private Path extractTargetToTmp(String target) throws IOException {
        Path targetPath = Files.createTempDirectory(target).toAbsolutePath();
        log.debug("Target {} will be extracted to {}", target, targetPath);
        
        Set<String> targets = getTargets();
        log.debug("Targets in classpath: {}", targets);
        if (targets.isEmpty()) {
            throw new IllegalStateException("Targets not found");
        }
        
        if (!targets.contains(target)) {
            throw new NoSuchElementException("Target not found: "+target);
        }
        
        Set<String> files = getTargetResources(target);
        log.debug("Target {} files: {}", target, files);
        if (files.isEmpty()) {
            throw new IllegalStateException("Target "+target+" contains no files");
        }
        
        for (String file : files) {
            try (InputStream is = getResourceAsStream(target, file)) {
                Path toPath = targetPath.resolve(file);
                Files.copy(is, toPath);
            }
        }
        
        return targetPath;
    }

    public final static String TARGETS = "targets";
    
    /** All class path resources. */
    private static Set<ClassPath.ResourceInfo> resourceInfos;
    
    /** Get the class path resources containing targets. */
    private static Set<ClassPath.ResourceInfo> getClassPathResources() throws IOException {
        if (resourceInfos == null) {
            ClassPath classPath = ClassPath.from(Main.class.getClassLoader());
            resourceInfos = classPath.getResources()
                    .stream()
                    .filter(ri -> ri.getResourceName().startsWith(TARGETS))
                    .collect(Collectors.toSet());
        }
        return resourceInfos;
    }
    
    /** Opens a stream for the given resource. */
    InputStream getResourceAsStream(String target, String resource) throws IOException {
        Optional<ByteSource> byteSource = getClassPathResources()
                .stream()
                .filter(ri -> ri.getResourceName().equals(TARGETS+"/"+target+"/"+resource))
                .map(ri -> ri.asByteSource())
                .findFirst();
        return byteSource.orElseThrow(() -> new IOException("Can't find "+target+"/"+resource)).openStream();
    }
    
    /** Get the list of possible target resources from the classpath. */
    private Set<String> getTargetResources(String target) throws IOException {
        return getClassPathResources()
                .stream()
                .filter(ri -> ri.getResourceName().startsWith(TARGETS+"/"+target))
                .map(ri -> ri.getResourceName().split("/")[2])
                .collect(Collectors.toSet());
    }
    
    /** Get the list of possible targets from the classpath. */
    private Set<String> getTargets() throws IOException {
        return getClassPathResources()
                .stream()
                .filter(ri -> ri.getResourceName().startsWith(TARGETS+"/"))
                .map(ri -> ri.getResourceName().split("/")[1])
                .collect(Collectors.toSet());
    }
    
    public static void main(String[] args) throws IOException, JAXBException, SAXException {
        getClassPathResources();
        Params params = Params.parse(args);
        if (params == null) {
            return;
        }

        Main main = new Main(params);
        Capsula build = main.readDescriptor();
        Set<ConstraintViolation<Capsula>> constraintViolations = main.validate(build);
        if (!constraintViolations.isEmpty() || params.isValidate()) {
            return;
        }

        build.getTargets()
                .stream()
                .filter(t -> params.getTargets() == null || params.getTargets().contains(t))
                .forEach(t -> {
            try {
                log.debug("Target {}", t);
                Path targetPath = main.extractTargetToTmp(t);
                TargetBuilder builder = new TargetBuilder(build, t, targetPath);
                try {
                    builder.call();
                    builder.copyPackageFilesTo(params.getOut());
                } finally {
                    if (params.isDebug()) {
                        System.err.println("DEBUG: Target directory: " + builder.getTargetPath());
                    } else {
                        log.debug("Cleaning up builder");
                        builder.cleanup();
                        FileUtils.deleteRecursive(targetPath);
                    }
                }
            } catch (Exception ex) {
                throw new BuildException("Problem in builder " + t, ex);
            }
        });
    }
}
