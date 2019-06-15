package d.com.flucca.missedconnection.Models;

import java.util.Date;

public class Connection {
    private String Id;
    private String UserGrantedId;
    private String UserWhoGrantsId;
    private Date date;
    private String Alias;

    public String getAlias() {
        return Alias;
    }

    public void setAlias(String alias) {
        Alias = alias;
    }

    public Connection(){

    }

    public Connection(String Id, String UserGrantedId, String UserWhoGrantsId, Date Date,String Alias){
        this.setId(Id);
        this.setUserGrantedId(UserGrantedId);
        this.setUserWhoGrantsId(UserWhoGrantsId);
        this.setDate(Date);
        this.setAlias(Alias);
    }

    public String getId() {
        return Id;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void setId(String id) {
        Id = id;
    }

    public String getUserGrantedId() {
        return UserGrantedId;
    }

    public void setUserGrantedId(String userGranted) {
        UserGrantedId = userGranted;
    }

    public String getUserWhoGrantsId() {
        return UserWhoGrantsId;
    }

    public void setUserWhoGrantsId(String userWhoGrants) {
        UserWhoGrantsId = userWhoGrants;
    }
}
