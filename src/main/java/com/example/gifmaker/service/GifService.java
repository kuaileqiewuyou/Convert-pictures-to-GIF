package com.example.gifmaker.service;

import com.example.gifmaker.util.GifSequenceWriter;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Service
public class GifService {

    private static final long MAX_FILE_SIZE = 10L * 1024 * 1024;
    private static final int MAX_FILES = 60;

    public byte[] createGif(List<MultipartFile> files, int delayMs, boolean loop) {
        validate(files);

        List<BufferedImage> frames = new ArrayList<>();
        for (MultipartFile file : files) {
            try {
                BufferedImage image = ImageIO.read(new ByteArrayInputStream(file.getBytes()));
                if (image == null) {
                    throw new ResponseStatusException(BAD_REQUEST, "Unsupported image format: " + file.getOriginalFilename());
                }
                frames.add(image);
            } catch (IOException e) {
                throw new ResponseStatusException(BAD_REQUEST, "Failed to read image: " + file.getOriginalFilename(), e);
            }
        }

        Dimension targetSize = getMinSize(frames);
        List<BufferedImage> normalizedFrames = frames.stream()
                .map(frame -> resizeTo(frame, targetSize.width, targetSize.height))
                .toList();

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ImageOutputStream ios = ImageIO.createImageOutputStream(baos);
             GifSequenceWriter writer = new GifSequenceWriter(ios, BufferedImage.TYPE_INT_ARGB, delayMs, loop)) {

            for (BufferedImage frame : normalizedFrames) {
                writer.writeToSequence(frame);
            }

            ios.flush();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new ResponseStatusException(BAD_REQUEST, "Failed to generate GIF", e);
        }
    }

    private void validate(List<MultipartFile> files) {
        if (files == null || files.size() < 2) {
            throw new ResponseStatusException(BAD_REQUEST, "Please upload at least 2 images");
        }
        if (files.size() > MAX_FILES) {
            throw new ResponseStatusException(BAD_REQUEST, "Too many images. Max: " + MAX_FILES);
        }
        for (MultipartFile file : files) {
            if (file.isEmpty()) {
                throw new ResponseStatusException(BAD_REQUEST, "Empty file detected");
            }
            if (file.getSize() > MAX_FILE_SIZE) {
                throw new ResponseStatusException(BAD_REQUEST, "Single image size must be <= 10MB");
            }
        }
    }

    private Dimension getMinSize(List<BufferedImage> frames) {
        int minW = Integer.MAX_VALUE;
        int minH = Integer.MAX_VALUE;

        for (BufferedImage frame : frames) {
            minW = Math.min(minW, frame.getWidth());
            minH = Math.min(minH, frame.getHeight());
        }

        return new Dimension(minW, minH);
    }

    private BufferedImage resizeTo(BufferedImage source, int width, int height) {
        BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = resized.createGraphics();
        try {
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2.drawImage(source, 0, 0, width, height, null);
        } finally {
            g2.dispose();
        }
        return resized;
    }
}