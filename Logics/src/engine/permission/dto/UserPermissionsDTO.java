package engine.permission.dto;

import engine.permission.UserPermissions;
import engine.permission.property.PermissionStatus;
import engine.permission.property.PermissionType;

public class UserPermissionsDTO {
    private String userName;
    private PermissionType permissionType;
    private PermissionStatus permissionStatus;

    public UserPermissionsDTO(UserPermissions userPermissions) {
        userName = userPermissions.getUserName();
        permissionType = userPermissions.getUserPermissionType();
        permissionStatus = userPermissions.getUserPermissionStatus();
    }

    public String getUserName() {
        return userName;
    }
    public PermissionType getPermissionType() {
        return permissionType;
    }
    public PermissionStatus getPermissionStatus() {
        return permissionStatus;
    }
}
