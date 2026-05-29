package org.example.booknest_2.controller;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import javax.imageio.ImageIO;

@RestController
@RequestMapping("/api/pdf")
public class PdfRenderController {

    private final RestTemplate restTemplate = new RestTemplate();

    @GetMapping(value = "/render-page", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> renderPdfPage(@RequestParam String iaId, @RequestParam int page) {
        String pdfUrl = "https://archive.org/download/" + iaId + "/" + iaId + ".pdf";

        try (InputStream in = new URL(pdfUrl).openStream();
             PDDocument document = Loader.loadPDF(in.readAllBytes())) {

            if (page < 0 || page >= document.getNumberOfPages()) {
                return ResponseEntity.badRequest().build();
            }

            PDFRenderer pdfRenderer = new PDFRenderer(document);
            BufferedImage bim = pdfRenderer.renderImageWithDPI(page, 150);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(bim, "png", baos);

            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_PNG)
                    .body(baos.toByteArray());

        } catch (Exception e) {
            System.err.println("Rendering error: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/pages-count")
    public ResponseEntity<Integer> getPagesCount(@RequestParam String iaId) {
        String pdfUrl = "https://archive.org/download/" + iaId + "/" + iaId + ".pdf";
        try (InputStream in = new URL(pdfUrl).openStream();
             PDDocument document = Loader.loadPDF(in.readAllBytes())) {
            return ResponseEntity.ok(document.getNumberOfPages());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(0);
        }
    }
}