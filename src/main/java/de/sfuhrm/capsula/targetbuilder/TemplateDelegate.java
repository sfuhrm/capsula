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
package de.sfuhrm.capsula.targetbuilder;

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
import lombok.extern.slf4j.Slf4j;
import org.apache.log4j.MDC;

/**
 *
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

    public void template(String from, String to) {
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
        } catch (TemplateException | IOException ex) {
            throw new BuildException("Template problem for "+from, ex);
        }
    }
}
