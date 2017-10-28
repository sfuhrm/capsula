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
 * @author Stephan Fuhrmann
 */
@Slf4j
class TemplateDelegate extends AbstractDelegate {
    
    private final Configuration cfg;

    public TemplateDelegate(TargetBuilder targetBuilder) throws IOException {
        super(targetBuilder);
        
        cfg = new Configuration(Configuration.VERSION_2_3_23);
        cfg.setDirectoryForTemplateLoading(targetBuilder.getLayoutDirectory().toFile());
        cfg.setDefaultEncoding("UTF-8");
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        cfg.setLogTemplateExceptions(false);
        cfg.setLocale(Locale.US);
    }

    public void template(String from, String to, Optional<TargetCommand> cmd) throws IOException {
        MDC.put("from", from);
        MDC.put("to", to);
        Objects.requireNonNull(from, "from is null");
        Objects.requireNonNull(to, "to is null");
        
        Path toPath = getTargetBuilder().getTargetPath().resolve(to);
        
        try {
            Template temp = cfg.getTemplate(from);
            if (! Files.exists(toPath.getParent())) {                
                Files.createDirectories(toPath.getParent());            
            }       
            try (Writer out = Files.newBufferedWriter(toPath, Charset.forName("UTF-8"))) {
                temp.process(getTargetBuilder().getEnvironment(), out);
            }
            
            if (cmd.isPresent()) {
                FileUtils.applyTargetFileModifications(toPath, cmd.get());
            }
        } catch (TemplateException ex) {
            throw new BuildException("Template problem for "+from, ex);
        }
    }
}
