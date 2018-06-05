package com.accelerate.dynamicpermissions;

public class PermissionInfo {

    private String mName;
    private String mShortName;

    PermissionInfo(String name) {
        this.mName = name;
        this.mShortName = name.substring(name.lastIndexOf(".") + 1);
    }

    public String getName() {
        return mName;
    }


    public void setName(String mName) {
        this.mName = mName;
    }

    @SuppressWarnings("unused")
    public String getShortName() {
        return mShortName;
    }

    @Override
    public String toString() {
        return "PermissionInfo{" +
                "mName='" + mName + '\'' +
                ", mShortName='" + mShortName + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PermissionInfo that = (PermissionInfo) o;

        return mName.equals(that.mName);

    }

    @Override
    public int hashCode() {
        return mName.hashCode();
    }

}
