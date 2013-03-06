package com.mujobo.crawler.parser;

import com.mujobo.crawler.model.JobData;
import org.jsoup.nodes.Document;

/**
 * Created with IntelliJ IDEA.
 * User: mac
 * Date: 1/30/13
 * Time: 2:45 PM
 * To change this template use File | Settings | File Templates.
 */
public interface Parser {
       public JobData parse(Document doc);
}
