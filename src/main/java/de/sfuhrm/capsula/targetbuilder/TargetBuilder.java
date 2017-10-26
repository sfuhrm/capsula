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
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
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
    
    /** Reads the layout, processes it as a template and parses it.
     * @return the parsed layout file as an object.
     */
    public Layout readLayout() throws IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());        
        
        Path tmp = Files.createTempFile("layout", "yaml");
        
        templateDelegate.template(LAYOUT_YAML, tmp.toString());
        
        Layout layout = mapper.readValue(tmp.toFile(), Layout.class);
        return layout;
    }
    
    /** Initialize the preset variables that can be used in the
     * template.
     */
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
