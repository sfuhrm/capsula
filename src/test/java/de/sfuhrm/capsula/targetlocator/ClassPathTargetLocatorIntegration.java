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
package de.sfuhrm.capsula.targetlocator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import static org.junit.Assert.*;

import de.sfuhrm.capsula.FileUtils;
import de.sfuhrm.capsula.targetlocator.ClassPathTargetLocator;
import org.junit.Test;

/**
 * Tests the {@link ClassPathTargetLocator}.
 *
 * @author Stephan Fuhrmann
 */
public class ClassPathTargetLocatorIntegration {

    public final String CENTOS_7 = "centos_7";
    public final String DEBIAN_STRETCH = "debian_stretch";

    @Test
    public void testGetTargets() throws IOException {
        ClassPathTargetLocator locator = new ClassPathTargetLocator();
        Set<String> targets = locator.getTargets();
        // this needs to be adjusted when adding more targets
        assertEquals(new HashSet<>(Arrays.asList(CENTOS_7, DEBIAN_STRETCH)), targets);
    }

    @Test
    public void testExtractTargetToTmp() throws IOException {
        Path tmp = null;
        try {
            tmp = Files.createTempDirectory("targetlocatortest");
            ClassPathTargetLocator locator = new ClassPathTargetLocator();
            locator.extractTargetToTmp(tmp, CENTOS_7);

            List<Path> paths = Files.walk(tmp)
                    .filter(p -> Files.isRegularFile(p))
                    .collect(Collectors.toList());

            // at the moment there are 4 files in the centos_7 folder
            assertEquals(4, paths.size());
        } finally {
            if (tmp != null) {
                FileUtils.deleteRecursive(tmp);
            }
        }
    }
}
