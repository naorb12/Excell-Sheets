package engine.permission;

import engine.permission.property.PermissionStatus;
import engine.permission.property.PermissionType;

public class UserPermissions {
    private String userName;
    private PermissionType permissionType;
    private PermissionStatus permissionStatus;

    public UserPermissions(String userName, PermissionType permissionType, PermissionStatus permissionStatus) {
        this.userName = userName;
        this.permissionType = permissionType;
        this.permissionStatus = permissionStatus;
    }

    public void setUserPermissionTypeAndStatus(PermissionType permissionType, PermissionStatus permissionStatus) {
        this.permissionType = permissionType;
        this.permissionStatus = permissionStatus;
    }


    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public PermissionType getUserPermissionType() {
        return permissionType;
    }

    public void setUserPermissionType(PermissionType permissionType) {
        this.permissionType = permissionType;
    }

    public PermissionStatus getUserPermissionStatus() {
        return permissionStatus;
    }

    public void setUserPermissionStatus(PermissionStatus permissionStatus) {
        this.permissionStatus = permissionStatus;
    }
}
