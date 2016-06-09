package app.com.example.mubingliu.intrepidcheckin;

/**
 * Created by mubingliu on 6/9/16.
 * Construct JSON object
 */
public class SlackJsonString {
    //create key strings
    public String text;
    public String username;
    //argument should be text sent to the slack channel
    public SlackJsonString (String value) {
        this.username = "incoming-webhook-Mubing";
        this.text = value;
    }
}
