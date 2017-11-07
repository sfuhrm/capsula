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

import lombok.Getter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** A well-known software license. */
public enum License {
    /** GNU General Public License 3.0. */
    GPL_30("GPL-3.0", "GPL-3", "https://www.gnu.org/licenses/gpl-3.0.txt"),
    /** GNU Lesser General Public License 3.0. */
    LGPL_30("LGPL-3.0", "LGPL-3",
            "https://www.gnu.org/licenses/lgpl-3.0.txt"),
    /** GNU General Public License 2.0. */
    GPL_20("GPL-2.0", "GPL-2", "https://www.gnu.org/licenses/gpl-2.0.txt"),
    /** GNU Lesser General Public License 2.1. */
    LGPL_21("LGPL-2.1", "LGPL-2.1",
            "https://www.gnu.org/licenses/lgpl-2.1.txt"),
    /** GNU Lesser General Public License 2.0. */
    LGPL_20("LGPL-2.0", "LGPL-2",
            "https://www.gnu.org/licenses/lgpl-2.0.txt"),
    /** Apache License 2.0. */
    APACHE_20("APACHE-2.0", "Apache-2.0",
            "http://www.apache.org/licenses/LICENSE-2.0.txt");

    /** The URL of the license legal text. */
    @Getter
    private final String licenseTextUrl;

    /** The display name of the license. */
    @Getter
    private final String licenseName;

    /** The name of the license on Debian systems. */
    private final String debianName;

    /** New instance.
     * @param inName the display name of the license.
     * @param inDebianName the debian name of the license.
     * @param inLicenseTextUrl the URL where the license legal text
     *                         is located.
     * */
    License(final String inName, final String inDebianName,
            final String inLicenseTextUrl) {
        this.licenseName = Objects.requireNonNull(inName);
        this.debianName = Objects.requireNonNull(inDebianName);
        this.licenseTextUrl = Objects.requireNonNull(inLicenseTextUrl);
    }

    /** Reads the legal license text from the license embedded URL.
     * @return a list of lines of the license text.
     * @throws IOException if retrieving the license text failed.
     * */
    public List<String> getLicenseText() throws IOException {
        try (InputStream inputStream = new java.net.URL(
                getLicenseTextUrl()).openStream();
             InputStreamReader inputStreamReader =
                        new InputStreamReader(inputStream,
                                Charset.forName("UTF-8"));
             BufferedReader bufferedReader =
                     new BufferedReader(inputStreamReader)) {
            List<String> lines = new ArrayList<>();
            String line;
            while (null != (line = bufferedReader.readLine())) {
                lines.add(line);
            }
            return lines;
        }
    }

    /** Gets the license file inclusive directory under Debian systems.
     * TBD the path should be in the template, not here.
     * @return the Debian specific path of the license file.
     * */
    public String getDebianFile() {
        return "/usr/share/common-licenses/" + debianName;
    }
}
