package de.sfuhrm.capsula.yaml.command;

/** A tuple of permissions and user / group.
 * All methods can return {@code null}.
 * */
public interface PermissionSet {
    /** Get the file mode in octal format.
     * @return a octal format String representing the {@link #getMode()}.
     * */
    String getOctal();

    /** The name of the owner to assign the created file / directory to.
     * @return the textual owner name, for example {@code "joe"}.
     * */
    String getOwner();

    /** The name of the group to assign the created file / directory to.
     * @return the textual group name, for example {@code "users"}.
     * */
    String getGroup();

    /** The UNIX permissions to assign the file / directory.
     * Has the format {@code rwxrwx---}.
     * @return the mode in UNIX bit notation.
     * */
    String getMode();
}
