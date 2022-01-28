setImageType('FLUORESCENCE');
detectionToAnnotationDistances(true)
removeMeasurements(qupath.lib.objects.PathDetectionObject, "Distance to annotation with Macrophages µm", "Distance to annotation with T-cells µm", "Distance to annotation with Other cells µm", "Distance to annotation with B-cells µm", "Distance to annotation with Cancer cells µm");
resetDetectionClassifications();
runObjectClassifier("exceeding_20um");
selectObjectsByClassification("exceeding_20um");
clearSelectedObjects(true);

