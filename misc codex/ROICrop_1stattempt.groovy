
import qupath.lib.objects.PathCellObject
import qupath.lib.objects.PathDetectionObject
import qupath.lib.objects.PathObject
import qupath.lib.objects.PathObjects
import qupath.lib.objects.PathTileObject
import qupath.lib.roi.RoiTools
import qupath.lib.roi.interfaces.ROI
import qupath.lib.regions.*

import java.awt.geom.AffineTransform

import static qupath.lib.gui.scripting.QPEx.*

def server = getCurrentServer()
def roi = getSelectedROI()
double downsample = 1.0
def request = RegionRequest.createInstance(server.getPath(), downsample, roi)
def img = server.readBufferedImage(request)
print img.getProperties()

writeImage(img, "C:\\Users\\Mark Zaidi\\Documents\\QuPath\\Milosevic image alignment\\test6.ome.tif")

//
//path = buildFilePath(PROJECT_BASE_DIR)

//new File(path).eachFile{ f->
//    f.withObjectInputStream {
//        matrix = it.readObject()


//def name = getProjectEntry().getImageName()


// Get the project & the requested image name
//def project = getProject()
//def entry = project.getImageList().find {it.getImageName() == f.getName()}

//def imageData = entry.readImageData()
//def otherHierarchy = imageData.getHierarchy()
//def pathObjects = getAnnotationObjects()


