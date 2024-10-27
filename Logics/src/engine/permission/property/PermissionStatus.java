package engine.permission.property;

public enum PermissionStatus {
    APPROVED(String.class) ,
    REJECTED(String.class) ,
    PENDING(String.class) ;


    private Class<?> type;

    PermissionStatus(Class<?> type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return name().substring(0, 1).toUpperCase() + name().substring(1).toLowerCase();
    }
}
