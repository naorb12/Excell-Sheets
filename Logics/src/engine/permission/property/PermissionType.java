package engine.permission.property;

public enum PermissionType {
    OWNER(String.class) ,
    WRITER(String.class) ,
    READER(String.class) ;


    private Class<?> type;

    PermissionType(Class<?> type) {
        this.type = type;
    }

    public static PermissionType convertStringToType(String type) {
        if(type.equalsIgnoreCase("Writer")){
            return WRITER;
        }
        else if(type.equalsIgnoreCase("Reader")){
            return READER;
        }
        return OWNER;
    }

    @Override
    public String toString() {
        return name().substring(0, 1).toUpperCase() + name().substring(1).toLowerCase();
    }
}
