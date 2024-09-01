package com.uninote.backend.interfaceProjection;

import java.math.BigDecimal;

import com.uninote.backend.dto.NoteDTO;

public interface CollectionProjection {
    Long getCollectionId();
    String getName();
    String getDescription();
    String getAdminUsername(); 
    Long getTotalLikes();
    Long getNoteNum();
    BigDecimal getIsPublicRaw();  

   NoteDTO getFirstNote();
    default Boolean getIsPublic() {
        return getIsPublicRaw() != null && getIsPublicRaw().compareTo(BigDecimal.ONE) == 0;
    }
    

}
