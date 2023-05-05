package watermark

import java.awt.Color
import java.awt.Transparency.TRANSLUCENT
import java.awt.image.BufferedImage
import java.io.File
import java.lang.NumberFormatException
import javax.imageio.ImageIO
import kotlin.system.exitProcess

fun main() {
    println("Input the image filename:")
    val inputSourceFile = readln()
    val imageSourceFile = File(inputSourceFile)
    val image: BufferedImage


    if (!imageSourceFile.exists()) {
        println("The file $inputSourceFile doesn't exist.")
        exitProcess(0)
    } else image = ImageIO.read(imageSourceFile)

    if (image.colorModel.numColorComponents != 3) {
        println("The number of image color components isn't 3.")
        exitProcess(0)
    } else if (image.colorModel.pixelSize != 24 && image.colorModel.pixelSize != 32) {
        println("The image isn't 24 or 32-bit.")
        exitProcess(0)
    }


    println("Input the watermark image filename:")
    val inputFileWatermark = readln()
    val imageWatermarkFile = File(inputFileWatermark)
    val imageWatermark: BufferedImage

    if (!imageWatermarkFile.exists()) {
        println("The file $inputFileWatermark doesn't exist.")
        exitProcess(0)
    } else imageWatermark = ImageIO.read(imageWatermarkFile)

    if (imageWatermark.colorModel.numColorComponents != 3) {
        println("The number of watermark color components isn't 3.")
        exitProcess(0)
    } else if (imageWatermark.colorModel.pixelSize != 24 && imageWatermark.colorModel.pixelSize != 32) {
        println("The watermark isn't 24 or 32-bit.")
        exitProcess(0)
    } else if (image.width < imageWatermark.width || image.height < imageWatermark.height) {
        println("The watermark's dimensions are larger.")
        exitProcess(0)
    }


    val alphaChanel = if (imageWatermark.transparency == TRANSLUCENT) {
        println("Do you want to use the watermark's Alpha channel?")
        readln().lowercase()
    } else "no"


    fun errorTransparencyColor() {
        println("The transparency color input is invalid.")
        exitProcess(0)
    }

    val transparencyColorList = arrayListOf<Int>()
    var transparencyColor = Color(255, 255, 255)
    if (imageWatermark.transparency != TRANSLUCENT) {
        println("Do you want to set a transparency color?")
        if (readln().lowercase() == "yes") {
            println("Input a transparency color ([Red] [Green] [Blue]):")
            val input = readln().split(" ")
            try {
                for (str in input) {
                    if (input.size == 3 && str.toInt() in 0..255) transparencyColorList.add(str.toInt())
                    else errorTransparencyColor()
                }
                transparencyColor = Color(
                    transparencyColorList[0],
                    transparencyColorList[1],
                    transparencyColorList[2]
                )
            } catch (e: NumberFormatException) {
                errorTransparencyColor()
            }
        }
    }


    println("Input the watermark transparency percentage (Integer 0-100):")
    val watermarkTransparency: Int
    try {
        watermarkTransparency = readln().toInt()
    } catch (e: NumberFormatException) {
        println("The transparency percentage isn't an integer number.")
        exitProcess(0)
    }
    if (watermarkTransparency !in 0..100) {
        println("The transparency percentage is out of range.")
        exitProcess(0)
    }


    fun errorPositionWatermark() {
        println("The position input is invalid.")
        exitProcess(0)
    }

    fun errorRangePositionWatermark() {
        println("The position input is out of range.")
        exitProcess(0)
    }

    val watermarkPosition = arrayListOf<Int>()
    val diffX = image.width - imageWatermark.width
    val diffY = image.height - imageWatermark.height
    println("Choose the position method (single, grid):")
    val inputPositionMethod = readln().lowercase()
    if (inputPositionMethod == "single") {
        println("Input the watermark position ([x 0-$diffX] [y 0-$diffY]):")
        val inputWatermarkPosition = readln().split(" ")
        try {
            if (inputWatermarkPosition.size != 2) errorPositionWatermark()
            else if (inputWatermarkPosition[0].toInt() in 0..diffX && inputWatermarkPosition[1].toInt() in 0..diffY) {
                watermarkPosition.add(inputWatermarkPosition[0].toInt())
                watermarkPosition.add(inputWatermarkPosition[1].toInt())
            } else errorRangePositionWatermark()
        } catch (e: Exception) {
            errorPositionWatermark()
        }
    } else if (inputPositionMethod != "grid") {
        println("The position method input is invalid.")
        exitProcess(0)
    }

    println("Input the output image filename (jpg or png extension):")
    val inputOutputFile = readln()
//    val outputImage = BufferedImage(image.width, image.height, BufferedImage.TYPE_INT_RGB)
    val outputImage = image
    if (".jpg" !in inputOutputFile && ".png" !in inputOutputFile) {
        println("The output file extension isn't \"jpg\" or \"png\".")
        exitProcess(0)
    }

    if (inputPositionMethod == "single") {
        for (x in 0 until image.width)
            for (y in 0 until image.height) {
                if (x in watermarkPosition[0] until watermarkPosition[0] + imageWatermark.width &&
                    y in watermarkPosition[1] until watermarkPosition[1] + imageWatermark.height
                ) {

                    val i = Color(image.getRGB(x, y)) // image.color
                    val w = if (alphaChanel == "yes") // watermark.color
                        Color(imageWatermark.getRGB(x - watermarkPosition[0], y - watermarkPosition[1]), true)
                    else
                        Color(
                            imageWatermark.getRGB(
                                x - watermarkPosition[0],
                                y - watermarkPosition[1]
                            )
                        )

                    val newColor = if (alphaChanel == "yes" && w.alpha == 0) Color(i.rgb)
                    else if (transparencyColorList.isNotEmpty() && w == transparencyColor) Color(i.rgb)
                    else Color(
                        (watermarkTransparency * w.red + (100 - watermarkTransparency) * i.red) / 100,
                        (watermarkTransparency * w.green + (100 - watermarkTransparency) * i.green) / 100,
                        (watermarkTransparency * w.blue + (100 - watermarkTransparency) * i.blue) / 100
                    )

                    outputImage.setRGB(x, y, newColor.rgb)
                }
            }
    } else if (inputPositionMethod == "grid") {
        for (x in 0 until image.width) {
            for (y in 0 until image.height) {
                val i = Color(image.getRGB(x, y)) // image.color
                val w = if (alphaChanel == "yes") // watermark.color
                    Color(imageWatermark.getRGB(x % imageWatermark.width, y % imageWatermark.height), true)
                else Color(imageWatermark.getRGB(x % imageWatermark.width, y % imageWatermark.height))

                val newColor = if (alphaChanel == "yes" && w.alpha == 0) Color(i.rgb)
                else if (transparencyColorList.isNotEmpty() && w == transparencyColor) Color(i.rgb)
                else Color(
                    (watermarkTransparency * w.red + (100 - watermarkTransparency) * i.red) / 100,
                    (watermarkTransparency * w.green + (100 - watermarkTransparency) * i.green) / 100,
                    (watermarkTransparency * w.blue + (100 - watermarkTransparency) * i.blue) / 100
                )

                outputImage.setRGB(x, y, newColor.rgb)
            }
        }
    }

    val outputFile = File(inputOutputFile)
    ImageIO.write(outputImage, inputOutputFile.substringAfterLast('.'), outputFile)
    println("The watermarked image $inputOutputFile has been created.")
}
