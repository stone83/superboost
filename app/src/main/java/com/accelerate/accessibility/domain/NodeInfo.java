package com.accelerate.accessibility.domain;

public class NodeInfo {

    public String className;
    public String text;
    public String contentDescription;

    public NodeInfo() {

    }

    public NodeInfo(String className, String text, String contentDescription) {
        this.className = className;
        this.text = text;
        this.contentDescription = contentDescription;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NodeInfo nodeInfo = (NodeInfo) o;

        return className != null ? className.equals(nodeInfo.className) : nodeInfo.className == null
                && (text != null ? text.equals(nodeInfo.text) : nodeInfo.text == null
                && (contentDescription != null ? contentDescription
                .equals(nodeInfo.contentDescription) : nodeInfo.contentDescription == null));
    }

    @Override
    public int hashCode() {
        int result = className != null ? className.hashCode() : 0;
        result = 31 * result + (text != null ? text.hashCode() : 0);
        result = 31 * result + (contentDescription != null ? contentDescription.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "NodeInfo{" +
                "className='" + className + '\'' +
                ", text='" + text + '\'' +
                '}';
    }

}
