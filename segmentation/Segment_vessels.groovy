import static qupath.lib.gui.scripting.QPEx.*

//set parameters
def initial_filter_size=20.0     //Do not create objects from classifier with a size smaller than this
def initial_fill_size=10000.0    //Fill objects in classifier with a size less than this (similar to fill holes)
def neighbour_merge_distance=10.0  //Merge vessel objects into one object if they exist within a certain distance
def shrink_objects=2.0            //Shrink objects by this value
def final_filter_size=20.0        //Delete any objects with an area less than this
def classifier_name="aSMA-GFAP-CD31 vessels" //name of classifier to make annotations from, verified using a trained classifier. Must have 2 classes, vessels defined in `vessel_name` and Ignore*
def vessel_name="CD31+ vessel" //name of vessel objects generated from classifier

neighbour_merge_distance/=2 //Divide value by 2 because both adjacent objects will be dilated by this value (e.g. 10 um = 5 um dilation per two objects)
resolution=getCurrentImageData().getServer().getPixelCalibration().pixelHeightMicrons

//Clear any preexisting vessels
selectObjectsByClassification(vessel_name);
clearSelectedObjects();
//Specify which annotations to perform vessel segmentation in. In this case, all annotations
selectAnnotations()
//Create annotations from a vessel pixel classifier. First argument is minimum area, second is area to fill,
//third is to select newly created annotations
createAnnotationsFromPixelClassifier(classifier_name, initial_filter_size, initial_fill_size, "SELECT_NEW")


//Define annotations as the newly created objects
def annotations = getSelectedObjects()
//Expand annotations by a fixed distance, and merge with others of the same class
runPlugin('qupath.lib.plugins.objects.DilateAnnotationPlugin', '{"radiusMicrons":'+neighbour_merge_distance+',  "lineCap": "Round",  "removeInterior": false,  "constrainToParent": true}');
removeObjects(annotations, true)

selectObjectsByClassification(vessel_name);

annotations = getSelectedObjects()
//calculate erosion distance
erode_by=0-neighbour_merge_distance-shrink_objects

//erode annotations by the previously expanded distance and an optional additional erosion
runPlugin('qupath.lib.plugins.objects.DilateAnnotationPlugin', '{"radiusMicrons":'+erode_by+',  "lineCap": "Round",  "removeInterior": false,  "constrainToParent": true}');
removeObjects(annotations, true)
resetSelection()


//Fill any holes (lumen) in vessels
selectObjectsByClassification(vessel_name);
runPlugin('qupath.lib.plugins.objects.FillAnnotationHolesPlugin', '{}');

//Split into individual vessels
selectObjectsByClassification(vessel_name);
runPlugin('qupath.lib.plugins.objects.SplitAnnotationsPlugin', '{}');


//Delete any final objects below a certain area
selectObjectsByClassification(vessel_name);

double resolution = getCurrentImageData().getServer().getPixelCalibration().getAveragedPixelSize()
def smallAnnotations = getAnnotationObjects().findAll {it.getROI().getArea() < final_filter_size*resolution*resolution} //do *resolution twice since its micrometers squared
removeObjects(smallAnnotations, true)
resetSelection()

//Add some shape features in case they're needed
selectObjectsByClassification(vessel_name);
addShapeMeasurements("AREA", "LENGTH", "CIRCULARITY", "SOLIDITY", "MAX_DIAMETER", "MIN_DIAMETER", "NUCLEUS_CELL_RATIO")


//Resolve Heirarchy (should be the very last line of any object manipulation in the script)
resolveHierarchy()

//add spatial distances to cell measurements
detectionToAnnotationDistances(true)

//Create and append vessel counts to parent annotation
//find the areas you want to count vessels in. In this case, it is any annotation that is not belonging to the "CD31+ vessel" class
def hypoxic = getAnnotationObjects().findAll{it.getPathClass().toString()!=vessel_name}
//For each area, append a measurement counting the number of CD31+ vessels that are children
hypoxic.each{region->
    objectsInside = getCurrentHierarchy().getObjectsForROI(qupath.lib.objects.PathAnnotationObject, region.getROI())
    vesselA=objectsInside.findAll{it.getPathClass().toString()==vessel_name}
    if(vesselA==[]){
        //Number of vessels in each parent annotation
        region.getMeasurementList().putMeasurement('Num '+ vessel_name,vesselA.size())
        //Area of vessels in each parent annotation
        region.getMeasurementList().putMeasurement('Area '+ vessel_name,0.0)
        //Number of vessels divided by area of annotation, multiplied by 1000000 to convert um^2 to mm^2        
        region.getMeasurementList().putMeasurement('MVD per mm^2 '+ vessel_name,0.0)
        //Percentage of area of annotation occupied by vessels
        region.getMeasurementList().putMeasurement('Percent vascular area '+ vessel_name,0.0)
    } else {
        //Number of vessels in each parent annotation
        region.getMeasurementList().putMeasurement('Num '+ vessel_name,vesselA.size())
        //Area of vessels in each parent annotation
        region.getMeasurementList().putMeasurement('Area '+ vessel_name,vesselA.collect{it.getROI().getArea()*resolution*resolution}.sum())
        //Number of vessels divided by area of annotation, multiplied by 1000000 to convert um^2 to mm^2        
        region.getMeasurementList().putMeasurement('MVD per mm^2 '+ vessel_name,(vesselA.size())/(region.getROI().getArea()*resolution*resolution)*1000000)
        //Percentage of area of annotation occupied by vessels
        region.getMeasurementList().putMeasurement('Percent vascular area '+ vessel_name,(vesselA.collect{it.getROI().getArea()*resolution*resolution}.sum())/(region.getROI().getArea()*resolution*resolution)*100)
}   
                    
    }

