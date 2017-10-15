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
package de.sfuhrm.capsula.targetbuilder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import de.sfuhrm.capsula.yaml.Capsula;
import de.sfuhrm.capsula.yaml.Layout;
import freemarker.template.Configuration;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.log4j.MDC;

/**
 *
 * @author Stephan Fuhrmann
 */
@Slf4j
public class TargetBuilder implements Callable<TargetBuilder.Result> {

    @Getter
    private final Path layoutDirectory;
    
    @Getter
    private Path layoutFilePath;
    
    @Getter
    private Path targetPath;
    
    @Getter
    private Map<String, Object> environment;
    
    @Getter    
    private final Capsula build;
    
    public final static String LAYOUT_YAML = "layout.yaml";
    
    private TemplateDelegate templateDelegate;
    
    public TargetBuilder(Capsula build, Path directory) throws IOException {
        this.build = Objects.requireNonNull(build);
        
        log.debug("Layout directory is {}", directory);
        this.layoutDirectory = Objects.requireNonNull(directory, "directory is null");
        if (! Files.isDirectory(directory)) {
            throw new IllegalStateException(directory+" is not a directory");
        }

        layoutFilePath = directory.resolve(LAYOUT_YAML);
        log.debug("Layout file is {}", layoutFilePath);
        if (! Files.isRegularFile(layoutFilePath)) {
            throw new IllegalStateException(layoutFilePath+" is not a file");
        }
        
        targetPath = Files.createTempDirectory("capsula").toAbsolutePath();
        log.debug("Target path is {}", targetPath);
        templateDelegate = new TemplateDelegate(this);
    }
    
    public Layout readLayout() throws IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());        
        
        Path tmp = Files.createTempFile("layout", "yaml");
        
        templateDelegate.template(LAYOUT_YAML, tmp.toString());
        
        Layout layout = mapper.readValue(tmp.toFile(), Layout.class);
        return layout;
    }
    
    private void initEnvironment() {
        environment = new HashMap<>();
        environment.put("capsula", getBuild());
        environment.put("source", layoutDirectory);
        environment.put("target", targetPath);
    }

    @Override
    public Result call() throws Exception {
        initEnvironment();
        Layout layout = readLayout(); // must be AFTER initEnvironment()
        
        MDC.put("layout", layout.getName());
        
        if (layout.getEnvironment() != null) {
            environment.putAll(layout.getEnvironment());
        }
        environment.put("layout", layout);
        
        if (layout.getCopy()!= null) {
            CopyDelegate delegate = new CopyDelegate(this);
            layout.getCopy().entrySet().stream()
                    .forEach(e -> {
                        delegate.copy(e.getValue(), e.getKey());
                    });
        }
        
        if (layout.getTemplates()!= null) {
            TemplateDelegate delegate = templateDelegate;
            layout.getTemplates().entrySet().stream()
                    .forEach(e -> {
                        delegate.template(e.getValue(), e.getKey());
                    });
        }
        
        if (layout.getRun()!= null) {
            RunDelegate delegate = new RunDelegate(this);
            layout.getRun().stream()
                    .forEach(e -> {
                        delegate.run(e.split(" "));
                    });
        }
        
        MDC.remove("layout");
        
        Result result = new Result();
        return result;
    }

    public void cleanup() {
        deleteRecursive(targetPath);
    }
    
    private static void deleteRecursive(Path p) {
        try {
            if (Files.isRegularFile(p)) {
                log.debug("Deleting file {}", p);
                Files.delete(p);
            }
            if (Files.isDirectory(p)) {
                log.debug("Deleting directory contents {}", p);
                Files.list(p).forEach(t -> deleteRecursive(t));
                log.debug("Deleting directory {}", p);
                Files.delete(p);
            }
        } catch (IOException exception) {
            throw new BuildException("Error deleting recursively: " + p, exception);
        }
    }
    
    
    static class Result {
        boolean success;
    }
}
