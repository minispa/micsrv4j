package com.github.minispa.micsrv.media.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.tuple.Pair;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class MixAll {

    private MixAll() {}

    public static Pair<Integer, Integer> scale(int x, int y, int maxX, int maxY) {
        double radiusA = new BigDecimal(x).divide(new BigDecimal(y), 9, RoundingMode.HALF_DOWN).doubleValue();
        double radiusB = new BigDecimal(maxX).divide(new BigDecimal(maxY), 9, RoundingMode.HALF_DOWN).doubleValue();

        if (radiusA > radiusB) {
            if (x > maxX) {
                y = y * maxX / x;
                x = maxX;
            }
        }
        if (radiusA < radiusB) {
            if (y > maxY) {
                x = x * maxY / y;
                y = maxY;
            }
        }
        if (radiusA == radiusB) {
            if (x > maxX) {
                x = maxX;
            }
            if (y > maxY) {
                y = maxY;
            }
        }
        return Pair.of(x, y);
    }

    public static JSONObject anyVideoStream(JSONObject streamsObj) {
        final JSONArray streams = streamsObj.getJSONArray("streams");
        for(int i = 0;  i< streams.size(); i++) {
            JSONObject stream = streams.getJSONObject(i);
            if("video".equalsIgnoreCase(stream.getString("codec_type"))) {
                return stream;
            }
        }
        return null;
    }

}
