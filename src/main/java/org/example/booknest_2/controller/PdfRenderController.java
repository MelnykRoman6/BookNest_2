package org.example.booknest_2.controller;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.example.booknest_2.exception.ExternalServiceException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import javax.imageio.ImageIO;

@RestController
@RequestMapping("/api/pdf")
public class PdfRenderController {

    @GetMapping(value = "/render-page", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> renderPdfPage(@RequestParam String iaId, @RequestParam int page) {
        try (PDDocument document = loadDocument(iaId)) {

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

        } catch (IOException e) {
            throw new UncheckedIOException("Could not render page " + page + " of " + iaId, e);
        }
    }

    @GetMapping("/pages-count")
    public ResponseEntity<Integer> getPagesCount(@RequestParam String iaId) {
        try (PDDocument document = loadDocument(iaId)) {
            return ResponseEntity.ok(document.getNumberOfPages());
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read page count of " + iaId, e);
        }
    }

    private PDDocument loadDocument(String iaId) throws IOException {
        String pdfUrl = "https://archive.org/download/" + iaId + "/" + iaId + ".pdf";

        byte[] pdfBytes;
        try (InputStream in = URI.create(pdfUrl).toURL().openStream()) {
            pdfBytes = in.readAllBytes();
        } catch (IOException e) {
            throw new ExternalServiceException("Could not download PDF from " + pdfUrl, e);
        }

        return Loader.loadPDF(pdfBytes);
    }
}
