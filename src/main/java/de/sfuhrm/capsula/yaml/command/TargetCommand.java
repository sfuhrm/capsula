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
package de.sfuhrm.capsula.yaml.command;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * A comand having a source and target file.
 * @author Stephan Fuhrmann
 */
public class TargetCommand {
    @Getter @Setter @NotNull
    private String to;
    
    @Getter @Setter @Size(min = 1)
    private String owner;
    
    @Getter @Setter @Size(min = 1)
    private String group;
    
    @Getter @Setter @Pattern(regexp = "([r-][w-][x-]){3}") @NotNull
    private String mode;
    
    public String getOctal() {
        String myMode = getMode();
        int value = 0;
        for (int i = 0; i < 3; i++) {
            int offset = 3 * i;
            for (int j = 0; j < 3; j++) {
                char c = myMode.charAt(offset + j);
                int bitValue = 2 - j;
                value |= c != '-' ? (1<<bitValue) << (3*(2-i)) : 0;
            }
        }
        return String.format("%04o", value);
    }
}
