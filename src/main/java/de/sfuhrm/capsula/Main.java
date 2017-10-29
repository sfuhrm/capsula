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
        
    public static void main(String[] args) throws IOException, JAXBException, SAXException {
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
        
        TargetLocator targetLocator = new TargetLocator();
        if (params.isListTargets()) {
            System.out.println(targetLocator.getTargets());
            return;
        }

        build.getTargets()
                .stream()
                .filter(t -> params.getTargets() == null || params.getTargets().contains(t))
                .forEach(t -> {
            try {
                log.debug("Target {}", t);
                Path targetPath = targetLocator.extractTargetToTmp(t);
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
