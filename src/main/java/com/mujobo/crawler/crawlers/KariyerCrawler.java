package com.mujobo.crawler.crawlers;

import com.mujobo.crawler.model.DbJob;
import com.mujobo.crawler.model.KariyerLink;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.hibernate.exception.ConstraintViolationException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.EntityManager;

public class KariyerCrawler extends DbJob implements Runnable {
	static HttpClient client = new HttpClient();

	public KariyerCrawler(EntityManager em) {
		super(em);
	}

	public void run() {
            while(true){
                crawl();
                System.out.println("Craw Completed: " + Calendar.getInstance().getTime());
            }
	}

    public void crawl(){
        int isIlani = 0;
        int page = 1;
        List<String> urlList = getSectionUrl();
        for(String url : urlList){
            for(page = 1;;page++){
               String pageContent = null;
               if(url.contains("/mavi/")){
                   System.out.println("Mavi Yaka, Sayfa: " + page);
                   pageContent = getMaviYakaPageContent(page);
               }
               else if(url.contains("/staj/")){
                   System.out.println("Staj, Sayfa: " + page);
                   pageContent = getStajerPageContent(page);
               }
               else if(url.contains("/sektor/")){
                   System.out.println("Sektor, Sayfa: " + page);
                   String sektor = url.substring(url.lastIndexOf("/") + 1);
                   pageContent = getSektorPageContent(sektor,page);
               }
               else if(url.contains("/is-alani/")){
                   System.out.println("Is Alani, Sayfa: " + page);
                   String isalani = url.substring(url.lastIndexOf("/") + 1);
                   pageContent = getIsAlaniPageContent(isalani, page);
               }
               else if(url.contains("/sehir/")){
                   System.out.println("Sehir, Sayfa: " + page);
                   String sehir = url.substring(url.lastIndexOf("/") + 1);
                   pageContent = getSehirPageContent(sehir, page);
               }
               else{
                   System.err.println("Unknow section");
               }
               if(pageContent == null){
                   System.out.println("Page content is null");
                   break;
               }
                else if(pageContent.contains("Arad\u0131\u011f\u0131n\u0131z kriterlere uygun ilan bulunamad\u0131")){
                   break;
               }

               Pattern pageCount = Pattern.compile("<b>[0-9]+</b>");
               Matcher pageMatch = pageCount.matcher(pageContent);
               if(pageMatch.find()){
                  String numberStr = pageMatch.group();
                  int resultCount = Integer.parseInt(numberStr.replace("<b>", "").replace("</b>", ""));
                  if( (page - 1 ) * 50 > resultCount ){
                      break;
                  }

               }

               Pattern isIlaniPattern = Pattern.compile("/is-ilani/.*[\\d+]");
               Matcher matcher =  isIlaniPattern.matcher(pageContent);
                while(matcher.find()){
                    String link = matcher.group();
                    int index = link.lastIndexOf("?tmpsno");
                    if(index > -1){
                        link = link.substring(0, index);
                    }
                    KariyerLink kariyerLink = new KariyerLink();
                    kariyerLink.crawled = false;
                    kariyerLink.url = "http://www.kariyer.net" + link;
                    try{
                    	save(kariyerLink);
                    }
                    catch (Exception e) {
                    	if(!e.getMessage().contains("Unique index or primary key violation")){
                    		e.printStackTrace();
                    	}
					}
                    
                }
            }
        }
    }

    public static List<String> getSectionUrl(){
        Pattern isAramaPattern = Pattern.compile("/is-arama/.*[\\d+]");
        List<String> urlList = new ArrayList<String>();
        GetMethod get = new GetMethod("http://kariyer.net/website/index.aspx");
        try {
            client.executeMethod(get);
            String mainPage = get.getResponseBodyAsString();
            Matcher m = isAramaPattern.matcher(mainPage);
            while(m.find()){
                urlList.add("http://kariyer.net" + m.group());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        urlList.add("http://kariyer.net/is-ariyorum/yeni/Yeni-is-ilanlari/1g");
        urlList.add("http://kariyer.net/is-istiyorum/guncel/Guncel-is-ilanlari/1g");
        return urlList;
    }

	public static String getIsAlaniPageContent(String isalani, int sayfa) {
		PostMethod post = new PostMethod(
				"http://kariyer.net/website/isarama/getIlanlar.aspx");
		post.addParameter("isalani", isalani);
		post.addParameter("sayfano", String.valueOf(sayfa));
        post.addParameter("siralama", "1");
        post.addParameter("sonarama", "0");
		try {
			client.executeMethod(post);
			return post.getResponseBodyAsString();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

    public static String getSektorPageContent(String sektor, int sayfa){
        PostMethod post = new PostMethod(
                "http://kariyer.net/website/isarama/getIlanlar.aspx");
        post.addParameter("fsektor", sektor);
        post.addParameter("sayfano", String.valueOf(sayfa));
        post.addParameter("siralama", "1");
        post.addParameter("sonarama", "0");
        try {
            client.executeMethod(post);
            return post.getResponseBodyAsString();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    public static String getSehirPageContent(String sehirid, int sayfa){
        PostMethod post = new PostMethod(
                "http://kariyer.net/website/isarama/getIlanlar.aspx");
        post.addParameter("sehirid", sehirid);
        post.addParameter("sayfano", String.valueOf(sayfa));
        post.addParameter("siralama", "1");
        post.addParameter("sonarama", "0");
        try {
            client.executeMethod(post);
            return post.getResponseBodyAsString();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    public static String getMaviYakaPageContent(int sayfa){
        PostMethod post = new PostMethod(
                "http://kariyer.net/website/isarama/getIlanlar.aspx");
        post.addParameter("tarih", "1g");
        post.addParameter("sadeceyeni", "1");
        post.addParameter("siralama", "1");
        post.addParameter("sonarama", "0");
        post.addParameter("sayfano", String .valueOf(sayfa));
        try {
            client.executeMethod(post);
            return post.getResponseBodyAsString();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    public static String getStajerPageContent(int sayfa){
        PostMethod post = new PostMethod(
                "http://kariyer.net/website/isarama/getIlanlar.aspx");
        post.addParameter("pozisyonseviyesi", "8");
        post.addParameter("siralama", "1");
        post.addParameter("sonarama", "0");
        post.addParameter("sayfano", String .valueOf(sayfa));
        try {
            client.executeMethod(post);
            return post.getResponseBodyAsString();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

	public static void main(String[] args) {
		//crawl();
	}
}
