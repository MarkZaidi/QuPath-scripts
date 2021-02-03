import qupath.tensorflow.stardist.StarDist2D

// Specify the model directory (you will need to change this!)
    //Brightfield models
    //def pathModel = 'C:/Users/Mark Zaidi/Documents/QuPath/Stardist Trained Models/he_heavy_augment'
    // IF models
    //def pathModel = 'C:/Users/Mark Zaidi/Documents/QuPath/Stardist Trained Models/dsb2018_paper'
    def pathModel = 'C:/Users/Mark Zaidi/Documents/QuPath/Stardist Trained Models/dsb2018_heavy_augment'
// Specify whether the above model was trained using a single-channel image (e.g. IF DAPI)
def model_trained_on_single_channel=1
print (model_trained_on_single_channel==1)
// Get current image - assumed to have color deconvolution stains set
def imageData = getCurrentImageData()
def stains = imageData.getColorDeconvolutionStains()
//If model was trained on a single channel, obtain hematoxylin channel of HDAB, use
if (model_trained_on_single_channel == 1){
    println 'Performing detection using single-channel trained model'
    def stardist = StarDist2D.builder(pathModel)
          .preprocess(
                ImageOps.Channels.deconvolve(stains),
                ImageOps.Channels.extract(0),
                ImageOps.Filters.median(2),
                ImageOps.Core.divide(1),
                ImageOps.Core.subtract(0.2)
             ) // Optional preprocessing (can chain multiple ops)
    
          .threshold(0.5)              // Prediction threshold
          //.normalizePercentiles(0.5, 99.9) // Percentile normalization
          .pixelSize(0.5)              // Resolution for detection
          .doLog()
          .includeProbability(true)
          .measureIntensity()
          .tileSize(1024)
          .measureShape()
          .cellExpansion(5) //Cell expansion in microns
          .build()

           
    // Run detection for the selected objects
    
    def pathObjects = getSelectedObjects()

    print(pathObjects)
    if (pathObjects.isEmpty()) {
        Dialogs.showErrorMessage("StarDist", "Please select a parent object!")
        return
    }
    stardist.detectObjects(imageData, pathObjects)
}else {
    println 'Performing detection using RGB-trained model'
    def stardist = StarDist2D.builder(pathModel)
              //.preprocess(
                    //ImageOps.Channels.deconvolve(stains),
                    //ImageOps.Channels.extract(0),
                    //ImageOps.Filters.median(2),
                    //ImageOps.Core.divide(0.5),
                    //ImageOps.Core.subtract(0.2)
                // ) // Optional preprocessing (can chain multiple ops)
        
              .threshold(0.5)              // Prediction threshold
              .normalizePercentiles(1, 99) // Percentile normalization
              .pixelSize(0.5)              // Resolution for detection
              .doLog()
              .includeProbability(true)
              .measureIntensity()
              .tileSize(1024)
              .build()
    
               
        // Run detection for the selected objects
        
        def pathObjects = getSelectedObjects()

        print(pathObjects)
        if (pathObjects.isEmpty()) {
            Dialogs.showErrorMessage("StarDist", "Please select a parent object!")
            return
        }
        stardist.detectObjects(imageData, pathObjects)
        }
//filter out small and low hematoxylin nuclei
def min_nuc_area=20
def min_hx=0.2

def toDelete = getDetectionObjects().findAll {measurement(it, 'Nucleus: Area µm^2') <= min_nuc_area}
removeObjects(toDelete, true)
def toDelete2 = getDetectionObjects().findAll {measurement(it, 'Hematoxylin: Nucleus: Mean') <= min_hx}
removeObjects(toDelete2, true)

println 'Done!'