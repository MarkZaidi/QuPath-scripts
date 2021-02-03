
/**
 * Script to export color deconvolved tiles from a whole slide image in QuPath
 * to ImageJ TIFFs for further processing.
 *
 * @author Pete Bankhead
 */
import groovy.time.*

def timeStart = new Date()
// Some code you want to time

import qupath.lib.common.GeneralTools
import qupath.lib.images.ImageData
import qupath.lib.images.servers.ImageServerMetadata
import qupath.lib.images.servers.TransformedServerBuilder
import qupath.lib.images.writers.TileExporter

import java.awt.image.BufferedImage

import static qupath.lib.gui.scripting.QPEx.*
setImageType('BRIGHTFIELD_H_DAB');
setColorDeconvolutionStains('{"Name" : "H-DAB adjusted", "Stain 1" : "Hematoxylin", "Values 1" : "0.4709 0.4892 0.3884 ", "Stain 2" : "DAB", "Values 2" : "0.4402 0.6335 0.7576 ", "Background" : " 242 242 242 "}');
// Create an export path relative to the current project (requires that there *is* a project)
def path = buildFilePath(PROJECT_BASE_DIR, 'tiles')
mkdirs(path)

// Create an ImageServer that applies color deconvolution to the current image, using the current stains
def imageData = getCurrentImageData()
def server = new TransformedServerBuilder(imageData.getServer())
    .deconvolveStains(imageData.getColorDeconvolutionStains())
    .build()

// Slightly tortured way to control the output file names without all the stains encoded within it
server.setMetadata(
        new ImageServerMetadata.Builder(server.getMetadata())
            .name(GeneralTools.getNameWithoutExtension(imageData.getServer().getMetadata().getName()))
            .build()
)

// Export tiles
def exporter = new TileExporter(new ImageData<BufferedImage>(server))
    .tileSize(8192, 8192)   // Determines export tile size
    .downsample(1.0)        // Determines export resolution
    .imageExtension('.tif') // ImageJ TIFF
    .includePartialTiles(true) // Controls tiles as the boundary
    .channels('DAB')

// Uncomment this line if you want to see customization options
//println(describe(exporter))

// Write the tiles
exporter.writeTiles(path)
println('Done')

def timeStop = new Date()
TimeDuration duration = TimeCategory.minus(timeStop, timeStart)
println duration