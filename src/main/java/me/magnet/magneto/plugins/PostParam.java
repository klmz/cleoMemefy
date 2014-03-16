package me.magnet.magneto.plugins;

import lombok.Data;

import java.net.URLEncoder;
import java.util.List;

/**
 * Created by joost on 15/03/14.
 */
@Data
public class PostParam {
    public final String key;
    public final String value;
    public static String parseParams(List<PostParam> params){
        String result = "";
        for (PostParam param : params) {
            result += URLEncoder.encode(param.getKey())+"="+ URLEncoder.encode(param.getValue()) + "&";

        }
        return result.substring(0,result.length()-1);
    }
}
