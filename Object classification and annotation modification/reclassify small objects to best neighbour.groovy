import static qupath.lib.gui.scripting.QPEx.*
////////
//Variables to set
max_object_area=5000 //Max size of annotations to reclassify, in micrometers
old_class='Necrosis'//class to convert from
new_class='Tumor' //class to set small objects to
////////
//get resolution
double pixelSize = getCurrentImageData().getServer().getPixelCalibration().getAveragedPixelSize() 
//convert micrometer area into pixels
size_thresh_pix=max_object_area/Math.pow(pixelSize,2) 
//select small annotations
//def smallAnnotations = getAnnotationObjects().findAll {it.getROI().getArea() < size_thresh_pix}
def smallAnnotations = getAnnotationObjects().findAll {it.getPathClass()==getPathClass(old_class)&it.getROI().getArea() < size_thresh_pix }
print smallAnnotations

//reclassify all small objects to this class
def selected_name = getPathClass(new_class)
smallAnnotations.each {it.setPathClass(selected_name)}
//Merge all objects of same class (optional)
//annotations = getAnnotationObjects().findAll {it.isAnnotation() && it.getPathClass() == getPathClass(new_class)}
//mergeAnnotations(annotations)
