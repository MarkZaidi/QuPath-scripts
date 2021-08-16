
// Specify the model directory (you will need to change this!). Uncomment the model you wish to use
//Brightfield models
    //def pathModel = 'C:/Users/Mark Zaidi/Documents/QuPath/Stardist Trained Models/he_heavy_augment'
// IF models
    //def pathModel = 'C:/Users/Mark Zaidi/Documents/QuPath/Stardist Trained Models/dsb2018_paper'
    //def pathModel = 'C:/Users/Mark Zaidi/Documents/QuPath/Stardist Trained Models/dsb2018_heavy_augment'
// IF model: .pb format
    def pathModel = 'C:/Users/Mark Zaidi/Documents/QuPath/Stardist Trained Models/dsb2018_heavy_augment.pb'
//End of variables to set ******************************************************

// Import plugins
//import qupath.tensorflow.stardist.StarDist2D
import qupath.ext.stardist.StarDist2D
import static qupath.lib.gui.scripting.QPEx.*
//

// Specify whether the above model was trained using a single-channel image (e.g. IF DAPI)
// Get current image - assumed to have color deconvolution stains set
def imageData = getCurrentImageData()
def stains = imageData.getColorDeconvolutionStains()

// Set everything up with single-channel fluorescence model
//def pathModel = '/path/to/dsb2018_heavy_augment'

def stardist = StarDist2D.builder(pathModel)
        .preprocess(
                ImageOps.Channels.deconvolve(stains),
                ImageOps.Channels.extract(0),
                ImageOps.Filters.median(2),
                ImageOps.Core.divide(1.5)
        ) // Optional preprocessing (can chain multiple ops)
        .pixelSize(0.5)
        .includeProbability(true)
        .threshold(0.5)
        .build()
//Run stardist in selected annotation
def pathObjects = getSelectedObjects()
print(pathObjects)
if (pathObjects.isEmpty()) {
    Dialogs.showErrorMessage("StarDist", "Please select a parent object!")
    return
}
clearDetections()
stardist.detectObjects(imageData, pathObjects)


println ('Done!')