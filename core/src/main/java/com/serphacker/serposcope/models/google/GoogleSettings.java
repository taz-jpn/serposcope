/* 
 * Serposcope - SEO rank checker https://serposcope.serphacker.com/
 * 
 * Copyright (c) 2016 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */
package com.serphacker.serposcope.models.google;

import com.serphacker.serposcope.scraper.google.GoogleDevice;
import java.util.Arrays;
import java.util.List;


public class GoogleSettings {
    
    int resultPerPage = 100;
    int pages = 1;
    int minPauseBetweenPageSec = 5;
    int maxPauseBetweenPageSec = 5;
    int maxThreads = 1;
    int fetchRetry = 3;    
    
    String defaultTld = "com";
    String defaultDatacenter = null;
    GoogleDevice defaultDevice = GoogleDevice.DESKTOP;
    String defaultLocal = null;
    String defaultCustomParameters = null;
    String defaultUserAgentDesktop = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_6) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/11.1.2 Safari/605.1.15";
    String defaultUserAgentMobile = "Mozilla/5.0 (iPhone; CPU iPhone OS 11_4_1 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/11.0 Mobile/15E148 Safari/604.1";
    private String defaultserpsSelectorDesktop = "#ires .srg div:not(#imagebox_bigimages).g > div > div.rc > div.r";
    private String defaultserpsSelectorMobile = "#rso > div.srg a.C8nzq.BmP5tf, #rso > div.srg a.C8nzq.JTuIPc, #rso > div.srg a.sXtWJb";

    public int getResultPerPage() {
        return resultPerPage;
    }

    public void setResultPerPage(int resultPerPage) {
        this.resultPerPage = resultPerPage;
    }

    public int getPages() {
        return pages;
    }

    public void setPages(int pages) {
        this.pages = pages;
    }

    public int getMinPauseBetweenPageSec() {
        return minPauseBetweenPageSec;
    }

    public void setMinPauseBetweenPageSec(int minPauseBetweenPageSec) {
        this.minPauseBetweenPageSec = minPauseBetweenPageSec;
    }

    public int getMaxPauseBetweenPageSec() {
        return maxPauseBetweenPageSec;
    }

    public void setMaxPauseBetweenPageSec(int maxPauseBetweenPageSec) {
        this.maxPauseBetweenPageSec = maxPauseBetweenPageSec;
    }

    public int getMaxThreads() {
        return maxThreads;
    }

    public void setMaxThreads(int maxThreads) {
        this.maxThreads = maxThreads;
    }

    public int getFetchRetry() {
        return fetchRetry;
    }

    public void setFetchRetry(int fetchRetry) {
        this.fetchRetry = fetchRetry;
    }
    
    // search
    public String getDefaultTld() {
        return defaultTld;
    }

    public void setDefaultTld(String defaultTld) {
        this.defaultTld = defaultTld;
    }

    public String getDefaultDatacenter() {
        return defaultDatacenter;
    }

    public String getDefaultUserAgentDesktop() {
        return defaultUserAgentDesktop;
    }

    public void setDefaultUserAgentDesktop(String defaultUserAgentDesktop) {
        this.defaultUserAgentDesktop = defaultUserAgentDesktop;
    }

    public String getDefaultUserAgentMobile() {
        return defaultUserAgentMobile;
    }

    public void setDefaultUserAgentMobile(String defaultUserAgentMobile) {
        this.defaultUserAgentMobile = defaultUserAgentMobile;
    }

    public void setDefaultDatacenter(String defaultDatacenter) {
        this.defaultDatacenter = defaultDatacenter;
    }

    public GoogleDevice getDefaultDevice() {
        return defaultDevice;
    }

    public void setDefaultDevice(GoogleDevice defaultDevice) {
        this.defaultDevice = defaultDevice;
    }
    
    public void setDefaultDevice(String deviceId){
        this.defaultDevice = GoogleDevice.DESKTOP;
        
        if(deviceId == null){
            return;
        }
        
        try {
            this.defaultDevice = GoogleDevice.values()[Integer.parseInt(deviceId)];
        } catch(Exception ex){
        }
    }

    public String getDefaultLocal() {
        return defaultLocal;
    }

    public void setDefaultLocal(String defaultLocal) {
        this.defaultLocal = defaultLocal;
    }

    public String getDefaultCustomParameters() {
        return defaultCustomParameters;
    }

    public void setDefaultCustomParameters(String defaultCustomParameters) {
        this.defaultCustomParameters = defaultCustomParameters;
    }

    public String getDefaultserpsSelectorDesktop() {
        return defaultserpsSelectorDesktop;
    }

    public void setDefaultserpsSelectorDesktop(String defaultserpsSelectorDesktop) {
        this.defaultserpsSelectorDesktop = defaultserpsSelectorDesktop;
    }

    public String getDefaultserpsSelectorMobile() {
        return defaultserpsSelectorMobile;
    }

    public void setDefaultserpsSelectorMobile(String defaultserpsSelectorMobile) {
        this.defaultserpsSelectorMobile = defaultserpsSelectorMobile;
    }
}
