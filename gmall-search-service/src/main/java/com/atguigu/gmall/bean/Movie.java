package com.atguigu.gmall.bean;

import java.util.List;

public class Movie {

    private String id;
    private String name;
    private Double doubanScore;
    private List<Actor> actorList;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getDoubanScore() {
        return doubanScore;
    }

    public void setDoubanScore(Double doubanScore) {
        this.doubanScore = doubanScore;
    }

    public List<Actor> getActorList() {
        return actorList;
    }

    public void setActorList(List<Actor> actorList) {
        this.actorList = actorList;
    }
}
