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
import de.sfuhrm.capsula.yaml.Capsula;
import de.sfuhrm.capsula.yaml.command.Command;
import de.sfuhrm.capsula.yaml.Layout;
import de.sfuhrm.capsula.yaml.command.TargetCommand;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.GroupPrincipal;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.nio.file.attribute.UserPrincipal;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Callable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.log4j.MDC;

/**
 * Builds for one build target using a layout file.
 * @see Layout
 * @author Stephan Fuhrmann
 */
@Slf4j
public class TargetBuilder implements Callable<TargetBuilder.Result> {

    /** Where the layout file is in. Can be read-only. */
    @Getter
    private final Path layoutDirectory;
    
    /** The path to the layout file itself. */
    @Getter
    private Path layoutFilePath;
    
    /** The file to the target directory where the building happens.
     * This is usually a temporary directory. Must be writable!
     */
    @Getter
    private Path targetPath;
    
    /** The environment for this target builder.
     * Has the following variables set:
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
    
    /** The generic configuration file. */
    @Getter    
    private final Capsula build;
    
    /** The configuration for building this target. */
    private Layout layout;
    
    /** Name of the layout config file in the directory. 
     * @see #readLayout() 
     */
    private final static String LAYOUT_YAML = "layout.yaml";
    
    /** Name of the environment config file in the directory. 
     * @see #readEnvironment()  
     */
    private final static String ENVIRONMENT_YAML = "environment.yaml";
    
    /** Delegate for template generation. Is used for all templating tasks,
     * also when reading the layout/environment files.
     */
    private TemplateDelegate templateDelegate;
    
    /**
     * Creates an instance.
     * @param build the build descriptor for all builds.
     * @param directory the target the layout and templates are located in.
     * @throws IOException if something goes wrong while initialization.
     */
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
    
    private Path layoutTmp;
    /** Reads the layout, processes it as a template and parses it.
     * @return the parsed layout file as an object.
     */
    public Layout readLayout() throws IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());        
        
        layoutTmp = Files.createTempFile("layout", "yaml");
        
        templateDelegate.template(LAYOUT_YAML, layoutTmp.toString());
        
        Layout layout = mapper.readValue(layoutTmp.toFile(), Layout.class);
        return layout;
    }
    
    private Path environmentTmp;
    public Map<String,String> readEnvironment() throws IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());        
        
        environmentTmp = Files.createTempFile("environment", "yaml");
        
        templateDelegate.template(ENVIRONMENT_YAML, environmentTmp.toString());
        
        Map<String,String> env = mapper.readValue(environmentTmp.toFile(), Map.class);
        return env;
    }
    
    /** Initialize the preset variables that can be used in the
     * template.
     */
    private void initEnvironment() {
        environment = new HashMap<>();
        environment.put("capsula", getBuild());
        environment.put("version", getBuild().getVersions().get(0));
        environment.put("source", layoutDirectory);
        environment.put("target", targetPath);
        try {
            
            Map<String,String> fileEnv = readEnvironment();
            environment.putAll(fileEnv);
        } 
        catch (FileNotFoundException ex) {
            log.info("Environment file {} not found, going on without", ENVIRONMENT_YAML);
        }
        catch (IOException ex) {
            throw new BuildException("Error while loading "+ENVIRONMENT_YAML, ex);            
        }
    }
    
    public void copyPackageFilesTo(Path out) throws IOException {
        Objects.requireNonNull(layout, "layout needs to be non-null");
        for (String file : layout.getPackages()) {
            Path fromPath = getTargetPath().resolve(file);
            Path toPath = out.resolve(fromPath.getFileName());
            Files.copy(fromPath, toPath, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    @Override
    public Result call() throws Exception {
        initEnvironment();
        layout = readLayout(); // must be AFTER initEnvironment()
        
        MDC.put("layout", layout.getName());
        
        environment.put("layout", layout);
        
        for (Command cmd : layout.getPrepare()) {
            if (cmd.getCopy() != null) {
                CopyDelegate delegate = new CopyDelegate(this);
                delegate.copy(cmd.getCopy());
                applyTargetFileModifications(cmd.getCopy());
            }

            if (cmd.getTemplate()!= null) {
                TemplateDelegate delegate = templateDelegate;
                delegate.template(cmd.getTemplate().getFrom(), cmd.getTemplate().getTo());
                applyTargetFileModifications(cmd.getTemplate());
            }

            if (cmd.getRun() != null) {
                RunDelegate delegate = new RunDelegate(this);
                delegate.run(cmd.getRun());
            }
            
            if (cmd.getMkdir()!= null) {
                MkdirDelegate delegate = new MkdirDelegate(this);
                delegate.mkdir(cmd.getMkdir());
            }
        }
        
        MDC.remove("layout");
        
        Result result = new Result();
        result.setSuccess(true); // TBD never used?
        return result;
    }
    
    /** Changes owner, group and permissions for a target file.  */
    private void applyTargetFileModifications(TargetCommand command) throws IOException {
        Path toPath = getTargetPath().resolve(command.getTo());
                
        FileSystem fileSystem = toPath.getFileSystem();
        UserPrincipalLookupService lookupService
                = fileSystem.getUserPrincipalLookupService();
        
        if (command.getOwner() != null) {
            log.debug("Setting owner of {} to {}", toPath, command.getOwner());
            UserPrincipal owner = lookupService.lookupPrincipalByName(command.getOwner());
            Files.setOwner(toPath, owner);
        }
        
        if (command.getGroup() != null) {
            log.debug("Setting group of {} to {}", toPath, command.getOwner());
            GroupPrincipal group = lookupService.lookupPrincipalByGroupName(command.getGroup());
            Files.getFileAttributeView(toPath, PosixFileAttributeView.class, LinkOption.NOFOLLOW_LINKS).setGroup(group);
        }
        
        if (command.getMode()!= null) {
            log.debug("Setting mode of {} to {}", toPath, command.getMode());
            
            Set<PosixFilePermission> permissions = PosixFilePermissions.fromString(command.getMode());
            Files.getFileAttributeView(toPath, PosixFileAttributeView.class, LinkOption.NOFOLLOW_LINKS).setPermissions(permissions);
        }
    }

    /** Deletes the target directory. 
     * @throws BuildException in case of an IO exception.
     */
    public void cleanup() {
        deleteRecursive(targetPath);
        if (Files.exists(environmentTmp))
            deleteRecursive(environmentTmp);
        if (Files.exists(layoutTmp))
            deleteRecursive(layoutTmp);
    }
    
    /** Deletes a path and its children. 
     * @throws BuildException in case of an IO exception.
     */
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
        @Getter @Setter(AccessLevel.PRIVATE)
        private boolean success;
    }
}
