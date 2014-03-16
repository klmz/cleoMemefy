package me.magnet.magneto.plugins;

import lombok.Data;
import org.json.simple.JSONObject;

/**
 * Created by joost on 15/03/14.
 */
@Data
public class Meme {
    public String id;
    public String name;
    public String url;
    public String width;
    public String height;
}
