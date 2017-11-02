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
package de.sfuhrm.capsula.targetbuilder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import de.sfuhrm.capsula.Stage;
import de.sfuhrm.capsula.ValidationDelegate;
import de.sfuhrm.capsula.yaml.Capsula;
import de.sfuhrm.capsula.yaml.Layout;
import de.sfuhrm.capsula.yaml.command.Command;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;
import javax.validation.ConstraintViolation;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.log4j.MDC;

/**
 * Builds for one build target using a layout file.
 *
 * @see Layout
 * @author Stephan Fuhrmann
 */
@Slf4j
public class TargetBuilder implements Callable<TargetBuilder.Result> {

    /**
     * Where to create temp directories.
     */
    @Getter
    private final Path tempRoot;

    /**
     * Where the layout file is in. Can be read-only.
     */
    @Getter
    private final Path layoutDirectory;
    /**
     * The path to the layout file itself.
     */
    @Getter
    private Path layoutFilePath;
    /**
     * The file to the target directory where the building happens. This is
     * usually a temporary directory. Must be writable!
     */
    @Getter
    private Path targetPath;
    /**
     * The environment for this target builder. Has the following variables set:
     * <ul>
     * <li> capsula: the object from the config file
     * <li> version: the first version from the config file ("this" version)
     * <li> source: the layout directory
     * <li> target: the target directory where the build is performed
     * <li> ... and everything from <code>environment.yaml</code>
     * </ul>
     */
    @Getter
    private Map<String, Object> environment;
    /**
     * The generic configuration file.
     */
    @Getter
    private final Capsula build;
    /**
     * The configuration for building this target.
     */
    private Layout layout;
    /**
     * Name of the layout config file in the directory.
     *
     * @see #readLayout()
     */
    private final static String LAYOUT_YAML = "layout.yaml";
    /**
     * Name of the environment config file in the directory.
     *
     * @see #readEnvironment()
     */
    private final static String ENVIRONMENT_YAML = "environment.yaml";
    /**
     * Delegate for template generation. Is used for all templating tasks, also
     * when reading the layout/environment files.
     */
    private TemplateDelegate templateDelegate;
    /**
     * The name of the target.
     */
    @Getter
    private final String targetName;

    /** Stop processing after this stage. */
    private Stage stopAfter;

    /**
     * Creates an instance.
     *
     * @param build the build descriptor for all builds.
     * @param tempRoot the root directory where to create temp directories.
     * @param targetName the name of this target.
     * @param layoutDirectory the directory the layout and templates are located.
     * in.
     * @param stopAfter the stage after which to stop.
     * @throws IOException if something goes wrong while initialization.
     */
    public TargetBuilder(Capsula build, Path tempRoot, String targetName, Path layoutDirectory, Stage stopAfter) throws IOException {
        this.build = Objects.requireNonNull(build);
        this.tempRoot = Objects.requireNonNull(tempRoot);
        this.targetName = Objects.requireNonNull(targetName);
        log.debug("Layout directory is {}", layoutDirectory);
        this.layoutDirectory = Objects.requireNonNull(layoutDirectory, "directory is null");
        if (!Files.isDirectory(layoutDirectory)) {
            throw new IllegalStateException(layoutDirectory + " is not a directory");
        }
        layoutFilePath = layoutDirectory.resolve(LAYOUT_YAML);
        log.debug("Layout file is {}", layoutFilePath);
        if (!Files.isRegularFile(layoutFilePath)) {
            throw new IllegalStateException(layoutFilePath + " is not a file");
        }
        targetPath = Files.createTempDirectory(tempRoot, this.targetName).toAbsolutePath();
        log.debug("Target path is {}", targetPath);
        templateDelegate = new TemplateDelegate(this);
        this.stopAfter = Objects.requireNonNull(stopAfter, "stopAfter");
    }

    /**
     * Reads the layout, processes it as a template and parses it.
     *
     * @return the parsed layout file as an object.
     */
    public Layout readLayout() throws IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        Path layoutTmp = Files.createTempFile(tempRoot, "layout", ".yaml");
        templateDelegate.template(LAYOUT_YAML, layoutTmp.toString(), Optional.empty());
        Layout myLayout = mapper.readValue(layoutTmp.toFile(), Layout.class);
        ValidationDelegate validationDelegate = new ValidationDelegate();
        Set<ConstraintViolation<Layout>> constraintViolations = validationDelegate.validate(myLayout);
        if (!constraintViolations.isEmpty()) {
            throw new BuildException(LAYOUT_YAML + " contains errors.");
        }
        return myLayout;
    }

    /**
     * Reads the environment from the file.
     */
    public Map<String, String> readEnvironment() throws IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        Path environmentTmp = Files.createTempFile(tempRoot, "environment", ".yaml");
        templateDelegate.template(ENVIRONMENT_YAML, environmentTmp.toString(), Optional.empty());
        Map<String, String> env = mapper.readValue(environmentTmp.toFile(), Map.class);
        return env;
    }

    /**
     * Initialize the preset variables that can be used in the template.
     */
    private void initEnvironment() {
        environment = new HashMap<>();
        environment.put("capsula", getBuild());
        environment.put("version", getBuild().getVersions().get(0));
        environment.put("source", layoutDirectory);
        environment.put("target", targetPath);
        try {
            Map<String, String> fileEnv = readEnvironment();
            environment.putAll(fileEnv);
        }
        catch (FileNotFoundException ex) {
            log.info("Environment file {} not found, going on without", ENVIRONMENT_YAML);
        }
        catch (IOException ex) {
            throw new BuildException("Error while loading " + ENVIRONMENT_YAML, ex);
        }
    }

    public void copyPackageFilesTo(Path out) throws IOException {
        Objects.requireNonNull(layout, "layout needs to be non-null");
        for (String file : layout.getPackages()) {
            Path fromPath = getTargetPath().resolve(file);
            Path toPath = out.resolve(fromPath.getFileName());
            log.info("Copying file {} to {}", fromPath, toPath);
            Files.copy(fromPath, toPath, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    @Override
    public Result call() throws Exception {
        initEnvironment();
        layout = readLayout(); // must be AFTER initEnvironment()
        Result result;
        try {
            MDC.put("layout", layout.getName());
            environment.put("layout", layout);
            if (stopAfter.compareTo(Stage.PREPARE) >= 0) {
                log.debug("Stage entered: {}", Stage.PREPARE);
                for (Command cmd : layout.getPrepare()) {
                    execute(cmd, "p" + layout.getPrepare().indexOf(cmd));
                }
                log.debug("Stage passed: {}", Stage.PREPARE);
            }

            if (stopAfter.compareTo(Stage.BUILD) >= 0) {
                log.debug("Stage entered: {}", Stage.BUILD);
                for (Command cmd : layout.getBuild()) {
                    execute(cmd, "b" + layout.getBuild().indexOf(cmd));
                }
                log.debug("Stage passed: {}", Stage.BUILD);
            }
            result = new Result();
            result.setSuccess(true); // TBD never used?
        } finally {
            MDC.remove("layout");
        }
        return result;
    }

    /** Executes the given command.
     */
    private void execute(final Command cmd, final String commandId) throws IOException {
        MDC.put("cmdId", commandId);
        try {
            if (cmd.getCopy() != null) {
                CopyDelegate delegate = new CopyDelegate(this);
                delegate.copy(cmd.getCopy());
            }
            if (cmd.getTemplate() != null) {
                TemplateDelegate delegate = templateDelegate;
                delegate.template(cmd.getTemplate().getFrom(), cmd.getTemplate().getTo(), Optional.of(cmd.getTemplate()));
            }
            if (cmd.getRun() != null) {
                RunDelegate delegate = new RunDelegate(this);
                delegate.run(cmd.getRun());
            }
            if (cmd.getMkdir() != null) {
                MkdirDelegate delegate = new MkdirDelegate(this);
                delegate.mkdir(cmd.getMkdir());
            }
        } finally {
            MDC.remove("cmdId");
        }
    }

    static class Result {

        @Getter
        @Setter(AccessLevel.PRIVATE)
        private boolean success;
    }
}
