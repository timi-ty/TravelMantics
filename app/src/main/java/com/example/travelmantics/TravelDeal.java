package com.example.travelmantics;


import java.io.Serializable;

public class TravelDeal implements Serializable{
    private String id;
    private String title;
    private String price;
    private String description;
    private String imgRef;

    public TravelDeal(){}

    TravelDeal(String title, String price, String description) {
        this.title = title;
        this.price = price;
        this.description = description;
    }

    String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getImgRef() {
        return imgRef;
    }

    public void setImgRef(String imgUri) {
        this.imgRef = imgUri;
    }
}
