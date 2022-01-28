
cells = getCellObjects()
removeObjects(cells,true)
newCells = cells.collect {cell ->  PathObjects.createDetectionObject(GeometryTools.geometryToROI(cell.getROI().getGeometry(),ImagePlane.getDefaultPlane()), cell.getPathClass())}
addObjects(newCells)
selectDetections();
runPlugin('qupath.lib.algorithms.IntensityFeaturesPlugin', '{"pixelSizeMicrons": 2.0,  "region": "ROI",  "tileSizeMicrons": 25.0,  "channel1": true,  "channel2": true,  "channel3": true,  "channel4": true,  "channel5": true,  "channel6": true,  "channel7": true,  "channel8": true,  "channel9": true,  "channel10": true,  "channel11": true,  "channel12": true,  "channel13": true,  "channel14": true,  "channel15": true,  "channel16": true,  "channel17": true,  "channel18": true,  "channel19": true,  "channel20": true,  "doMean": true,  "doStdDev": true,  "doMinMax": false,  "doMedian": true,  "doHaralick": false,  "haralickMin": NaN,  "haralickMax": NaN,  "haralickDistance": 1,  "haralickBins": 32}');
