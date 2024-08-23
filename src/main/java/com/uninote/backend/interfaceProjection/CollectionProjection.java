package com.uninote.backend.interfaceProjection;

public interface CollectionProjection {
    Long getCollectionId();
    String getName();
    String getDescription();
    Boolean getIsPublic();
    String getAdminUsername(); 
    Long getTotalLikes();
    Long getNoteNum();
}
