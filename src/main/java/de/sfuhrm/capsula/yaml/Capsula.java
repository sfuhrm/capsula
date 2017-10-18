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
package de.sfuhrm.capsula.yaml;

import de.sfuhrm.capsula.yaml.command.Command;
import java.util.List;
import java.util.Set;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import org.hibernate.validator.constraints.URL;

/**
 * Descriptor file for a application to generate a package for.
 * @author Stephan Fuhrmann
 */
public class Capsula {
    @Getter @NotNull @NotBlank
    private String name;

    @Getter @NotNull @NotBlank
    private String version;
    
    @Getter @NotNull @Valid
    private NameEmail maintainer;
    
    @Getter @NotNull @NotBlank @URL
    private String homepage;

    @Getter
    private String iconFile;
    
    @Getter @NotNull @NotBlank
    private String sourceTar;
    
    @Getter @NotNull @NotBlank
    private String javaVersion;
    
    @Getter @NotNull @NotBlank
    private String shortSummary;
    
    @Getter @NotNull @NotBlank
    private String longDescription;
    
    enum License {
        GPL2,
        LGPL2,
        APACHE20
    };
    
    @Getter @NotNull @Valid
    private License license;
        
    @Getter @NotNull @Valid
    private Debian debian;
    
    enum Runtime {
        JDK,
        JRE
    };
    
    @Getter @NotNull @Valid
    private Runtime runtime;
    
    @Getter @Valid
    private Set<String> targets;
    
    @Getter @Valid
    private List<Version> versions; 
    
    @Getter @Valid
    private List<Command> install;
}
