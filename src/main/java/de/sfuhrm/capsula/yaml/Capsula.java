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
import java.util.stream.Collectors;
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
    private String packageName;

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
    
    @Getter @NotNull @Valid
    private Redhat redhat;
    
    enum Runtime {
        JDK,
        JRE
    };
    
    @Getter @NotNull @Valid
    private Runtime runtime;
    
    @Getter @Valid
    private Set<String> targets;
    
    @Getter @Valid
    private List<VersionWithChanges> versions; 
    
    @Getter @Valid
    private List<Command> install;
    
    
    public void calculateReleaseNumbers() {
        for (VersionWithChanges versionWithChanges : versions) {
            List<VersionWithChanges> sameVersion = versions.stream()
                    .filter(v -> v.getVersion().equals(versionWithChanges.getVersion()))
                    .collect(Collectors.toList());
            
            int total = sameVersion.size();
            int indexInverse = sameVersion.indexOf(versionWithChanges);
            int index = total - indexInverse - 1;
            
            versionWithChanges.setReleaseNumber(index);
        }
    }
    
    /** Get the project directory name from the GIT URL. */
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
