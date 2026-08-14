package org.example.booknest_2.controller;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.example.booknest_2.util.ExternalIds;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import javax.imageio.ImageIO;

@RestController
@RequestMapping("/api/pdf")
public class PdfRenderController {

    private static final int MAX_PDF_BYTES = 64 * 1024 * 1024;
    private static final int MAX_PAGE_INDEX = 10_000;

    @GetMapping(value = "/render-page", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> renderPdfPage(@RequestParam String iaId, @RequestParam int page) {
        if (!ExternalIds.isValidInternetArchiveId(iaId) || page < 0 || page > MAX_PAGE_INDEX) {
            return ResponseEntity.badRequest().build();
        }

        try (InputStream in = openPdfStream(iaId);
             PDDocument document = Loader.loadPDF(readBounded(in))) {

            if (page >= document.getNumberOfPages()) {
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
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/pages-count")
    public ResponseEntity<Integer> getPagesCount(@RequestParam String iaId) {
        if (!ExternalIds.isValidInternetArchiveId(iaId)) {
            return ResponseEntity.badRequest().body(0);
        }
        try (InputStream in = openPdfStream(iaId);
             PDDocument document = Loader.loadPDF(readBounded(in))) {
            return ResponseEntity.ok(document.getNumberOfPages());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(0);
        }
    }

    private InputStream openPdfStream(String iaId) throws IOException {
        URL pdfUrl = URI.create("https://archive.org/download/" + iaId + "/" + iaId + ".pdf").toURL();
        return pdfUrl.openStream();
    }

    private byte[] readBounded(InputStream in) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] chunk = new byte[8192];
        int read;
        while ((read = in.read(chunk)) != -1) {
            if (buffer.size() + read > MAX_PDF_BYTES) {
                throw new IOException("PDF exceeds the maximum allowed size");
            }
            buffer.write(chunk, 0, read);
        }
        return buffer.toByteArray();
    }
}
