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
    private final int MODE_GROUPS = 3;
    private final int GROUP_BITS = 3;

    @Getter @Setter @NotNull
    private String to;
    @Getter @Setter @Size(min = 1)
    private String owner;
    @Getter @Setter @Size(min = 1)
    private String group;
    @Getter @Setter @Pattern(regexp = "([r-][w-][x-]){" + MODE_GROUPS + "}")
    private String mode;
        
    public String getOctal() {
        String myMode = getMode();
        int value = 0;
        for (int group = 0; group < MODE_GROUPS; group++) {
            int offset = GROUP_BITS * group;
            for (int bit = 0; bit < GROUP_BITS; bit++) {
                char c = myMode.charAt(offset + bit);
                int bitValue = 2 - bit;
                value |= c != '-' ? (1 << bitValue) << (GROUP_BITS * (2 - group)) : 0;
            }
        }
        return String.format("%04o", value);
    }
}
