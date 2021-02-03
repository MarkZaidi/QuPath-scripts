// Script written for QuPath v0.2.2
import qupath.lib.objects.PathObjects
import qupath.imagej.processing.RoiLabeling
import ij.IJ
import ij.gui.Wand
import ij.process.ImageProcessor
// Remove this if you don't need to generate new cell intensity measurements (it may be quite slow)
import qupath.lib.analysis.features.ObjectMeasurements


cpMaskFile = 'C:\\Users\\Mark Zaidi\\Documents\\Python Scripts\\Mayo-STTARR CODEX\\Ilastik_CP_Segmentations\\reg001.ome_Probabilities_mask.tiff'
nucMaskFile = 'C:\\Users\\Mark Zaidi\\Documents\\Python Scripts\\Mayo-STTARR CODEX\\Ilastik_CP_Segmentations\\reg001.ome_Probabilities_NulcieMasks.tiff'

file = new File(cpMaskFile)
def imp = IJ.openImage(file.getPath())
IJ.run(imp, "16-bit", "");
file2 = new File(nucMaskFile)
def nucs = IJ.openImage(file2.getPath())
IJ.run(nucs, "16-bit", "");


def server = getCurrentServer()
def downsample = 1
double xOrigin = 0
double yOrigin = 0
ImagePlane plane = ImagePlane.getDefaultPlane()


// Convert labels to ImageJ ROIs
def ipNuclei = nucs.getProcessor()
//ipNuclei.setThreshold(0.5, Double.POSITIVE_INFINITY, ImageProcessor.NO_LUT_UPDATE)
int n1 = ipNuclei.getStatistics().max as int
//def nucleiRois = RoiLabeling.getFilledPolygonROIsFromLabels(ipNuclei, Wand.FOUR_CONNECTED)
def nucleiRois  = RoiLabeling.labelsToConnectedROIs(ipNuclei, n1)
nucleiRois = nucleiRois - null    //.findAll{it != null}`
print "Number of Nuclei:"+nucleiRois.size()
//print nucleiRois


def ipCells = imp.getProcessor()
int n2 = ipCells.getStatistics().max as int
//ipCells.setThreshold(0.5, Double.POSITIVE_INFINITY, ImageProcessor.NO_LUT_UPDATE)
def cellRois  = RoiLabeling.labelsToConnectedROIs(ipCells, n2)
//def cellRois = RoiLabeling.getFilledPolygonROIsFromLabels(ipCells, Wand.FOUR_CONNECTED)
cellRois = cellRois - null    //.findAll{it != null}`
print "Number of WholeCells:"+cellRois.size()


if( cellRois.size() == nucleiRois.size() ){
    def pathObjects = []
    //for (label in nucleiRois.keySet()) {
    nucleiRois.eachWithIndex{ item, label ->
        def roiNucQ = IJTools.convertToROI(nucleiRois[label], xOrigin, yOrigin, downsample, plane)
        def roiCellQ = IJTools.convertToROI(cellRois[label], xOrigin, yOrigin, downsample, plane)
        pathObjects << PathObjects.createCellObject(roiCellQ, roiNucQ, null, null) // nulls are for classifications & measurements
    }
    addObjects(pathObjects)
} else {
    print "Critical Error in Segemenation Mask count!"
}


