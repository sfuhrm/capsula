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
 *
 * @author Stephan Fuhrmann
 */
public class TargetCommand implements PermissionSet {

    /** The number of permission groups. This is user, group and world. */
    private static final int MODE_GROUPS = 3;
    /** The bits in a group. This is read, write, and execute. */
    private static final int GROUP_BITS = 3;

    /** The target to copy to. */
    @Getter
    @Setter
    @NotNull
    private String to;

    /** The name of the owner to assign the created file / directory to. */
    @Getter
    @Setter
    @Size(min = 1)
    private String owner;

    /** The name of the group to assign the created file / directory to. */
    @Getter
    @Setter
    @Size(min = 1)
    private String group;

    /** The UNIX permissions to assign the file / directory.
     * Has the format {@code rwxrwx---}.
     * */
    @Getter
    @Setter
    @Pattern(regexp = "([r-][w-][x-]){" + MODE_GROUPS + "}")
    private String mode;

    /** Get the file mode in octal format.
     * @return a octal format String representing the {@link #mode}.
     * */
    @Override
    public final String getOctal() {
        String myMode = getMode();
        int value = 0;
        for (int groupIndex = 0; groupIndex < MODE_GROUPS; groupIndex++) {
            int offset = GROUP_BITS * groupIndex;
            for (int bit = 0; bit < GROUP_BITS; bit++) {
                char c = myMode.charAt(offset + bit);
                int bitValue = 2 - bit;
                if (c != '-') {
                    value |= (1 << bitValue) << (GROUP_BITS * (2 - groupIndex));
                }
            }
        }
        return String.format("%04o", value);
    }
}
