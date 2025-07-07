package com.uninote.backend.controller;

import com.uninote.backend.config.security.FirebaseAuthentication;
import com.uninote.backend.dto.ResourceDTO;
import com.uninote.backend.dto.SpaceDTO;
import com.uninote.backend.dto.SpaceRequest;
import com.uninote.backend.dto.SpaceSummaryDTO;
import com.uninote.backend.entity.Resource;
import com.uninote.backend.entity.Space;
import com.uninote.backend.entity.User;
import com.uninote.backend.repository.UserRepository;
import com.uninote.backend.service.ResourceService;
import com.uninote.backend.service.SVDRecommendationService;
import com.uninote.backend.service.SpaceService;
import com.uninote.backend.service.UsageLimitService;

import java.nio.file.attribute.UserPrincipal;
import java.util.HashMap;
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

    public  static class SpaceCreationRequest {
        private String title;

        

        public void setTitle(String title) {
            this.title = title;
        }

        public String getTitle(){
            return this.title;
        }
    } 

    private static final Logger logger = LoggerFactory.getLogger(SpaceController.class);
    private final SpaceService spaceService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UsageLimitService usageLimitService;

    @Autowired
    public SpaceController(SpaceService spaceService) {
        this.spaceService = spaceService;
    }

    @PostMapping
    public ResponseEntity<SpaceDTO> createSpace(@RequestBody SpaceCreationRequest body) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {
            FirebaseAuthentication firebaseAuthentication = (FirebaseAuthentication) authentication;
            String userUid = firebaseAuthentication.getUid(); 
            User user = userRepository.findByFirebaseUid(userUid)
                .orElseThrow(() -> new RuntimeException("User not found"));
            usageLimitService.checkDailySpaceLimit(user);
            try {
                String title = body.getTitle();
                

                Space space = spaceService.createSpaceAndChat(title, userUid);
                logger.error("here");
                SpaceDTO dto = new SpaceDTO(space.getId(), 
                                            space.getTitle(), 
                                            space.getUuid(), 
                                            space.getCreatedAt());

                return new ResponseEntity<>(dto, HttpStatus.CREATED);
            } catch (Exception e) {
                e.printStackTrace();
                return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
            }
            } else {
                return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED); 
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

            Map<String, Object> response = new HashMap<>();
            response.put("conversation", conversation);
            response.put("resources", resources);
            response.put("title", space.getTitle());
            response.put("uuid", space.getUuid() != null ? space.getUuid().toString() : null);
            response.put("space_chat_uuid", spaceChat.getUuid() != null ? spaceChat.getUuid().toString() : null);
            
            return new ResponseEntity<>(response, HttpStatus.OK);

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
            @RequestParam(required = false) String url,
            @RequestParam(required = false) Long noteId
        ) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return new ResponseEntity<>("Authorization token missing or invalid.", HttpStatus.UNAUTHORIZED);
        }

        FirebaseAuthentication firebaseAuth = (FirebaseAuthentication) authentication;
        String userUid = firebaseAuth.getUid();
        Resource resource = spaceService.addResourceToSpace(
            type, file, url,  spaceUuid, userUid, noteId
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

    @DeleteMapping("/{spaceUuid}/resources/{resourceId}")
    public ResponseEntity<Void> removeResourceInSpace(
            @PathVariable String spaceUuid,
            @PathVariable Long resourceId) {

        spaceService.removeResourceFromSpace(spaceUuid, resourceId);
        return ResponseEntity.noContent().build(); 
    }

    @PutMapping("/{spaceUuid}")
    public ResponseEntity<SpaceRequest> updateSpace(@PathVariable String spaceUuid, @RequestBody SpaceRequest spaceRequest){
        SpaceRequest req =  spaceService.updateSpace(spaceUuid, spaceRequest);
        return ResponseEntity.ok(req);
    }
}
