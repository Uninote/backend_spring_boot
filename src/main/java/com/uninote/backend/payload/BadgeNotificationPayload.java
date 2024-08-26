package com.uninote.backend.payload;

public class BadgeNotificationPayload {
    private Long notificationId;
    private Long badgeId;

    public BadgeNotificationPayload(Long notificationId, Long badgeId) {
        this.notificationId = notificationId;
        this.badgeId = badgeId;
    }

    public Long getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(Long notificationId) {
        this.notificationId = notificationId;
    }

    public Long getBadgeId() {
        return badgeId;
    }

    public void setBadgeId(Long badgeId) {
        this.badgeId = badgeId;
    }

    @Override
    public String toString() {
        return "BadgeNotificationPayload{" +
                "notificationId=" + notificationId +
                ", badgeId=" + badgeId +
                '}';
    }
}
