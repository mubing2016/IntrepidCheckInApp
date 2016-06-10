package app.com.example.mubingliu.intrepidcheckin;


public class SlackJsonString {

    private static final String USER_NAME = "incoming-webhook-Mubing";
    public String text;
    public String username;

    public SlackJsonString (String value) {
        this.username =  USER_NAME;
        this.text = value;
    }
}
