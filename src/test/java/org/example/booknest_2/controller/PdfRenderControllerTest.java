package org.example.booknest_2.controller;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;

class PdfRenderControllerTest {

    private static byte[] pdfWithPages(int pages) throws IOException {
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            for (int i = 0; i < pages; i++) {
                document.addPage(new PDPage());
            }
            document.save(out);
            return out.toByteArray();
        }
    }

    private PdfRenderController controllerServing(byte[] pdf) {
        return new PdfRenderController() {
            @Override
            protected InputStream openPdfStream(String iaId) {
                return new ByteArrayInputStream(pdf);
            }
        };
    }

    private PdfRenderController failingController() {
        return new PdfRenderController() {
            @Override
            protected InputStream openPdfStream(String iaId) throws IOException {
                throw new IOException("archive unreachable");
            }
        };
    }

    @Test
    void renderPdfPageReturnsPngImage() throws IOException {
        ResponseEntity<byte[]> response = controllerServing(pdfWithPages(2)).renderPdfPage("dune1965", 1);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.IMAGE_PNG);
        assertThat(response.getBody()).isNotEmpty();
    }

    @Test
    void renderPdfPageRejectsOutOfRangePages() throws IOException {
        PdfRenderController controller = controllerServing(pdfWithPages(1));

        assertThat(controller.renderPdfPage("dune1965", -1).getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(controller.renderPdfPage("dune1965", 1).getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void renderPdfPageReturnsServerErrorWhenDownloadFails() {
        ResponseEntity<byte[]> response = failingController().renderPdfPage("dune1965", 0);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void getPagesCountReturnsNumberOfPages() throws IOException {
        ResponseEntity<Integer> response = controllerServing(pdfWithPages(3)).getPagesCount("dune1965");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(3);
    }

    @Test
    void getPagesCountReturnsZeroWhenDownloadFails() {
        ResponseEntity<Integer> response = failingController().getPagesCount("dune1965");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isZero();
    }
}
