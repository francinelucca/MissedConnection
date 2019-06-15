package d.com.flucca.missedconnection.Models;

import java.util.HashMap;

public class User {
    private String Id;
    private String UserId;
    private String DisplayName;
    private String DisplayImageURL;
    private String RealName;
    private String ContactMail;
    private String Instagram;
    private String FacebookName;
    private String ContactPhone1;
    private String ContactPhone2;
    private String WebLink;
    private String TwitterName;
    private HashMap<String,Connection> Connections;

    public HashMap<String, Connection> getConnections() {
        return Connections == null ? new HashMap<String, Connection>():Connections;
    }

    public void setConnections(HashMap<String, Connection> connectinos) {
        Connections = connectinos;
    }

    public User(){

    }

    public User(String Id, String UserId, String DisplayName, String DisplayImageURL, String RealName, String ContactMail,
                String Instagram, String FacebookName, String ContactPhone1, String ContactPhone2,String WebLink, String TwitterName){
        this.setId(Id);
        this.setUserId(UserId);
        this.setDisplayName(DisplayName);
        this.setDisplayImageURL(DisplayImageURL);
        this.setRealName(RealName);
        this.setContactMail(ContactMail);
        this.setInstagram(Instagram);
        this.setFacebookName(FacebookName);
        this.setContactPhone1(ContactPhone1);
        this.setContactPhone2(ContactPhone2);
        this.setWebLink(WebLink);
        this.setTwitterName(TwitterName);
        this.setConnections(new HashMap<String, Connection>());
    }

    public String getRealName() {
        return RealName;
    }

    public void setRealName(String realName) {
        RealName = realName;
    }

    public String getContactMail() {
        return ContactMail;
    }

    public void setContactMail(String contactMail) {
        ContactMail = contactMail;
    }

    public String getInstagram() {
        return Instagram;
    }

    public void setInstagram(String instagram) {
        Instagram = instagram;
    }

    public String getFacebookName() {
        return FacebookName;
    }

    public void setFacebookName(String facebookName) {
        FacebookName = facebookName;
    }

    public String getContactPhone1() {
        return ContactPhone1;
    }

    public void setContactPhone1(String contactPhone1) {
        ContactPhone1 = contactPhone1;
    }

    public String getContactPhone2() {
        return ContactPhone2;
    }

    public void setContactPhone2(String contactPhone2) {
        ContactPhone2 = contactPhone2;
    }

    public String getWebLink() {
        return WebLink;
    }

    public void setWebLink(String webLink) {
        WebLink = webLink;
    }

    public String getTwitterName() {
        return TwitterName;
    }

    public void setTwitterName(String twitterName) {
        TwitterName = twitterName;
    }


    public String getDisplayName() {
        return DisplayName;
    }

    public void setDisplayName(String displayName) {
        DisplayName = displayName;
    }

    public String getDisplayImageURL() {
        return DisplayImageURL;
    }

    public void setDisplayImageURL(String displayImageURL) {
        DisplayImageURL = displayImageURL;
    }

    public String getId() {
        return Id;
    }

    public void setId(String id) {
        Id = id;
    }

    public String getUserId() {
        return UserId;
    }

    public void setUserId(String userId) {
        UserId = userId;
    }

}
