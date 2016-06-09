package app.com.example.mubingliu.intrepidcheckin;

/**
 * Created by mubingliu on 6/9/16.
 */
public class SlackJsonString {
    public String text;
    public String username;
    public SlackJsonString (String value) {
        this.username = "incoming-webhook-Mubing";
        this.text = value;
    }
}
