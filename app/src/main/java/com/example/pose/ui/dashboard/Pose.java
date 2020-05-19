package com.example.pose.ui.dashboard;

public class Pose {
    private String name;
    private String txt;
    private int pic;

    public Pose() {
    }

    public Pose(String name, String txt, int pic) {
        this.name = name;
        this.txt = txt;
        this.pic = pic;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTxt() {
        return txt;
    }

    public void setTxt(String txt) {
        this.txt = txt;
    }

    public int getPic() {
        return pic;
    }

    public void setPic(int pic) {
        this.pic = pic;
    }

}
