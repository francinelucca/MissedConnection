package d.com.flucca.missedconnection.Models;

import java.util.Date;

public class PostComment {
    private String Id;
    private String UserId;
    private String PostId;
    private String Comment;
    private Date CommentDate;
    private String Author;

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

    public PostComment(){

    }
    public PostComment(String Id,String UserId, String PostId, String Comment, Date CommentDate, String Author){
        this.setId(Id);
        this.setUserId(UserId);
        this.setPostId(PostId);
        this.setComment(Comment);
        this.setCommentDate(CommentDate);
        this.setAuthor(Author);
    }

    public String getUserId() {
        return UserId;
    }

    public void setUserId(String userId) {
        UserId = userId;
    }

    public String getPostId() {
        return PostId;
    }

    public void setPostId(String postId) {
        PostId = postId;
    }

    public String getComment() {
        return Comment;
    }

    public void setComment(String comment) {
        Comment = comment;
    }

    public Date getCommentDate() {
        return CommentDate;
    }

    public void setCommentDate(Date commentDate) {
        CommentDate = commentDate;
    }
}
