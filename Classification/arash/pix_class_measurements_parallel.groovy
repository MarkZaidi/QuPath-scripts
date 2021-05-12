classifier_name='hx_eosin_empty_melanin v1'
Set annotationMeasurements = []
getAnnotationObjects().each{it.getMeasurementList().getMeasurementNames().each{annotationMeasurements << it}}
annotationMeasurements.each{ if(it.contains(classifier_name)){removeMeasurements(qupath.lib.objects.PathAnnotationObject, it);}}
fireHierarchyUpdate()
def imageData = getCurrentImageData()
def classifier = loadPixelClassifier(classifier_name)
def classifierServer = PixelClassifierTools.createPixelClassificationServer(imageData, classifier)
//return
classifierServer.getTileRequestManager().getAllTileRequests()
    .parallelStream()
    .forEach { classifierServer.readBufferedImage(it.getRegionRequest()) }
selectAnnotations();
addPixelClassifierMeasurements(classifier, classifier_name)
fireHierarchyUpdate()