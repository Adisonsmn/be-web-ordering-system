package com.aromasenja.common.util;

import com.aromasenja.common.exception.BusinessException;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Map;

/**
 * Generator QR Code menggunakan ZXing library.
 * Digunakan oleh domain meja untuk generate QR per meja.
 */
@Slf4j
@Component
public class QrCodeGenerator {

    private static final int DEFAULT_SIZE   = 300;
    private static final String IMAGE_FORMAT = "PNG";

    /**
     * Generate QR code sebagai byte array PNG.
     *
     * @param content konten yang di-encode dalam QR (biasanya URL scan meja)
     * @param width   lebar QR dalam pixel
     * @param height  tinggi QR dalam pixel
     * @return byte[] gambar PNG
     * @throws BusinessException jika ZXing gagal generate
     */
    public byte[] generateQrPng(String content, int width, int height) {
        try {
            QRCodeWriter writer = new QRCodeWriter();
            Map<EncodeHintType, Object> hints = Map.of(
                    EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M,
                    EncodeHintType.MARGIN, 1
            );
            BitMatrix bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, width, height, hints);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, IMAGE_FORMAT, outputStream);
            return outputStream.toByteArray();

        } catch (WriterException | IOException e) {
            log.error("Gagal generate QR code", e);
            throw new BusinessException("Gagal menghasilkan QR code: " + e.getMessage());
        }
    }

    /**
     * Generate QR code sebagai Base64 data URI (siap dikirim via JSON).
     *
     * @param content konten yang di-encode
     * @return string format "data:image/png;base64,..."
     */
    public String generateQrBase64(String content) {
        byte[] png = generateQrPng(content, DEFAULT_SIZE, DEFAULT_SIZE);
        return "data:image/png;base64," + Base64.getEncoder().encodeToString(png);
    }
}
