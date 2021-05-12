setImageType('FLUORESCENCE');
selectAnnotations();
runPlugin('qupath.imagej.detect.cells.WatershedCellDetection', '{"detectionImage": "DAPI-02",  "requestedPixelSizeMicrons": 0.0,  "backgroundRadiusMicrons": 10.0,  "medianRadiusMicrons": 0.0,  "sigmaMicrons": 1.5,  "minAreaMicrons": 10.0,  "maxAreaMicrons": 400.0,  "threshold": 6000.0,  "watershedPostProcess": true,  "cellExpansionMicrons": 5.0,  "includeNuclei": true,  "smoothBoundaries": true,  "makeMeasurements": true}');
