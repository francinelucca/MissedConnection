package d.com.flucca.missedconnection.Models;

public class PostInteraction {
    public static final String INTERACTION_TYPE_LIKE = "1";
    public static final String INTERACTION_TYPE_DISLIKE = "2";

    public String getId() {
        return Id;
    }

    public void setId(String id) {
        Id = id;
    }

    private String Id;
    private String UserId;
    private String PostId;
    private String InteractionType;

    public PostInteraction(){

    }

    public PostInteraction(String Id,String UserId, String PostId, String InteractionType){
        this.setId(Id);
        this.setUserId(UserId);
        this.setPostId(PostId);
        this.setInteractionType(InteractionType);
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

    public String getInteractionType() {
        return InteractionType;
    }

    public void setInteractionType(String interactionType) {
        InteractionType = interactionType;
    }
}
