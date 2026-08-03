package com.teachmeski.app

import androidx.compose.ui.graphics.toArgb
import com.teachmeski.app.ui.theme.TmsColor
import java.nio.file.Files
import java.nio.file.Path
import javax.imageio.ImageIO
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LauncherIconResourcesTest {

    @Test
    fun `adaptive background fills every launcher mask with brand blue`() {
        densityScales.forEach { (density, scale) ->
            val image = readIcon(density, "ic_launcher_background.png")

            assertEquals((108 * scale).toInt(), image.width)
            assertEquals((108 * scale).toInt(), image.height)
            assertEquals(TmsColor.Primary.toArgb(), image.getRGB(0, 0))
            assertEquals(TmsColor.Primary.toArgb(), image.getRGB(image.width / 2, image.height / 2))
            assertEquals(TmsColor.Primary.toArgb(), image.getRGB(image.width - 1, image.height - 1))
        }
    }

    @Test
    fun `adaptive foreground contains only the logo artwork`() {
        densityScales.forEach { (density, scale) ->
            val image = readIcon(density, "ic_launcher_foreground.png")

            assertEquals((108 * scale).toInt(), image.width)
            assertEquals((108 * scale).toInt(), image.height)
            assertEquals(0, alphaAt(image, 54, 20, scale))
            assertTrue(alphaAt(image, 54, 54, scale) > 0)
            assertTrue(alphaAt(image, 60, 54, scale) > 0)
        }
    }

    @Test
    fun `themed icon preserves the S as transparent negative space`() {
        densityScales.forEach { (density, scale) ->
            val image = readIcon(density, "ic_launcher_monochrome.png")

            assertEquals(0, alphaAt(image, 54, 20, scale))
            assertTrue(alphaAt(image, 54, 54, scale) > 0)
            assertTrue(alphaAt(image, 60, 54, scale) <= MAX_EDGE_ALPHA)
        }
    }

    private fun readIcon(density: String, fileName: String) =
        ImageIO.read(resourceRoot.resolve("mipmap-$density").resolve(fileName).toFile())

    private fun alphaAt(
        image: java.awt.image.BufferedImage,
        xDp: Int,
        yDp: Int,
        scale: Double,
    ): Int {
        val x = (xDp * scale).toInt()
        val y = (yDp * scale).toInt()
        return image.getRGB(x, y).ushr(24) and 0xFF
    }

    private companion object {
        val densityScales = mapOf(
            "mdpi" to 1.0,
            "hdpi" to 1.5,
            "xhdpi" to 2.0,
            "xxhdpi" to 3.0,
            "xxxhdpi" to 4.0,
        )

        val resourceRoot: Path = generateSequence(Path.of("").toAbsolutePath()) { it.parent }
            .map { it.resolve("app/src/main/res") }
            .first(Files::isDirectory)

        const val MAX_EDGE_ALPHA = 32
    }
}
