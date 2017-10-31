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
    /** The name of the package to gernerate. */
    @Getter @NotNull @NotBlank
    private String packageName;
    /** The author of the software in the package. */
    @Getter @NotNull @Valid
    private NameEmail author;
    /** The maintainer of the package. */
    @Getter @NotNull @Valid
    private NameEmail maintainer;
    /** The homepage of the project. */
    @Getter @NotNull @NotBlank @URL
    private String homepage;
    /** Where to get the project source code. */
    @Getter @NotNull @URL
    private String gitUrl;
    /** Short summary what this project is. */
    @Getter @NotNull @NotBlank
    private String shortSummary;
    /** Long multi-line description of the project. */
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
        private License(String inName, String inDebianName, String inLicenseTextUrl) {
            this.licenseName = Objects.requireNonNull(inName);
            this.debianName = Objects.requireNonNull(inDebianName);
            this.licenseTextUrl = Objects.requireNonNull(inLicenseTextUrl);
        }
        public List<String> getLicenseText() throws IOException {
            try (InputStream inputStream = new java.net.URL(getLicenseTextUrl()).openStream();
                    InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
                List<String> lines = new ArrayList<>();
                String line;
                while (null != (line = bufferedReader.readLine())) {
                    lines.add(line);
                }
                return lines;
            }
        }
        public String getDebianFile() {
            return "/usr/share/common-licenses/" + debianName;
        }
    };
    /** The license for the project. */
    @Getter @NotNull @Valid
    private License license;
    /** Debian specific information. */
    @Getter @NotNull @Valid
    private Debian debian;
    /** Redhat specific information. */
    @Getter @NotNull @Valid
    private Redhat redhat;
    /** Which targets to create packages for. */
    @Getter @Valid @Size(min = 1)
    private Set<String> targets;
    /** A changelog with newest versions coming first. The first
     * version is the version to generate.
     */
    @Getter @Valid @Size(min = 1)
    private List<VersionWithChanges> versions;
    /** Installation commmands.
     */
    @Getter @Valid @NotNull
    private List<Command> install;
    /** Calculates {@link Version#releaseNumber} fields.
     */
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
            throw new IllegalArgumentException("Can not determine got project from url '" + getGitUrl() + "'");
        }
    }
}
