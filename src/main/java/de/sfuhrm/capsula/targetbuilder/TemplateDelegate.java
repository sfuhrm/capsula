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

import de.sfuhrm.capsula.FileUtils;
import de.sfuhrm.capsula.yaml.command.TargetCommand;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.apache.log4j.MDC;

/**
 * Delegate for template generation.
 *
 * @author Stephan Fuhrmann
 */
@Slf4j
class TemplateDelegate extends AbstractDelegate {

    /** The freemarker configuration to use. */
    private final Configuration cfg;

    /** The charset the files are in. */
    private Charset charset;

    /** Creates a new instance.
     * @param targetBuilder the target builder this is a delegate for.
     * @throws IOException if something goes wrong creating a config
     * for the template engine.
     * */
    TemplateDelegate(final TargetBuilder targetBuilder) throws IOException {
        super(targetBuilder);
        this.charset = Charset.forName("UTF-8");
        cfg = new Configuration(Configuration.VERSION_2_3_23);
        cfg.setDirectoryForTemplateLoading(targetBuilder
                .getLayoutDirectory()
                .toAbsolutePath()
                .toFile());
        cfg.setDefaultEncoding(charset.name());
        cfg.setTemplateExceptionHandler(
                TemplateExceptionHandler.RETHROW_HANDLER);
        cfg.setLogTemplateExceptions(false);
        cfg.setLocale(Locale.US);
    }

    /** Expands a template.
     * @param from the name of the template which is relative to
     *             the {@link TargetBuilder#getLayoutDirectory()
     *             layout directory}.
     * @param to the path to write the expanded template to, relative to
     *           the {@link TargetBuilder#getTargetPath() target path}.
     * @param targetCommand optional target command that carries the file
     *                      attributes of the instantiated template file.
     * @throws IOException if something goes wrong in template generation.
     * */
    public void template(final String from, final String to,
                         final Optional<TargetCommand> targetCommand)
            throws IOException {
        MDC.put("from", from);
        MDC.put("to", to);
        Objects.requireNonNull(from, "from is null");
        Objects.requireNonNull(to, "to is null");
        Path toPath = getTargetBuilder().getTargetPath().resolve(to);
        try {
            Template temp = cfg.getTemplate(from);
            if (!Files.exists(toPath.getParent())) {
                Files.createDirectories(toPath.getParent());
            }
            try (Writer out = Files.newBufferedWriter(toPath, charset)) {
                temp.process(getTargetBuilder().getEnvironment(), out);
            }
            if (targetCommand.isPresent()) {
                FileUtils.applyTargetFileModifications(toPath,
                        targetCommand.get());
            }
        } catch (TemplateException ex) {
            log.error("Template exception", ex);
            throw new BuildException("Template problem for " + from, ex);
        } finally {
            MDC.remove("from");
            MDC.remove("to");
        }
    }
}
