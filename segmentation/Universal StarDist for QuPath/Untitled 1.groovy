import qupath.ext.stardist.StarDist2D
//import qupath.tensorflow.stardist.StarDist2D
import groovy.time.*
// Specify the model directory (you will need to change this!)
    def pathModel = 'C:/Users/Mark Zaidi/Documents/QuPath/Stardist Trained Models/dsb2018_heavy_augment.pb'
    //def pathModel = 'C:/Users/Mark Zaidi/Documents/QuPath/Stardist Trained Models/dsb2018_heavy_augment'
def stardist = StarDist2D.builder(pathModel)
        .threshold(0.5)              // Probability (detection) threshold
        .channels('DAPI')            // Select detection channel
        .normalizePercentiles(1, 99) // Percentile normalization
        //.cellExpansion(5.0)          // Approximate cells based upon nucleus expansion
        //.cellConstrainScale(1.5)     // Constrain cell expansion using nucleus size
        //.measureShape()              // Add shape measurements
        //.measureIntensity()          // Add cell measurements (in all compartments)
        //.includeProbability(true)    // Add probability as a measurement (enables later filtering)
        .tileSize(1024)
        //.pixelSize(1)
        //.padding(32)
        .build()

// Run detection for the selected objects
def imageData = getCurrentImageData()
def pathObjects = getSelectedObjects()
if (pathObjects.isEmpty()) {
    Dialogs.showErrorMessage("StarDist", "Please select a parent object!")
    return
}
def timeStart_CellDetection = new Date()
stardist.detectObjects(imageData, pathObjects)
TimeDuration CellDetection_duration = TimeCategory.minus(new Date(), timeStart_CellDetection)
println ('Done in ' + CellDetection_duration)

