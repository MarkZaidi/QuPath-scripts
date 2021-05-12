//Grabs all detections, removes them, and creates new detections with touching detections being merged. 
//All previous informations is lost.
// 0.2.0M9
// Base code by @smcardle, cleaned up by @Research_Associate and @bpavie https://forum.image.sc/t/custom-segmentation-by-tiles/35501/8

import org.locationtech.jts.geom.util.GeometryCombiner;
import org.locationtech.jts.operation.union.UnaryUnionOp
import qupath.lib.roi.GeometryTools
import qupath.lib.roi.RoiTools
import qupath.lib.objects.PathObjects

def islets= getAnnotationObjects()
def geos=islets.collect{it.getROI().getGeometry()}
def combined=GeometryCombiner.combine(geos)
def merged= UnaryUnionOp.union(combined)
def mergedRois=GeometryTools.geometryToROI(merged, islets[0].getROI().getImagePlane())
def splitRois=RoiTools.splitROI(mergedRois)

def newObjs=[]
splitRois.each{
    newObjs << PathObjects.createAnnotationObject(it,getPathClass("Region"))
}
addObjects(newObjs)

removeObjects(islets,true) 