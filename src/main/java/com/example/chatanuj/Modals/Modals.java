package com.example.chatanuj.Modals;

public class Modals {
    private String username;
    private String profileImageUrl;
    private String message;
    private String email;
    private String UID;
    private String TimesStamp; // Keep this as long
    private Integer messageCount;

    public Modals() {
        // Default constructor required for calls to DataSnapshot.getValue(Modals.class)
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public String getTimesStamp() {
        return TimesStamp;
    }

    public void setTimesStamp(String timesStamp) {
        TimesStamp = timesStamp;
    }

    public String getUID() {
        return UID;
    }

    public void setUID(String UID) {
        this.UID = UID;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Integer getMessageCount() {
        return messageCount;
    }

    public void setMessageCount(Integer messageCount) {
        this.messageCount = messageCount;
    }

    public Modals(String email, Integer messageCount, String timesStamp, String UID, String username, String profileImageUrl, String message) {
        this.email = email;
        this.messageCount = messageCount;
        TimesStamp = timesStamp;
        this.UID = UID;
        this.username = username;
        this.profileImageUrl = profileImageUrl;
        this.message = message;









    }
}
