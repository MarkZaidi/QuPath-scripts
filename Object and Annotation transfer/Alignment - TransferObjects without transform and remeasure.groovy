/**
 * Script to transfer QuPath objects from one image to another, applying an AffineTransform to any ROIs.
 * Should be run from the image you want to move the objects into
 * otherImageName should be the name of the file the objects are coming from...
 * and will also be the name of the affine transformation matrix file in the Affine folder
 
 
 * The easiest way to do this is to create objects in image A, perform the alignment between images in image B
 * and then run this script from image B (destination). 
 
 
 * The script can be reworked to behave in different ways as well, for example you could
 * create an alignment in image B, C, and D to image A (open 3 different images, create the alignment to the first image
 * in each), which will give you Affine files named B, C and D. Then the objects in B, C and D could all be transferred back
 * to A, or by flipping the invert boolean, distribute the objects in image A to B, C and D. Performing the alignment to A
 * from all other images streamlines downstream mass transfers, though some of the script may need to be edited for specific cases
 * There is also a varient of the script that loops through all Affine objects within a folder and uses them to transfer 
 * objects in a loop.

  Built off of : https://forum.image.sc/t/interactive-image-alignment/23745/9
 */

def name = getProjectEntry().getImageName()


// SET ME! Define image containing the original objects (must be in the current project)
def otherImageName = "H17-178 549 CTRL L2 EF5 - no hypoxia.tif_origIHC"

// SET ME! Delete existing objects
def deleteExisting = true

// SET ME! Change this if things end up in the wrong place
def createInverse = true


import qupath.lib.objects.PathCellObject
import qupath.lib.objects.PathDetectionObject
import qupath.lib.objects.PathObject
import qupath.lib.objects.PathObjects
import qupath.lib.objects.PathTileObject
import qupath.lib.roi.RoiTools
import qupath.lib.roi.interfaces.ROI

import java.awt.geom.AffineTransform

import static qupath.lib.gui.scripting.QPEx.*


// Get the project & the requested image name
def project = getProject()
def entry = project.getImageList().find {it.getImageName() == otherImageName}
if (entry == null) {
    print 'Could not find image with name ' + otherImageName
    return
}

def otherHierarchy = entry.readHierarchy()
def pathObjects = otherHierarchy.getAnnotationObjects()



def transform =new AffineTransform(1.0, 0.0, 0.0, 1.0, 0.0, 0.0)
print transform
if (createInverse)
    transform = transform.createInverse()

if (deleteExisting)
    clearAllObjects()

def newObjects = []
for (pathObject in pathObjects) {
    newObjects << transformObject(pathObject, transform)
}
addObjects(newObjects)

print 'Done!'

/**
 * Transform object, recursively transforming all child objects
 *
 * @param pathObject
 * @param transform
 * @return
 */
PathObject transformObject(PathObject pathObject, AffineTransform transform) {
    // Create a new object with the converted ROI
    def roi = pathObject.getROI()
    def roi2 = transformROI(roi, transform)
    def newObject = null
    if (pathObject instanceof PathCellObject) {
        def nucleusROI = pathObject.getNucleusROI()
        if (nucleusROI == null)
            newObject = PathObjects.createCellObject(roi2, pathObject.getPathClass(), pathObject.getMeasurementList())
        else
            newObject = PathObjects.createCellObject(roi2, transformROI(nucleusROI, transform), pathObject.getPathClass(), pathObject.getMeasurementList())
    } else if (pathObject instanceof PathTileObject) {
        newObject = PathObjects.createTileObject(roi2, pathObject.getPathClass(), pathObject.getMeasurementList())
    } else if (pathObject instanceof PathDetectionObject) {
        newObject = PathObjects.createDetectionObject(roi2, pathObject.getPathClass(), pathObject.getMeasurementList())
    } else {
        newObject = PathObjects.createAnnotationObject(roi2, pathObject.getPathClass(), pathObject.getMeasurementList())
    }
    // Handle child objects
    if (pathObject.hasChildren()) {
        newObject.addPathObjects(pathObject.getChildObjects().collect({transformObject(it, transform)}))
    }
    return newObject
}

/**
 * Transform ROI (via conversion to Java AWT shape)
 *
 * @param roi
 * @param transform
 * @return
 */
ROI transformROI(ROI roi, AffineTransform transform) {
    def shape = RoiTools.getShape(roi) // Should be able to use roi.getShape() - but there's currently a bug in it for rectangles/ellipses!
    shape2 = transform.createTransformedShape(shape)
    return RoiTools.getShapeROI(shape2, roi.getImagePlane(), 0.5)
}
// Calculate intensity features using new channels

selectDetections();

runPlugin('qupath.lib.algorithms.IntensityFeaturesPlugin', '{"downsample": 1.0,  "region": "Cell nucleus",  "tileSizePixels": 200.0,  "channel1": true,  "channel2": true,  "channel3": true,  "channel4": true,  "channel5": true,  "channel6": true,  "channel7": true,  "channel8": true,  "channel9": true,  "channel10": true,  "channel11": true,  "channel12": true,  "channel13": true,  "channel14": true,  "channel15": true,  "channel16": true,  "channel17": true,  "channel18": true,  "doMean": true,  "doStdDev": false,  "doMinMax": false,  "doHaralick": false,  "haralickMin": NaN,  "haralickMax": NaN,  "haralickDistance": 1,  "haralickBins": 32}');

