package com.uninote.backend.controller;

import com.uninote.backend.service.CollectionLikeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/collections")
public class CollectionLikeController {

    private final CollectionLikeService collectionLikeService;

    @Autowired
    public CollectionLikeController(CollectionLikeService collectionLikeService) {
        this.collectionLikeService = collectionLikeService;
    }

    @PostMapping("{collectionId}/like/{userId}")
    public ResponseEntity<?> likeCollection(
            @PathVariable Long collectionId,
            @PathVariable Long userId) {
        collectionLikeService.likeCollection(collectionId, userId);
        return ResponseEntity.ok().build();
    }


    @DeleteMapping("{collectionId}//unlike/{userId}")
    public ResponseEntity<?> unlikeCollection(
            @PathVariable Long collectionId,
            @PathVariable Long userId) {
        collectionLikeService.unlikeCollection(collectionId, userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{collectionId}/likes/count")
    public ResponseEntity<Long> getTotalActiveLikes(@PathVariable Long collectionId) {
        Long totalLikes = collectionLikeService.getTotalActiveLikesForCollection(collectionId);
        return ResponseEntity.ok(totalLikes);
    }
}
