package blagodarie.rating.ui

import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.WriterException
import com.google.zxing.qrcode.QRCodeWriter
import java.util.*

private const val TAG = "QrCode"

fun createQrCodeBitmap(
        content: String
): Bitmap {
    Log.d(TAG, "createQrCodeBitmap")
    val width = 500
    val height = 500
    val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val writer = QRCodeWriter()
    val hints: MutableMap<EncodeHintType, Any?> = EnumMap(EncodeHintType::class.java)
    hints[EncodeHintType.CHARACTER_SET] = "UTF-8"
    hints[EncodeHintType.MARGIN] = 0 // default = 4
    try {
        val bitMatrix = writer.encode(
                content,
                BarcodeFormat.QR_CODE,
                width,
                height,
                hints
        )
        for (x in 0 until width) {
            for (y in 0 until height) {
                result.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.TRANSPARENT)
            }
        }
    } catch (e: WriterException) {
        Log.e(TAG, Log.getStackTraceString(e))
    }
    return result
}