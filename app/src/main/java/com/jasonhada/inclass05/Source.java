package com.jasonhada.inclass05;

public class Source {

    String id, name;

    public Source() {
    }

    public Source(String id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public String toString() {
        return "Source{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
