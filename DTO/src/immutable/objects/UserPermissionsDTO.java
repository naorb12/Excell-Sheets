package immutable.objects;

import engine.permission.UserPermissions;
import engine.permission.property.PermissionStatus;
import engine.permission.property.PermissionType;

public class UserPermissionsDTO {
    private String userName;
    private PermissionType permissionType;
    private PermissionStatus permissionStatus;

    private PermissionType lastApprovedPermissionType;

    public UserPermissionsDTO(UserPermissions userPermissions) {
        userName = userPermissions.getUserName();
        permissionType = userPermissions.getUserPermissionType();
        permissionStatus = userPermissions.getUserPermissionStatus();
        lastApprovedPermissionType = userPermissions.getLastApprovedPermissionType();
    }

    public String getUserName() {
        return userName;
    }
    public PermissionType getPermissionType() {
        return permissionType;
    }
    public PermissionType getLastApprovedPermissionType() {
        return lastApprovedPermissionType;
    }
    public PermissionStatus getPermissionStatus() {
        return permissionStatus;
    }
}
