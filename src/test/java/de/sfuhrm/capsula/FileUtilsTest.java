package de.sfuhrm.capsula;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FileUtilsTest {
    @Test
    public void testMkDirsWithOneDirectory() throws IOException {
        Path tmp = Files.createTempDirectory("foo");
        String single = "single";
        Path newDir = tmp.resolve(single);
        List<Path> newDirs = new ArrayList<>();

        FileUtils.mkdirs(newDir, p -> newDirs.add(p));
        assertEquals(Arrays.asList(newDir), newDirs);

        FileUtils.deleteRecursive(tmp);
    }

    @Test
    public void testMkDirsWithTwoDirectories() throws IOException {
        Path tmp = Files.createTempDirectory("foo");
        String a = "first";
        String b = "second";
        Path newDir = tmp.resolve(a).resolve(b);
        List<Path> newDirs = new ArrayList<>();

        FileUtils.mkdirs(newDir, p -> newDirs.add(p));
        assertEquals(Arrays.asList(newDir, newDir.getParent()), newDirs);

        FileUtils.deleteRecursive(tmp);
    }
}
