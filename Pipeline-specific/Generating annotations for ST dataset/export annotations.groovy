/** Script to export image objects, either as a labelled image (if less than 65535 objects) or binary mask
 * Applicable for annotations, detections, and cells and their subcompartments
 * TO DO: Build a python script using skimage.measure.label to externally convert a binary mask of objects into
 * a label image, to account for >65535 objects. Probably store as a 32-bit image
 */

import qupath.lib.images.servers.LabeledImageServer

def imageData = getCurrentImageData()

// Define output path (relative to project)
def outputDir = buildFilePath(PROJECT_BASE_DIR, 'binary annotation masks')
mkdirs(outputDir)
def name = GeneralTools.getNameWithoutExtension(imageData.getServer().getMetadata().getName())
def path = buildFilePath(outputDir, name + "-labels7.tif")

// Define how much to downsample during export (may be required for large images)
double downsample = 1

// Create an ImageServer where the pixels are derived from annotations
def labelServer = new LabeledImageServer.Builder(imageData)
  .backgroundLabel(0, ColorTools.WHITE) // Specify background label (usually 0 or 255)
  .downsample(downsample)    // Choose server resolution; this should match the resolution at which tiles are exported
  //.useCells() //define which kinds of objects to export
  .addLabel('PIMO positive', 1)
  .addLabel('PIMO negative', 2)
  .addLabel('background', 3)//use if you want to export cells of a specific classification. Number denotes grayscale value
  //.addUnclassifiedLabel(1) //export all unclassified objects. Number denotes grayscale value
  //.lineThickness(1) //thickness of line separating objects
  //.setBoundaryLabel('Ignore',0,ColorTools.WHITE) //boundary label, grayscale value
  //.setBoundaryLabel('Ignore', 2)
  .multichannelOutput(false) // If true, each label refers to the channel of a multichannel binary image (required for multiclass probability)
  //.useUniqueLabels() //limited to 65535 cells

  .build()

// Write the image
writeImage(labelServer, path)
print('Done!')