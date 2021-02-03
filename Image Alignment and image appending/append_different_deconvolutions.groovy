/**
 * Merge images along the channels dimension in QuPath v0.2.0.
 *
 * This shows how multiple images can be combined by channel concatenation,
 * optionally applying color deconvolution or affine transformations along the way.
 * It may be applied to either brightfield images (with stains set) or fluorescence images.
 *
 * The result can be written to a file (if 'pathOutput' is defined) or opened in the QuPath viewer.
 *
 * Writing to a file is *strongly recommended* to ensure the result is preserved.
 * Opening in the viewer directly will have quite slow performance (as the transforms are applied dynamically)
 * and there is no guarantee the image can be reopened later, since the representation of the
 * transforms might change in future versions... so this is really only to preview results.
 *
 * Note QuPath does *not* offer full whole slide image registration - and there are no
 * plans to change this. If you require image registration, you probably need to use other
 * software to achieve this, and perhaps then import the registered images into QuPath later.
 *
 * Rather, this script is limited to applying a pre-defined affine transformation to align two or more
 * images. In the case where image registration has already been applied, it can be used to
 * concatenate images along the channel dimension without any addition transformation.
 *
 * In its current form, the script assumes you have an open project containing the images
 * OS-2.ndpi and OS-3.ndpi from the OpenSlide freely-distributable test data,
 * and the image type (and color deconvolution stains) have been set.
 * The script will apply a pre-defined affine transform to align the images (*very* roughly!),
 * and write their deconvolved channels together as a single 6-channel pseudo-fluorescence image.
 *
 * You will need to change the image names & add the correct transforms to apply it elsewhere.
 *
 * USE WITH CAUTION!
 * This uses still-in-development parts of QuPath that are not officially documented,
 * and may change or be removed in future versions.
 *
 * Made available due to frequency of questions, not readiness of code.
 *
 * For these reasons, I ask that you refrain from posting the script elsewhere, and instead link to this
 * Gist so that anyone requiring it can get the latest version.
 *
 * @author Pete Bankhead
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

// Define a transform, e.g. with the (also unfinished) 'Interactive image alignment' command
// Note: you may need to remove .createInverse() depending upon how the transform is created
//def os3Transform = GeometryTools.convertTransform(new AffineTransformation([1.0000, 0.0000,  433.7542,
                                                                            //0.0000,  1.0000, -313.6376] as double[])).createInverse()
// Define a map from the image name to the transform that should be applied to that image
def image_name = 'H17-178 CAL2 #29 L1 EF5 - high hypoxia.tif'
def transforms = [
        "zaidi_$image_name": new AffineTransform(), // Identity transform (use this if no transform is needed)
        "qupath_$image_name": new AffineTransform(),
        "histomicstk_$image_name": new AffineTransform(),
        "alsubaie_$image_name": new AffineTransform(),
        "macenko_$image_name": new AffineTransform(),
        "xu_$image_name": new AffineTransform(),
        "original_$image_name": new AffineTransform()
]
/*
def transforms = [
        'zaidi_H17-178 CA DUAL #26 L1 EF5 - no hypoxia.tif': new AffineTransform(), // Identity transform (use this if no transform is needed)
        'qupath_H17-178 CA DUAL #26 L1 EF5 - no hypoxia.tif': new AffineTransform(),
        'histomicstk_H17-178 CA DUAL #26 L1 EF5 - no hypoxia.tif': new AffineTransform(),
        'alsubaie_H17-178 CA DUAL #26 L1 EF5 - no hypoxia.tif': new AffineTransform(),
        'macenko_H17-178 CA DUAL #26 L1 EF5 - no hypoxia.tif': new AffineTransform(),
        'xu_H17-178 CA DUAL #26 L1 EF5 - no hypoxia.tif': new AffineTransform(),
        'original_H17-178 CA DUAL #26 L1 EF5 - no hypoxia.tif': new AffineTransform()
]
*/
//reg001_final.ome(1).tiff
//Exp_20200513_Region1_Spleen_H&E.tif
// Define an output path where the merged file should be written
// Recommended to use extension .ome.tif (required for a pyramidal image)
// If null, the image will be opened in a viewer
//String pathOutput = null
//String pathOutput = buildFilePath(PROJECT_BASE_DIR, "merged_$image_name.ome.tif")
String pathOutput = buildFilePath(PROJECT_BASE_DIR, "merged_$image_name"+".ome.tif")
// Choose how much to downsample the output (can be *very* slow to export large images with downsample 1!)
double outputDownsample = 1


// Loop through the transforms to create a server that merges these
def project = getProject()
def servers = []
def channels = []
int c = 0
for (def mapEntry : transforms.entrySet()) {
    // Find the next image & transform
    def name = mapEntry.getKey()
    print(name)
    def transform = mapEntry.getValue()
    if (transform == null)
        transform = new AffineTransform()
    def entry = project.getImageList().find {it.getImageName() == name}

    // Read the image & check if it has stains (for deconvolution)
    def imageData = entry.readImageData()
    def currentServer = imageData.getServer()
    def stains = imageData.getColorDeconvolutionStains()
    print(stains)
    
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
        println(stains)
        stains=null
        if (stains != null) {
            builder.deconvolveStains(stains)
            for (int i = 1; i <= 3; i++)
                channels << ImageChannel.getInstance(name + "-" + stains.getStain(i).getName(), ImageChannel.getDefaultChannelColor(c++))
        } else {
            channels.addAll(updateChannelNames(name, currentServer.getMetadata().getChannels()))
        }
        servers << builder.build()
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

// Prepend a base name to channel names
List<ImageChannel> updateChannelNames(String name, Collection<ImageChannel> channels) {
    return channels
            .stream()
            .map( c -> {
                return ImageChannel.getInstance(name + '-' + c.getName(), c.getColor())
                }
            ).collect(Collectors.toList())
}