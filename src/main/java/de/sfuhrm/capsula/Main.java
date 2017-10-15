/*
 * Copyright 2017 Stephan Fuhrmann.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.sfuhrm.capsula;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import de.sfuhrm.capsula.targetbuilder.BuildException;
import de.sfuhrm.capsula.targetbuilder.TargetBuilder;
import de.sfuhrm.capsula.yaml.Capsula;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
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

    public Capsula getDescriptor() throws IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        Capsula build = mapper.readValue(params.getDescriptor().toFile(), Capsula.class);
        validate(build);
        return build;
    }

    private <T> void validate(T o) {
        final Set<ConstraintViolation<T>> violations = validator.validate(o);
        if (violations.size() > 0) {
            AtomicBoolean ok = new AtomicBoolean(true);

            System.out.println("YAML config contains errors:");
            violations.forEach(u -> {
                log.error("Validation error for {} {}. ", u.getPropertyPath().toString(), u.getMessage());
                System.err.println("  \"" + u.getPropertyPath().toString() + "\"" + " " + u.getMessage());
                ok.set(false);
            });

            if (!ok.get()) {
                throw new IllegalArgumentException("Validation error, exiting");
            }
        } else {
            log.debug("Object validated");
        }

    }

    public static void main(String[] args) throws IOException, JAXBException, SAXException {
        Params params = Params.parse(args);
        if (params == null) {
            return;
        }

        Main main = new Main(params);
        Capsula build = main.getDescriptor();

        build.getTargets().stream().forEach(t -> {
            try {
                log.debug("Target {}", t);
                TargetBuilder builder = new TargetBuilder(build, FileSystems.getDefault().getPath(t));
                try {
                    builder.call();
                } finally {
                    if (params.isDebug()) {
                        System.err.println("DEBUG: Target directory: " + builder.getTargetPath());
                    } else {
                        log.debug("Cleaning up builder");
                        builder.cleanup();
                    }
                }
            } catch (Exception ex) {
                throw new BuildException("Problem in builder " + t, ex);
            }
        });
    }
}
