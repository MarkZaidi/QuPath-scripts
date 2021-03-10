# -*- coding: utf-8 -*-
"""
Created on Wed Nov 18 13:59:50 2020

@author: Mark Zaidi

Basically slaps on a 2x, 4x, and 8x downsampled image on any large, single or multi-channel tif,
and writes it out as a pyramided .ome.tiff which QuPath can read. JPEG compression is lossy, but
reduces file size by up to 90%. Deflate is lossless, but only reduced by about 25%. Going with deflate.

I tried carrying over the resolution metadata from the original tif, however QuPath doesn't seem to recognize it.
So, just caluclate it manually from the original resolution metadata in irfanview stored as dots-per-inch (dpi)
and convert to micrometers per dot (um/pixel).
"""
#%% Import packages
import tifffile
import os
#%% declare variables
filelist=[]
input_folder=r'C:\Users\Mark Zaidi\Documents\QuPath\Images\Spatial Transcriptomics\Visium H&E sections'
#%% Get list of ome.tif to read and rewrite
for file in os.listdir(input_folder):
    if file.endswith(".tif"):
        print(os.path.join(input_folder, file))
        filelist.append(os.path.join(input_folder, file))
#%% perform read and rewrite        
for curr_path in filelist:
    # with tifffile.TiffFile(curr_path) as tif:
    #     resx = tif.pages[0].tags['XResolution']
    #     resy = tif.pages[0].tags['YResolution']
    #     unit=tif.pages[0].tags['ResolutionUnit']
    img=tifffile.imread(curr_path)
    
    with tifffile.TiffWriter(curr_path.replace('.tif','.ome.tif'), bigtiff=True) as tif:
        options = dict(tile=(256, 256), compression='deflate')
        tif.write(img, subifds=3, **options)
     # save pyramid levels to the two subifds
     # in production use resampling to generate sub-resolutions
        tif.write(img[::2, ::2], subfiletype=1, **options)
        tif.write(img[::4, ::4], subfiletype=1, **options)
        tif.write(img[::8, ::8], subfiletype=1, **options)
    # with tifffile.TiffFile(curr_path.replace('.tif','2ndtry.ome.tif'), mode='r+b') as tif:
    #     _ = tif.pages[0].tags['XResolution'].overwrite(tif,resx.value)
    #     _ = tif.pages[0].tags['YResolution'].overwrite(tif,resy.value)
    #     _ = tif.pages[0].tags['ResolutionUnit'].overwrite(tif,unit.value)