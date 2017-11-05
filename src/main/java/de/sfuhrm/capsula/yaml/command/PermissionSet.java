package de.sfuhrm.capsula.yaml.command;

/** A tuple of permissions and user / group.
 * All methods can return {@code null}.
 * */
public interface PermissionSet {
    String getOctal();

    String getOwner();

    String getGroup();

    String getMode();
}
