package com.mujobo.crawler.parser;

import com.mujobo.crawler.crawlers.KariyerIndexer;
import com.mujobo.crawler.model.JobData;
import org.jsoup.Jsoup;
import org.jsoup.helper.StringUtil;
import org.jsoup.nodes.*;
import org.jsoup.select.Elements;
import org.jsoup.select.NodeTraversor;
import org.jsoup.select.NodeVisitor;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: mac
 * Date: 1/30/13
 * Time: 2:47 PM
 * To change this template use File | Settings | File Templates.
 */
public class KariyerParser implements Parser{
    public static final DateFormat df = new SimpleDateFormat("dd.mm.yyyy");
    private static final String SOURCE = "kariyer.net";
    static Pattern extractJobTitle = Pattern.compile("(,\\s)(.+?)(i\\D\\silan)");
    public JobData parse(Document doc) {

        String keyword = doc.select("meta[name=keywords]").first().attr("content");
        String[] keywordArr = keyword.split(",");

        String uniqueId = extractUniqueId(doc);
        String companyName = keywordArr[0];
        String jobTitle = keywordArr[1];
        List<String> cityList = extractCities(doc);

        Date date = extractDate(doc);
        String summary =  summary(doc);
        JobData jobData = null;
        try{
            jobData = new JobData(trim(SOURCE),trim(uniqueId),trim(companyName),trim(jobTitle),cityList,trim(summary),date,trim(doc.baseUri()), "");
        }
        catch (Exception ex){
            System.err.println(doc.baseUri() + "  , " + ex.getMessage());
        }
        return jobData;  //To change body of implemented methods use File | Settings | File Templates.
    }
    
    public static String trim(String s){
    	if(s != null){
    		return s.trim();
    	}
    	return null;
    }
    


    private String extractUniqueId(Document doc){
    	String uniqueId = null;
    	Element uniqueIdElement = doc.getElementById("hdnilankodu");
    	if(uniqueIdElement != null){
    		uniqueId = doc.getElementById("hdnilankodu").val();
    	}
        return uniqueId;
    }

    private String summary(Document doc){
        String summary = "";
        Element element =  doc.select("#divIlanDetay").first();
        if(element != null){
            summary = element.text();
        }
        
        return summary;
    }

    public static String getPlainHtml(Element element) {
        FormattingVisitor formatter = new FormattingVisitor();
        NodeTraversor traversor = new NodeTraversor(formatter);
        traversor.traverse(element); // walk the DOM, and call .head() and .tail() for each node

        return formatter.toString();
    }
    
    public static String getPlainText(Element element){
    	PlainTextFormatter plainTextFormatter = new PlainTextFormatter();
    	NodeTraversor traversor = new NodeTraversor(plainTextFormatter);
    	traversor.traverse(element);
    	return plainTextFormatter.toString();
    }

    // the formatting rules, implemented in a breadth-first DOM traverse
    private static class FormattingVisitor implements NodeVisitor {
        private static final int maxWidth = 100;
        private int width = 0;
        private StringBuilder accum = new StringBuilder(); // holds the accumulated text
        private String prev = null;
        // hit when the node is first seen
        public void head(Node node, int depth) {
            String name = node.nodeName();
            if (node instanceof TextNode){
                append(((TextNode) node).text()); // TextNodes carry all user-readable text in the DOM.
            }
            else if (name.equals("li"))
                append("<li>");
            else if(name.equals("ul")){
                append("<ul>");
            }
        }

        // hit when all of the node's children (if any) have been visited
        public void tail(Node node, int depth) {
            String name = node.nodeName();
            if (StringUtil.in(name, "p", "h1", "h2", "h3", "h4", "h5", "a", "span"))
                append("<br>");
            if(name.equals("ul")){
                append("</ul>");
            }
            if(name.equals("li")){
                append("</li>");
            }
        }

        // appends text to the string builder with a simple word wrap method
        private void append(String text) {
            text = text.trim();
            if(text.length() == 0){
                return;
            }
            if(text.matches("^\\W+")){
                return;
            }

            if(prev != null && prev.equals("<br>") && text.equals("<br>")){
                return;
            }
            prev = text;
            accum.append(text);
            prev = text;

        }

        public String toString() {
            return accum.toString().trim();
        }
    }


    public List<String> extractCities(Document doc){
    	HashMap<String, String> cityMap = new HashMap<String, String>();
    	cityMap.put("Adana", "Adana");
    	cityMap.put("Konya", "Konya");
    	cityMap.put("Tekirda\u011f", "Tekirda\u011f");
    	//\u011f
        List<String> cityList = new ArrayList<String>();
        Element ilanDetay = doc.select("div#divIlanDetay").first();
        String patternJobTitle = ".*(\u015eehir/\u00dclke|City/Country|Location).*";
    	Pattern pattern = Pattern.compile(patternJobTitle);
        Matcher matcher = pattern.matcher(getPlainText(ilanDetay));
        if(matcher.find()){
        	String cityLine = matcher.group();
        	String[] cityLineArr = cityLine.split(":");
        	if(cityLineArr.length > 1){
        		String cityCommaStr = cityLineArr[1].trim();
        		String[] cityArr = cityCommaStr.split(" ")[0].split(",");
        		for(String city : cityArr){
        			cityList.add(trim(city));
        		}
        	}
        }
        if(cityList.size() == 0){
        	Set<String> tokenSet = tokenize(doc.text());
        	for(String s : tokenSet){
        		if(cityMap.containsKey(s)){
        			cityList.add(trim(cityMap.get(s)));
        		}
        	}
        }
        return cityList;
    }
    
    public Set<String> tokenize(String str){
    	Set<String> tokenSet = new HashSet<String>();
    	StringTokenizer st2 = new StringTokenizer(str," ,:'.-\n\r\t");
    	 
		while (st2.hasMoreElements()) {
			tokenSet.add(trim(st2.nextToken()));
		}
		return tokenSet;
    }

    public Date extractDate(Document doc){
    	Date date = null;
    	String patternJobTitle = "(\\d{2}\\.\\d{2}.\\d{4})";
    	Element root = doc.select("div#divIlanDetay").first();
    	if(root == null){
    		return null;
    	}
    	String element = root.text();
        Pattern pattern = Pattern.compile(patternJobTitle);
        Matcher matcher = pattern.matcher(element);
        if(matcher.find()){
        	try {
				date = df.parse(matcher.group(1));
				return date;
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
        return date;
    }
    
    private static class PlainTextFormatter implements NodeVisitor {
        private static final int maxWidth = 80;
        private int width = 0;
        private StringBuilder accum = new StringBuilder(); // holds the accumulated text

        // hit when the node is first seen
        public void head(Node node, int depth) {
            String name = node.nodeName();
            if (node instanceof TextNode)
                append(((TextNode) node).text()); // TextNodes carry all user-readable text in the DOM.
            else if (name.equals("li"))
                append("\n * ");
        }

        // hit when all of the node's children (if any) have been visited
        public void tail(Node node, int depth) {
            String name = node.nodeName();
            if (name.equals("br"))
                append("\n");
            else if (StringUtil.in(name, "p", "h1", "h2", "h3", "h4", "h5"))
                append("\n\n");
            else if (name.equals("a"))
                append(String.format(" <%s>", node.absUrl("href")));
        }

        // appends text to the string builder with a simple word wrap method
        private void append(String text) {
            if (text.startsWith("\n"))
                width = 0; // reset counter if starts with a newline. only from formats above, not in natural text
            if (text.equals(" ") &&
                    (accum.length() == 0 || StringUtil.in(accum.substring(accum.length() - 1), " ", "\n")))
                return; // don't accumulate long runs of empty spaces

            if (text.length() + width > maxWidth) { // won't fit, needs to wrap
                String words[] = text.split("\\s+");
                for (int i = 0; i < words.length; i++) {
                    String word = words[i];
                    boolean last = i == words.length - 1;
                    if (!last) // insert a space if not the last word
                        word = word + " ";
                    if (word.length() + width > maxWidth) { // wrap and reset counter
                        accum.append("\n").append(word);
                        width = word.length();
                    } else {
                        accum.append(word);
                        width += word.length();
                    }
                }
            } else { // fits as is, without need to wrap text
                accum.append(text);
                width += text.length();
            }
        }

        public String toString() {
            return accum.toString();
        }
    }

    public static String extract(String input, Pattern pattern){
        Matcher matcher = pattern.matcher(input);
        while(matcher.find()){
            System.out.println(matcher.group(1));
        }
        return null;
    }



    public static void main(String[] args){
    	KariyerParser parser = new KariyerParser();
        try {
        	Document doc = Jsoup.connect("http://www.kariyer.net/is-ilani/manpower-bakirkoy/kimya-muhendisleri-corlu-is-ilani/942878/").get();
        	//String text = KariyerParser.getPlainText(doc.select("div#divIlanDetay").first());
        	parser.extractCities(doc);
    //    	System.out.println(text);
        	
           // JobData jobData = parser.parse(Jsoup.connect("http://kariyer.net/is-ilani/set-yazilim/java-yazilim-muhendisi-is-ilani/877231/?tmpsno=1").get());
            //jobData.get
            //jobData.post();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }


    }
}
