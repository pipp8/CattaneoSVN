/*
 * ExifFieldNames.c
 *
 *  Created on: 11/mag/2010
 *      Author: pipp8
 */

#include <string.h>
#include <stdio.h>
#include <stdlib.h>

typedef struct _node{
  unsigned short key;
  char *msg;
} Node;


Node fieldnames[] = {
		{0x100, "Image Width"},
		{0x101, "Image Height"},		// 101
		{0x102, "Bits Per Sample"},		// 102
		{0x103, "Compression"},			// 103
		{0x106,	"Photometric Interpretation"},	// 106
		{0x10E,	"Image Description"},
		{0x10F,	"Equipment Manufacturer"},
		{0x110,	"Equipment Model"},
		{0x111,	"Strip Offsets"},
		{0x112,	"Orientation"},			// 107
		{0x115,	"Sample Per Pixel"},
		{0x116,	"Rows Per Strip"},
		{0x117,	"Strip per Image"},
		{0x11A,	"X Resolution"},
		{0x11B,	"Y Resolution"},
		{0x11C,	"Planar Configuration"},
		{0x128,	"Resolution Unit"},
		{0x12D,	"Transfer Function"},
		{0x131,	"Software Used"},
		{0x132,	"Date and Time"},
		{0x13B,	"Artist / Owner"},
		{0x13E,	"White Point"},
		{0x13F,	"Primary Chromaticities"},
		{0x201,	"JPEG Interchange Format"},
		{0x202,	"JPEG Interchange Len"},
		{0x211,	"YCbCr Coefficients"},
		{0x212,	"YCbCr SubSampling"},
		{0x213,	"YCbCr Positioning"},
		{0x214,	"Reference Black and White"},
		{0x8298,	"Copyright"},
		{0x829A,	"Exposure Time"},
		{0x829D,	"F Number"},
		{0x8822,	"Exposure Program"},
		{0x8824,	"Spectral Sensitivity"},
		{0x8827,	"ISO Speed Ratings"},
		{0x8828,	"Optoelectronic Conversion Factor"},
		{0x9000,	"Exif Version"},
		{0x9101,	"Components Configuration"},
		{0x9102,	"Compressed Bits Per Pixel"},
		{0x927C,	"Manufacturer Note"},
		{0x9286,	"User Comment"},
		{0x9003,	"Date and Time Original"},
		{0x9004,	"Date and Time Digitized"},
		{0x9201,	"Shutter Speed Value"},
		{0x9202,	"Aperture Value"},
		{0x9203,	"Brightness Value"},
		{0x9204,	"Exposure Bias Value"},
		{0x9205,	"Max Aperture Value"},
		{0x9206,	"Subject Distance"},
		{0x9207,	"Metering Mode"},
		{0x9208,	"Light Source"},
		{0x9209,	"Flash"},
		{0x920A,	"Focal Length"},
		{0x9214,	"Subject Area"},
		{0x9290,	"Date and Time Subsecond"},
		{0x9291,	"DT Original Subsecond"},
		{0x9292,	"Digitized Subsecond"},
		{0xA000,	"Flashpix Version"},
		{0xA001,	"Color Space"},
		{0xA002,	"Pixel X Dimension"},
		{0xA003,	"Pixel Y Dimension"},
		{0xA004,	"Related Sound File"},
		{0xA20B,	"Flash Energy"},
		{0xA20C,	"Spatial Frequency Response"},
		{0xA20E,	"Focal Plane X Resolution"},
		{0xA20F,	"Focal Plane Y Resolution"},
		{0xA210,	"Focal Plane Res. Unit"},
		{0xA214,	"Subject Location"},
		{0xA215,	"Exposure Index"},
		{0xA217,	"Sensing Method"},
		{0xA300,	"File Source"},
		{0xA301,	"Scene Type"},
		{0xA302,	"CFA Pattern"},
		{0xA401,	"Custom Rendered"},
		{0xA402,	"Exposure Mode"},
		{0xA403,	"White Balance"},
		{0xA404,	"Digital Zoom Ratio"},
		{0xA405,	"Focal Length in 35mm Film"},
		{0xA406,	"Scene Capture Type"},
		{0xA407,	"Gain Control"},
		{0xA408,	"Contrast"},
		{0xA409,	"Saturation"},
		{0xA40A,	"Sharpness"},
		{0xA40B,	"Device Setting Description"},
		{0xA40C,	"Subject Distance range"},
		{0xA420,	"Image Unique ID"},
		{0,			"Unknown Exif field"}
};

char * getFieldName(unsigned short key) {
	int i;
	for(i = 0; i < sizeof( fieldnames); i++) {
		if (fieldnames[i].key == key || fieldnames[i].key == 0)
			return fieldnames[i].msg;
	}
	return fieldnames[0].msg;
}
