package other;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageConfig;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;

import java.awt.image.BufferedImage;

public class QRCodeUtil {
    public static BufferedImage generate(String text, int size) throws Exception {
        BitMatrix bm = new MultiFormatWriter().encode(text, BarcodeFormat.QR_CODE, size, size);

        return MatrixToImageWriter.toBufferedImage(bm, new MatrixToImageConfig(0xFF000000, 0xFFFFFFFF));
    }
}
