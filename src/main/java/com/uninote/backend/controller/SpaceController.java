package com.uninote.backend.controller;

import com.uninote.backend.config.security.FirebaseAuthentication;
import com.uninote.backend.dto.SpaceSummaryDTO;
import com.uninote.backend.entity.Resource;
import com.uninote.backend.entity.Space;
import com.uninote.backend.service.ResourceService;
import com.uninote.backend.service.SVDRecommendationService;
import com.uninote.backend.service.SpaceService;

import java.nio.file.attribute.UserPrincipal;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/spaces")
public class SpaceController {

    private static final Logger logger = LoggerFactory.getLogger(SpaceController.class);
    private final SpaceService spaceService;


    @Autowired
    public SpaceController(SpaceService spaceService) {
        this.spaceService = spaceService;
    }

    @PostMapping
    public ResponseEntity<Space> createSpace(@RequestParam String title) {
        logger.error("here");
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            logger.error("here");

            if (authentication != null && authentication.isAuthenticated()) {
                FirebaseAuthentication firebaseAuthentication = (FirebaseAuthentication) authentication;
                String userUid = firebaseAuthentication.getUid(); 
                
                Space space = spaceService.createSpaceAndChat(title, userUid);
                logger.error("here");

                return new ResponseEntity<>(space, HttpStatus.CREATED);
            } else {
                return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED); 
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }



    @GetMapping("/{uuid}")
    public ResponseEntity<?> getSpaceDetails(@PathVariable String uuid) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || !authentication.isAuthenticated()) {
                return new ResponseEntity<>("Authorization token missing or invalid.", HttpStatus.UNAUTHORIZED);
            }

            FirebaseAuthentication firebaseAuth = (FirebaseAuthentication) authentication;
            String userUid = firebaseAuth.getUid();

            Space space = spaceService.findSpaceByUuidAndUser(uuid, userUid);
            if (space == null) {
                return new ResponseEntity<>("Space not found or you do not have access.", HttpStatus.NOT_FOUND);
            }

            var spaceChat = spaceService.getFirstSpaceChat(space);
            if (spaceChat == null) {
                return new ResponseEntity<>("Chat not found for this space.", HttpStatus.NOT_FOUND);
            }

            var conversation = spaceService.getChatMessagesById(spaceChat.getId());
            var resources = spaceService.getSpaceResources(uuid);

            return new ResponseEntity<>(Map.of(
                    "conversation", conversation,
                    "resources", resources,
                    "title", space.getTitle(),
                    "uuid", space.getUuid().toString(),
                    "space_chat_uuid", spaceChat.getUuid().toString()
            ), HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("An unexpected error occurred.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @PostMapping("/{spaceUuid}/resource")
    public ResponseEntity<?> addResourceToSpace(
            @PathVariable String spaceUuid,
            @RequestParam String type,
            @RequestParam(required = false) MultipartFile file,
            @RequestParam(required = false) String url
        ) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return new ResponseEntity<>("Authorization token missing or invalid.", HttpStatus.UNAUTHORIZED);
        }

        FirebaseAuthentication firebaseAuth = (FirebaseAuthentication) authentication;
        String userUid = firebaseAuth.getUid();
        Resource resource = spaceService.addResourceToSpace(
            type, file, url,  spaceUuid, userUid
        );

        return ResponseEntity.ok(resource);
    }

    @GetMapping
    public ResponseEntity<List<SpaceSummaryDTO>> getUserSpaces() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        FirebaseAuthentication firebaseAuth = (FirebaseAuthentication) authentication;
        String userUid = firebaseAuth.getUid();

        List<SpaceSummaryDTO> spaces = spaceService.getSpacesByUser(userUid);
        return ResponseEntity.ok(spaces);
    }
}
