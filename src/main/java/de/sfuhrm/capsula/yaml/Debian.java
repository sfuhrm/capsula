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

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Getter;

/**
 * Specific descriptor for Debian distribution.
 * @see https://www.debian.org/doc/manuals/maint-guide/dreq.de.html
 * @author Stephan Fuhrmann
 */
public class Debian {
    @Getter @NotNull @NotBlank
    private String packageName;
    
    enum Priority {
        optional,
        required,
        important,
        standard
    };
    
    @Getter @NotNull
    private Priority priority = Priority.optional;
    
    @Getter @NotNull @NotBlank
    private String section;
    
    enum Architecture {
        any,
        all
    };
    
    @Getter @NotNull @NotBlank
    private String version;
    
    @Getter @NotNull
    private Architecture architecture;
}
