/*
 * Serposcope - SEO rank checker https://serposcope.serphacker.com/
 *
 * Copyright (c) 2016 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */
package com.serphacker.serposcope.models.google;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GoogleRank {

    public final static int UNRANKED = Short.MAX_VALUE;
    private final static Logger LOG = LoggerFactory.getLogger(GoogleRank.class);

    public final int runId;
    public final int groupId;
    public final int googleTargetId;
    public final int googleSearchId;
    public final short rank;
    public final short previousRank;
    public final short diff;
    public final String url;

    public GoogleRank(int runId, int groupId, int googleTargetId, int googleSearchId, int rank, int previousRank, String url) {
        if(previousRank == 0){
            previousRank = GoogleRank.UNRANKED;
        }
        if(rank == 0){
            rank = GoogleRank.UNRANKED;
        }
        this.runId = runId;
        this.groupId = groupId;
        this.googleTargetId = googleTargetId;
        this.googleSearchId = googleSearchId;
        this.rank = (short)rank;
        this.previousRank = (short)previousRank;
        this.diff = (short)(rank - previousRank);
        try {
            url = URLDecoder.decode(url, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            ex.printStackTrace();
            LOG.error(String.format(
                    "url deocode exception occuurred. runId:[%d] groupId:[%d] googleTargetId[%d] googleSearchId[%d]",
                    runId, groupId, googleTargetId, googleSearchId), ex);
        }
        if(url != null && url.length() >= 256){
            url = url.substring(0, 256);
        }
        this.url = url;
    }

    public String getDisplayDiff(){
        if(previousRank == UNRANKED && rank != UNRANKED){
            return "in";
        }
        if(previousRank != UNRANKED && rank == UNRANKED){
            return "out";
        }
        int diff = previousRank - rank;
        if(diff == 0){
            return "=";
        }
        if(diff > 0){
            return "+" + diff;
        }
        return Integer.toString(diff);
    }

}
