package d.com.flucca.missedconnection.Models;

import android.location.Location;

import org.w3c.dom.Comment;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class Post {

    private String Id;
    private String Title;
    private String DisplayImageUrl;
    private String Description;
    private Date Date;
    private Integer Likes;
    private Integer Dislikes;
    private String Author;
    private String UserId;
    private double Longitude;
    private double Latitude;
    private HashMap<String,PostInteraction> Interactions;
    private HashMap<String,PostComment> PostComments;

    public HashMap<String, PostInteraction> getInteractions() {
        return Interactions == null ? new HashMap<String, PostInteraction>(): Interactions;
    }

    public HashMap<String, PostComment> getPostComments() {
        return PostComments == null ? new HashMap<String, PostComment>(): PostComments;
    }

    public void setPostComments(HashMap<String, PostComment> postComments) {
        PostComments = postComments;
    }

    public void setInteractions(HashMap<String, PostInteraction> interactions) {
        Interactions = interactions;
    }

    public String getUserId() {
        return UserId;
    }

    public void setUserId(String userId) {
        UserId = userId;
    }

    public Post(){

    }

    public double getLongitude() {
        return Longitude;
    }

    public void setLongitude(double longitude) {
        Longitude = longitude;
    }

    public double getLatitude() {
        return Latitude;
    }

    public void setLatitude(double latitude) {
        Latitude = latitude;
    }

    public Post(String Id, String Title, String DisplayImageURL, String Description, Date Date, String Author, String UserId, Double Longitude, Double Latitude ){
        this.setId(Id);
        this.setTitle(Title);
        this.setDisplayImageUrl(DisplayImageURL);
        this.setDescription(Description);
        this.setDate(Date);
        this.setAuthor(Author);
        this.setUserId(UserId);
        this.setLongitude(Longitude);
        this.setLatitude(Latitude);
        this.setLikes(0);
        this.setDislikes(0);
        this.setInteractions(new HashMap<String, PostInteraction>());
        this.setPostComments(new HashMap<String, PostComment>());
    }

    public String getAuthor() {
        return Author;
    }

    public void setAuthor(String author) {
        Author = author;
    }

    public String getId() {
        return Id;
    }

    public void setId(String id) {
        Id = id;
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public String getDisplayImageUrl() {
        return DisplayImageUrl == null ? "" : DisplayImageUrl;
    }

    public void setDisplayImageUrl(String displayImageUrl) {
        DisplayImageUrl = displayImageUrl;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public Date getDate() {
        return this.Date;
    }

    public void setDate(Date fecha) {
        this.Date = fecha;
    }

    public Integer getLikes() {
        return Likes == null ? 0:Likes;
    }

    public void setLikes(Integer likes) {
        this.Likes = likes;
    }

    public Integer getDislikes() {
        return Dislikes == null ? 0: Dislikes;
    }

    public void setDislikes(Integer dislikes) {
        this.Dislikes = dislikes;
    }

    public Location getLocation(){
        if(this.getLongitude()== -1.0 || this.getLatitude() == -1.0){
            return null;
        }
        Location location = new Location("dummyprovider");
        location.setLongitude(this.Longitude);
        location.setLatitude(this.Latitude);
        return location;
    }

}
