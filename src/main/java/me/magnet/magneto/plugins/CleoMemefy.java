package me.magnet.magneto.plugins;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import me.magnet.magneto.ChatRoom;
import me.magnet.magneto.annotations.Param;
import me.magnet.magneto.annotations.RespondTo;
import me.magnet.magneto.plugins.MagnetoPlugin;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.util.*;


@Slf4j
public class CleoMemefy implements MagnetoPlugin {
    ArrayList<String> thelist = new ArrayList<>();
    private final ObjectMapper mapper = new ObjectMapper();
    enum TimeUnits{
        SECONDS,
        MINUTES,
        HOURS,
        DAYS,
        WEEKS,
        MONTHS,
        YEARS
    }

    public CleoMemefy() throws IOException {
    }

    /**
     * List memes
     */
    @RespondTo(regex = "\\b(all memes|show me all memes about|show all memes about) {q}.*")
    public void listMemes(final ChatRoom room, @Param("q") String memeTemplate)  {
        JSONObject json;
        try {
            room.sendMessage("Let me just look that up");
            List<Meme> listOfMemes = getListOfMemes(memeTemplate);
            if(listOfMemes.size()>0){
                room.sendMessage(convertListToString(listOfMemes));
            }else{
                room.sendMessage("I didn't find any memes about "+memeTemplate);
            }

        } catch (org.json.simple.parser.ParseException e) {
            e.printStackTrace();
            room.sendMessage("Sorry, the JSON couldn't be parsed");
        } catch (JsonMappingException e) {
            room.sendMessage("Sorry.. I couldn't map the json");
            e.printStackTrace();
        } catch (JsonParseException e) {
            room.sendMessage("Sorry, the JSON couldn't be parsed");
            e.printStackTrace();
        } catch (IOException e) {
            room.sendMessage("Uhura seems to be fucking shit up..");
            e.printStackTrace();
        }
    }

    private List<Meme> getListOfMemes(String memeTemplate) throws IOException, org.json.simple.parser.ParseException {
        JSONObject json = doGet("https://api.imgflip.com/get_memes");
        JSONObject data = (JSONObject) json.get("data");
        JSONArray memes = (JSONArray) data.get("memes");

        ObjectMapper mapper = new ObjectMapper();
        List<Meme> listOfMemes = new ArrayList<>();

        for (Object o : memes) {
            JSONObject jsonMeme = (JSONObject) o;
            Meme meme = mapper.readValue(jsonMeme.toJSONString(), Meme.class);

            //Search if q is not all
            if(memeTemplate.equals("all")){
                listOfMemes.add(meme);
            }else if(meme.getName().toLowerCase().contains(memeTemplate.toLowerCase())){
                listOfMemes.add(meme);
            }
        }
        return listOfMemes;
    }

    private String convertListToString(List<Meme> listOfMemes) {
        String result = "";
        for (Meme meme : listOfMemes) {
            result += meme.getId()+": "+meme.getName()+", "+meme.getUrl()+" \n";

        }
        return result;
    }

    /**
         * Make a meme based on an id
         */
    @RespondTo(regex = "\\b(make meme {id} with {line1} and {line2}).*")
    public void makeMeme(final ChatRoom room, @Param("id") String id, @Param("line1") String line1, @Param("line2") String line2) throws ParseException, IOException {
        room.sendMessage("Ok, just a sec..");

        ArrayList<PostParam> params = new ArrayList<PostParam>();
        params.add(new PostParam("template_id", id));
        params.add(new PostParam("username", "cleo"));
        params.add(new PostParam("password", "cleo2014"));
        params.add(new PostParam("text0", line1));
        params.add(new PostParam("text1", line2));

        JSONObject result = null;
        try {
            result = doPost("https://api.imgflip.com/caption_image", params);
            JSONObject data = (JSONObject) result.get("data");
            String imageUrl = (String) data.get("url");
            room.sendHtml("Ok, I made you this image: <img height='300' src='"+imageUrl+"'>");
        } catch (org.json.simple.parser.ParseException e) {
            room.sendMessage("O dear.. Something went wrong with parsing the xml..");
            e.printStackTrace();
        }

    }

//    /**
//     * alias for single lined meme
//     * @param room
//     * @param id
//     * @param line1
//     * @throws ParseException
//     * @throws IOException
//     */
//    @RespondTo(regex = "\\b(make meme {id} with {line1}).*")
//    public void makeMeme(final ChatRoom room, @Param("id") String id, @Param("line1") String line1) throws ParseException, IOException {
//        makeMeme(room,id,line1,"");
//    }

        /**
         * Make a meme based on a search query.
         */
    @RespondTo(regex = "\\b(make a {textID} meme with {line1} and {line2}).*")
    public void makeTextMeme(final ChatRoom room, @Param("textID") String id, @Param("line1") String line1, @Param("line2") String line2) throws ParseException, IOException, org.json.simple.parser.ParseException {
        room.sendMessage("Ok, I'll try to make a meme about "+id);
        List<Meme> memes = getListOfMemes(id);
        if(memes.size()>0){
            room.sendMessage("Oooh! I found one, just adding the text..");
            int index = new Random().nextInt(memes.size());
            String meme_id = memes.get(index).getId();
            ArrayList<PostParam> params = new ArrayList<PostParam>();
            params.add(new PostParam("template_id", meme_id));
            params.add(new PostParam("username", "cleo"));
            params.add(new PostParam("password", "cleo2014"));
            params.add(new PostParam("text0", line1));
            params.add(new PostParam("text1", line2));

            JSONObject result = null;
            try {
                result = doPost("https://api.imgflip.com/caption_image", params);
                JSONObject data = (JSONObject) result.get("data");
                String imageUrl = (String) data.get("url");
                room.sendHtml("Ok, I made you this image: <img height='300' src='"+imageUrl+"'>");
            } catch (org.json.simple.parser.ParseException e) {
                room.sendMessage("O dear.. Something went wrong with parsing the xml..");
                e.printStackTrace();
            }
        }else{
            room.sendMessage("Sorry.. I couldn't find any meme about "+ id);
        }


    }

//    /**
//     * alias for single lined meme
//     * @param room
//     * @param id
//     * @param line1
//     * @throws ParseException
//     * @throws IOException
//     */
//    @RespondTo(regex = "\\b(make a {textID} meme with {line1}).*")
//    public void makeTextMeme(final ChatRoom room, @Param("textID") String id, @Param("line1") String line1) throws ParseException, IOException, org.json.simple.parser.ParseException {
//        makeTextMeme(room, id, line1, "");
//    }

    private JSONObject doPost(String url, List<PostParam> params) throws IOException, org.json.simple.parser.ParseException {
        URL obj = new URL(url);
        HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("User-Agent", "Mozilla/5.0");
        con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

        String urlParams = PostParam.parseParams(params);
        con.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        wr.writeBytes(urlParams);
        wr.flush();
        wr.close();

        int responseCode = con.getResponseCode();
        log.info("Send post to: " + url);
        log.info("Response: "+ responseCode);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        //print result

        JSONObject json = (JSONObject) new JSONParser().parse(response.toString());
        return json;

    }

    // HTTP GET request
    private JSONObject doGet(String url) throws IOException, org.json.simple.parser.ParseException {


        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        // optional default is GET
        con.setRequestMethod("GET");

        //add request header
        con.setRequestProperty("User-Agent", "Mozilla/5.0");

        int responseCode = con.getResponseCode();
        log.info("\nSending 'GET' request to URL : " + url);
        log.info("Response Code : " + responseCode);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        JSONObject json = (JSONObject) new JSONParser().parse(response.toString());
        return json;

    }

    @Override
    public String getName() {
        return "Cleo Memefy";
    }
}
