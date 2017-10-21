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
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
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
    private NameEmail author;
    
    @Getter @NotNull @Valid
    private NameEmail maintainer;
    
    @Getter @NotNull @NotBlank @URL
    private String homepage;

    @Getter
    private String iconFile;
    
    @Getter @NotNull @URL
    private String gitUrl;
    
    @Getter @NotNull @NotBlank
    private String javaVersion;
    
    @Getter @NotNull @NotBlank
    private String shortSummary;
    
    @Getter @NotNull @Size(min = 1)
    private List<String> longDescription;
    
    public enum License {
        GPL_30("GPL-3.0", "GPL-3", "https://www.gnu.org/licenses/gpl-3.0.txt"),
        LGPL_30("LGPL-3.0", "LGPL-3", "https://www.gnu.org/licenses/lgpl-3.0.txt"),        
        GPL_20("GPL-2.0", "GPL-2", "https://www.gnu.org/licenses/gpl-2.0.txt"),
        LGPL_21("LGPL-2.1", "LGPL-2.1", "https://www.gnu.org/licenses/lgpl-2.1.txt"),
        LGPL_20("LGPL-2.0", "LGPL-2", "https://www.gnu.org/licenses/lgpl-2.0.txt"),
        APACHE_20("APACHE-2.0", "Apache-2.0", "http://www.apache.org/licenses/LICENSE-2.0.txt");

        @Getter
        private final String licenseTextUrl;
        
        @Getter
        private final String licenseName;
        
        private final String debianName;
        
        private License(String name, String debianName, String licenseTextUrl) {
            this.licenseName = Objects.requireNonNull(name);
            this.debianName = Objects.requireNonNull(debianName);
            this.licenseTextUrl = Objects.requireNonNull(licenseTextUrl);
        }
        
        public List<String> getLicenseText() throws IOException {
            InputStream inputStream = new java.net.URL(getLicenseTextUrl()).openStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            List<String> lines = new ArrayList<>();
            String line;
            
            while (null != (line = bufferedReader.readLine())) {
                lines.add(line);
            }
            return lines;
        }
        
        public String getDebianFile() {
            return "/usr/share/common-licenses/"+debianName;
        }
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
    
    public String getGitProject() throws MalformedURLException {
        Pattern p = Pattern.compile(".*/([^/]*)\\.git");
        Matcher m = p.matcher(getGitUrl());
        
        if (m.matches()) {
            return m.group(1);
        } else {
            throw new IllegalArgumentException("Can not determine got project from url '"+getGitUrl()+"'");
        }
    }
}
