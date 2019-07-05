package com.example.naderwalid.iugchatbot;

class MemberData {
    private String name;
    private String color;

    public MemberData(String name, String color) {
        this.name = name;
        this.color = color;
    }

    public MemberData() {
    }
    public String getName() {
        return name;
    }

    public String getColor() {
        return color;
    }
}
