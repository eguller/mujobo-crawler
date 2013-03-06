package com.mujobo.crawler.model;

import com.mujobo.crawler.parser.KariyerParser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: mac
 * Date: 1/30/13
 * Time: 2:42 PM
 * To change this template use File | Settings | File Templates.
 */
public class JobData {
    String uniqueId;
    String companyName;
    String title;
    List<String> cities = new ArrayList<String>();
    String sector;
    String description;

    Date date;
    String source;
    String url;

    public JobData(String source, String uniqueId, String companyName, String jobTitle, List<String> cityList, String description, Date date, String url, String sector) {
        if(source == null || source.length() == 0){
            throw new RuntimeException("Source is null");
        }
        this.source = source;
        if(url == null || url.length() == 0){
            throw new RuntimeException("Url is null");
        }
        this.url = url;
        if(uniqueId == null || uniqueId.length() == 0){
            throw new RuntimeException("Unique Id is null");
        }
        this.uniqueId = uniqueId;

        if(companyName == null || companyName.length() == 0){
            throw new RuntimeException("Company name is null");
        }
        this.companyName = companyName;
        if(jobTitle == null || jobTitle.length() == 0){
            throw new RuntimeException("Job title is null");
        }
        this.title = jobTitle;
        if(cityList == null || cityList.size() == 0){
            throw new RuntimeException("City list is empty");
        }
        this.cities = cityList;
        if(date == null){
            throw new RuntimeException("Date is null");
        }
        if(description == null || description.length() == 0){
        	throw new RuntimeException("Description is null");
        }
        this.date = date;
        this.description = description;
        this.sector = sector;

    }

    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("title: ").append(title).append("\n");
        sb.append("company: ").append(companyName).append("\n");
        sb.append("cities: ").append(cities).append("\n");
        sb.append("date: ").append(date).append("\n");
        sb.append("sector: ").append(sector).append("\n");
        sb.append("description: ").append(description).append("\n");
        return sb.toString();
    }

    public Map<String, String> toMap(){
        Map<String, String> jobMap = new HashMap<String, String>();
        jobMap.put("title", title);
        jobMap.put("company", companyName);
        String cityString = cities.toString();
        cityString = cityString.substring(1,cityString.length()-1);
        jobMap.put("city",cityString);
        date = date == null ? Calendar.getInstance().getTime() : date;
        jobMap.put("date",KariyerParser.df.format(date));
        jobMap.put("description", description);
        jobMap.put("uniqueId",uniqueId);
        jobMap.put("sector",sector);
        jobMap.put("source", source);
        jobMap.put("url", url);
        return jobMap;
    }

    public void post(){
        try {
            Document doc = Jsoup.connect("http://localhost:9000/crawler/api/job/add").data(toMap()).post();
            System.out.println(doc.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
