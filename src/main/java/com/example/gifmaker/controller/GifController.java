package com.example.gifmaker.controller;

import com.example.gifmaker.service.GifService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/gif")
@Validated
public class GifController {

    private final GifService gifService;

    public GifController(GifService gifService) {
        this.gifService = gifService;
    }

    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<byte[]> createGif(
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam(name = "delayMs", defaultValue = "300") @Min(50) @Max(5000) int delayMs,
            @RequestParam(name = "loop", defaultValue = "true") boolean loop
    ) {
        byte[] gifBytes = gifService.createGif(files, delayMs, loop);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_GIF);
        headers.setContentDisposition(ContentDisposition.attachment().filename("generated.gif").build());

        return ResponseEntity.ok()
                .headers(headers)
                .body(gifBytes);
    }
}
