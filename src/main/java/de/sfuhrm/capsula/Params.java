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
package de.sfuhrm.capsula;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

/**
 * The command line parameters as a POJO. Must be created using
 * {@link #parse(java.lang.String[])}.
 *
 * @see #parse(java.lang.String[])
 * @author Stephan Fuhrmann
 */
@Slf4j
final class Params {

    /** Whether just to show the command line help.  */
    @Getter
    @Option(name = "-help", aliases = {"-h"},
            usage = "Show this command line help.", help = true)
    private boolean help;

    /** The YAML descriptor that describes how to build the package. */
    @Getter
    @Option(name = "-descriptor", aliases = {"-f"},
            usage = "YAML descriptor for the application packaging. "
                    + "Describes the package and how to "
                    + "install the components.",
            metaVar = "YAML", required = true)
    private Path descriptor;

    /** Whether to show more debugging information. */
    @Getter
    @Option(name = "-debug", aliases = {"-d"},
            usage = "Show more debugging output. Will keep temporary files "
                    + "instead of deleting them.")
    private boolean debug;

    /** Whether to stop after validating the
     * {@link #descriptor descriptor} file. */
    @Getter
    @Option(name = "-validate", aliases = {"-c"},
            usage = "Validate YAML descriptor file and exit.")
    private boolean validate;

    /** Optional directory for temporary files needed in the build process. */
    @Getter
    @Option(name = "-build-dir", aliases = {"-B"},
            usage = "The optional build directory to write the temporary "
                    + "building files to. "
                    + "When this option is not given a directory in the "
                    + "temporary directory is created.")
    private Path buildDirectory;

    /** Where to put the produced package files. */
    @Getter
    @Option(name = "-out", required = true, aliases = {"-o"},
            usage = "The output directory to write the generated packages to.")
    private Path out;

    /** Restriction of the targets to build. */
    @Getter
    @Option(name = "-targets", aliases = {"-t"},
            usage = "Restrict the targets created to the given names.")
    private List<String> targets;

    /** Show all targets that are available and exit .*/
    @Getter
    @Option(name = "-list-targets", aliases = {"-T"},
            usage = "List the currently available targets and exit.",
            help = true)
    private boolean listTargets;

    /** Whether to build the targets in parallel. */
    @Getter
    @Option(name = "-parallel", aliases = {"-p"},
            usage = "Execute the targets in parallel. This may need much"
                     + "memory. Please note that the console output will "
                     + "be almost useless with parallel execution.")
    private boolean parallel;

    /** Stop running after the given processing stage.
     * @see Stage
     * */
    @Getter
    @Option(name = "-stop-after",
            usage = "Stop after the given processing stage.")
    private Stage stopAfter = Stage.ALL;

    /**
     * Parse the command line options.
     *
     * @param args the command line args as passed to the main method of the
     * program.
     * @return the parsed command line options or {@code null} if the program
     * needs to exit. {@code null} will be returned if the command lines are
     * wrong or the command line help was displayed.
     */
    public static Params parse(final String[] args) {
        Params result = new Params();
        CmdLineParser cmdLineParser = new CmdLineParser(result);
        try {
            if (log.isDebugEnabled()) {
                log.debug("Args: {}", Arrays.toString(args));
            }
            cmdLineParser.parseArgument(args);
            if (result.help) {
                cmdLineParser.printUsage(System.err);
                return null;
            }
            return result;
        } catch (CmdLineException ex) {
            log.warn("Error in parsing", ex);
            System.err.println(ex.getMessage());
            cmdLineParser.printUsage(System.err);
        }
        return null;
    }
}
