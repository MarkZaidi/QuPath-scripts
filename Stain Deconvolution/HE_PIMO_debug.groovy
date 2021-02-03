import javafx.application.Platform
import org.locationtech.jts.geom.util.AffineTransformation
import qupath.lib.images.ImageData
import qupath.lib.images.servers.ImageChannel
import qupath.lib.images.servers.ImageServer
import qupath.lib.images.servers.ImageServers
import qupath.lib.roi.GeometryTools

import java.awt.geom.AffineTransform
import java.awt.image.BufferedImage
import java.util.stream.Collectors

import static qupath.lib.gui.scripting.QPEx.*
import qupath.lib.images.servers.TransformedServerBuilder

def img_name_full=getProjectEntry()
print(img_name_full)
if (img_name_full.toString().contains('HE'))
    print('HE image detected')
    def image_name = img_name_full.toString().replace('HE.tif','')
    print(image_name)
    def image_name2 = 'PIMO 19C 3'
    def transforms = [
            ("$image_name"+" HE.tif"): new AffineTransform(), // Identity transform (use this if no transform is needed)
            ("$image_name"+" PIMO.tif"): new AffineTransform()
    ]

