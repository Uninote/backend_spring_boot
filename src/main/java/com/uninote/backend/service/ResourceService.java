package com.uninote.backend.service;

import com.uninote.backend.entity.*;
import com.uninote.backend.repository.FileResourceRepository;
import com.uninote.backend.repository.YouTubeResourceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

@Service
public class ResourceService {

    @Autowired
    private FileResourceRepository fileResourceRepository;

    @Autowired
    private YouTubeResourceRepository youTubeResourceRepository;

    public FileResource createFileResource(MultipartFile file) {
        FileResource fr = new FileResource();
        fr.setSupabaseFileUrl("https://supabase.storage/" + UUID.randomUUID());
        fr.setTitle(file.getOriginalFilename());
        fr.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        return fileResourceRepository.save(fr);
    }

    public YouTubeResource createYouTubeResource(String youtubeUrl) {
        String videoId = extractVideoId(youtubeUrl);

        YouTubeResource yt = new YouTubeResource();
        yt.setYoutubeUrl(youtubeUrl);
        yt.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        return youTubeResourceRepository.save(yt);
    }

    private String extractVideoId(String url) {
        if (url.contains("v=")) return url.split("v=")[1].split("&")[0];
        if (url.contains("youtu.be")) return url.substring(url.lastIndexOf("/") + 1);
        throw new IllegalArgumentException("Invalid YouTube URL");
    }
}
