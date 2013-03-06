package com.mujobo.crawler.model;


import javax.persistence.Column;
import javax.persistence.Entity;
import java.util.List;

@Entity
public class KariyerLink extends Model{
    @Column(unique = true)
    public String url;
    public boolean crawled = false;

}
