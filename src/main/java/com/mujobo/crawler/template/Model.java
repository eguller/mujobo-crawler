package com.mujobo.crawler.template;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: mac
 * Date: 1/29/13
 * Time: 9:52 PM
 * To change this template use File | Settings | File Templates.
 */
public class Model {
    private String name;
    private String entryPoint;
    private String jobUrl;
    private String contentSelector;
    private String titleSelector;




    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEntryPoint() {
        return entryPoint;
    }

    public void setEntryPoint(String entryPoint) {
        this.entryPoint = entryPoint;
    }

    public String getJobUrl() {
        return jobUrl;
    }

    public void setJobUrl(String jobUrl) {
        this.jobUrl = jobUrl;
    }

    public static class Data {
        private String name;
        private boolean mandatory;
        private String selector;

        public boolean getMandatory() {
            return mandatory;
        }

        public void setMandatory(boolean mandatory) {
            this.mandatory = mandatory;
        }

        public String getSelector() {
            return selector;
        }

        public void setSelector(String selector) {
            this.selector = selector;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
