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
package de.sfuhrm.capsula.yaml.constraints;

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Checks whether a file exists.
 * @author Stephan Fuhrmann
 */
public class FileExistsValidator implements ConstraintValidator<FileExists, String> {

    @Override
    public boolean isValid(String t, ConstraintValidatorContext cvc) {
        if (t == null) {
            return true;
        }

        Path path = FileSystems.getDefault().getPath(t);
        return Files.exists(path);
    }
}
