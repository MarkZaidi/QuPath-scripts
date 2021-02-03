/**
 * Modification of Pete's QuPath-Concatenate channels.groovy: https://gist.github.com/petebankhead/db3a3c199546cadc49a6c73c2da14d6c
 * Iterates over project containing aligned H&E and PIMO images, deconvolves them using the current color vectors,
 * and writes it out as a 12-channel ome tiff (6 floating point channels for deconvoluted channels, 6 uint8/16 for original RGB.
 * Instructions:
 * - verify images have already been aligned
 * - verify HE image filename ends in ' HE.tif' and PIMO image ends in ' PIMO.tif' (including space)
 * - verify image type and stain vectors are already applied
 *      -To do this, select a representative HE and PIMO image, go preprocessing > estimate stain vectors
 * @original author Pete Bankhead
 */

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
// Run on all images in project. For each image, check to see if it contains HE in title.
// If true, deconvolve HE and corresponding PIMO
def img_name_full = getProjectEntry()
print(img_name_full)
if (img_name_full.toString().contains('HE')) {
    print('HE image detected')
    def image_name = img_name_full.toString().replace(' HE.tif', "")
    print(image_name)
    def transforms = [
            ("$image_name" + " HE.tif")  : new AffineTransform(), // Identity transform (use this if no transform is needed)
            ("$image_name" + " PIMO.tif"): new AffineTransform()
    ]
    // Define an output path where the merged file should be written
    // Recommended to use extension .ome.tif (required for a pyramidal image)
    // If null, the image will be opened in a viewer
    //String pathOutput = null
    String pathOutput = buildFilePath(PROJECT_BASE_DIR, ('merged_' + "$image_name" + ".ome.tif"))

    // Choose how much to downsample the output (can be *very* slow to export large images with downsample 1!)
    double outputDownsample = 3
    print(pathOutput)


    // Loop through the transforms to create a server that merges these
    def project = getProject()
    def servers = []
    def channels = []
    int c = 0
    for (def mapEntry : transforms.entrySet()) {
        // Find the next image & transform
        def name = mapEntry.getKey()
        def transform = mapEntry.getValue()
        if (transform == null)
            transform = new AffineTransform()
        def entry = project.getImageList().find { it.getImageName() == name }
        // Read the image & check if it has stains (for deconvolution)
        def imageData = entry.readImageData()
        def currentServer = imageData.getServer()
        def stains = imageData.getColorDeconvolutionStains()
        // Nothing more to do if we have the identity trainform & no stains
        if (transform.isIdentity() && stains == null) {
            channels.addAll(updateChannelNames(name, currentServer.getMetadata().getChannels()))
            servers << currentServer
            continue
        } else {
            // Create a server to apply transforms
            def builder = new TransformedServerBuilder(currentServer)
            if (!transform.isIdentity())
                builder.transform(transform)
            // If we have stains, deconvolve them

            if (stains != null) {
                builder.deconvolveStains(stains)
                for (int i = 1; i <= 3; i++)
                    channels << ImageChannel.getInstance(name + "-" + stains.getStain(i).getName(), ImageChannel.getDefaultChannelColor(c++))
            } else {
                channels.addAll(updateChannelNames(name, currentServer.getMetadata().getChannels()))
            }

            channels.addAll(updateChannelNames(name, currentServer.getMetadata().getChannels()))
            //Mark modification: in addition to writing out deconvolved channels, include original RGB channels for viewing purposes
            servers << builder.build()
            servers << currentServer


        }
    }

    println 'Channels: ' + channels.size()

    // Remove the first server - we need to use it as a basis (defining key metadata, size)
    ImageServer<BufferedImage> server = servers.remove(0)
    // If anything else remains, concatenate along the channels dimension
    if (!servers.isEmpty())
        server = new TransformedServerBuilder(server)
                .concatChannels(servers)
                .build()

    // Write the image or open it in the viewer
    if (pathOutput != null) {
        if (outputDownsample > 1)
            server = ImageServers.pyramidalize(server, outputDownsample)
        writeImage(server, pathOutput)
    } else {
        // Create the new image & add to the project
        def imageData = new ImageData<BufferedImage>(server)
        setChannels(imageData, channels as ImageChannel[])
        Platform.runLater {
            getCurrentViewer().setImageData(imageData)
        }
    }

   
} else {
print('PIMO section detected, skipping')
}
 // Prepend a base name to channel names
    List<ImageChannel> updateChannelNames(String name, Collection<ImageChannel> channels) {
        return channels
                .stream()
                .map(c -> {
                    return ImageChannel.getInstance(name + '-' + c.getName(), c.getColor())
                }
                ).collect(Collectors.toList())
    }