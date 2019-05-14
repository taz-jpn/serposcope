/* 
 * Serposcope - SEO rank checker https://serposcope.serphacker.com/
 * 
 * Copyright (c) 2016 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */
package com.serphacker.serposcope.scraper.google;

import java.util.Objects;
import java.util.Random;


public class GoogleScrapSearch {
    
    private final static Random random = new Random();
    private String userAgentDesktop;
    private String userAgentMobile;
    private String serpsSelectorDesktop = "#ires .srg div:not(#imagebox_bigimages).g > div > div.rc > div.r";
    private String serpsSelectorMobile = "#rso > div.srg a.C8nzq.BmP5tf, #rso > div.srg a.C8nzq.JTuIPc, #rso > div.srg a.sXtWJb";

    public GoogleScrapSearch() {
    }
    
    int resultPerPage = 10;
    int pages = 5;
    long minPauseBetweenPageMS = 0l;
    long maxPauseBetweenPageMS = 0l;
    String keyword;
    String tld = "com";
    String datacenter;
    GoogleDevice device = GoogleDevice.DESKTOP;
    String local;
    String customParameters;

    public int getResultPerPage() {
        return resultPerPage;
    }

    public void setResultPerPage(int resultPerPage) {
        this.resultPerPage = resultPerPage;
    }

    public String getUserAgentDesktop() { return this.userAgentDesktop; }

    public void setUserAgentDesktop(String value) {
        this.userAgentDesktop = value;
    }

    public String getUserAgentMobile() { return this.userAgentMobile; }

    public void setUserAgentMobile(String value) {
        this.userAgentMobile = value;
    }

    public int getPages() {
        return pages;
    }

    public void setPages(int pages) {
        this.pages = pages;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getTld() {
        return tld;
    }

    public void setTld(String tld) {
        this.tld = tld;
    }

    public String getDatacenter() {
        return datacenter;
    }

    public void setDatacenter(String datacenter) {
        this.datacenter = datacenter;
    }

    public GoogleDevice getDevice() {
        return device;
    }

    public void setDevice(GoogleDevice device) {
        this.device = device;
    }

    public String getLocal() {
        return local;
    }

    public void setLocal(String local) {
        this.local = local;
    }

    public String getCustomParameters() {
        return customParameters;
    }

    public void setCustomParameters(String customParameters) {
        this.customParameters = customParameters;
    }

    public String getSerpsSelectorDesktop() { return this.serpsSelectorDesktop; }

    public void setSerpsSelectorDesktop(String value) {
        this.serpsSelectorDesktop = value;
    }

    public String getSerpsSelectorMobile() { return this.serpsSelectorMobile; }

    public void setSerpsSelectorMobile(String value) {
        this.serpsSelectorMobile = value;
    }

    public void setPagePauseMS(long minMs, long maxMs){
        if(minMs > maxMs){
            throw new IllegalArgumentException("minMs > maxMs");
        }
        minPauseBetweenPageMS = minMs;
        maxPauseBetweenPageMS = maxMs;
    }
    
    public long getRandomPagePauseMS(){
        if(minPauseBetweenPageMS == maxPauseBetweenPageMS){
            return maxPauseBetweenPageMS;
        }
        return minPauseBetweenPageMS + Math.abs(random.nextLong()%(maxPauseBetweenPageMS-minPauseBetweenPageMS));
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + this.resultPerPage;
        hash = 79 * hash + this.pages;
        hash = 79 * hash + (int) (this.minPauseBetweenPageMS ^ (this.minPauseBetweenPageMS >>> 32));
        hash = 79 * hash + (int) (this.maxPauseBetweenPageMS ^ (this.maxPauseBetweenPageMS >>> 32));
        hash = 79 * hash + Objects.hashCode(this.keyword);
        hash = 79 * hash + Objects.hashCode(this.tld);
        hash = 79 * hash + Objects.hashCode(this.datacenter);
        hash = 79 * hash + (this.device == null ? 0 : (this.device.ordinal()+1) );
        hash = 79 * hash + Objects.hashCode(this.local);
        hash = 79 * hash + Objects.hashCode(this.customParameters);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final GoogleScrapSearch other = (GoogleScrapSearch) obj;
        if (this.resultPerPage != other.resultPerPage) {
            return false;
        }
        if (this.pages != other.pages) {
            return false;
        }
        if (this.minPauseBetweenPageMS != other.minPauseBetweenPageMS) {
            return false;
        }
        if (this.maxPauseBetweenPageMS != other.maxPauseBetweenPageMS) {
            return false;
        }
        if (!Objects.equals(this.keyword, other.keyword)) {
            return false;
        }
        if (!Objects.equals(this.tld, other.tld)) {
            return false;
        }
        if (!Objects.equals(this.datacenter, other.datacenter)) {
            return false;
        }
        if (!Objects.equals(this.local, other.local)) {
            return false;
        }
        if (!Objects.equals(this.customParameters, other.customParameters)) {
            return false;
        }
        if (this.device != other.device) {
            return false;
        }
        return true;
    }
    
}