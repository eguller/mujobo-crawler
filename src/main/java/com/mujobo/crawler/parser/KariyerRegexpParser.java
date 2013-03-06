package com.mujobo.crawler.parser;

import com.mujobo.crawler.model.JobData;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: mac
 * Date: 1/31/13
 * Time: 1:44 PM
 * To change this template use File | Settings | File Templates.
 */
public class KariyerRegexpParser implements Parser {
    public JobData parse(Document doc) {
        return null;
    }

    public static void main(String[] args){
        //KariyerRegexpParser regexpParser = KariyerRegexpParser();
        //String patternJobTitle = "(.*)[\\s|\u00A0]\\(Ref.*Updated Date:[\\s| ](\\d{2}\\.\\d{2}.\\d{4})[\\s| ]{2}City\\/Country:[\\s| ](.*)[\\s|\u00A0]{2}Number Of Personnel[\\s| ]{2}[\\d+|-][\\s| ](.*)General Qualifications:(.*)";
    	String patternJobTitle = "(\\d{2}\\.\\d{2}.\\d{4})";
    	Document doc = null;
        try {
            doc = Jsoup.connect("http://www.kariyer.net/is-ilani/orga-systems/senior-consultant-is-ilani/943209/").get();
            Element root = doc.select("div#divIlanDetay").first();
            String element = root.text();
            Pattern pattern = Pattern.compile(patternJobTitle);
            Matcher matcher = pattern.matcher(element);
            if(matcher.find()){
                System.out.println(matcher.group(1));
                //System.out.println(matcher.group(2));
                //System.out.println(matcher.group(3));
            }

        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

    }
}

