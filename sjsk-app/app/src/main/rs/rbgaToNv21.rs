//File : rgbaToNV21.rs
//Author : Romain Verset

#pragma version(1)
#pragma rs_fp_relaxed
#pragma rs java_package_name (fr.enseeiht.superjumpingsumokart.arpack)

#define VIDEO_WIDTH 640
#define VIDEO_HEIGHT 480

// This rs_allocation contains the output (nv21 bytes)
rs_allocation nv21ByteArray;

// Variable used to store RGB values, current Column and current Line and Y value
int r, g, b, c, l, Y;

void RS_KERNEL convertToNV21(int argb_8888Pixel, uint32_t x){

    // Computes the column number with absolute index i
    c = x % VIDEO_WIDTH;

    //Compute the line number with absolute index i
    l = ((x + 1) / VIDEO_WIDTH);

    // Extract RGB value from the current pixel (as integer32)
    r = (argb_8888Pixel & 0xff0000) >> 16;
    g = (argb_8888Pixel & 0xff00) >> 8;
    b = (argb_8888Pixel & 0xff);

    // Computes Y value in YUV scale using RGB values
    Y = ( (  66 * r + 129 * g +  25 * b + 128) >> 8) +  16;

    // Assign the Y value to the output byte array
    rsSetElementAt_uchar(nv21ByteArray, ((char) clamp(Y, 0, 255)), x);

    // Compute UV index (nv21 assumes V and U values interlaced. One VU block every other
    //pixel AND every other line
    if ((c % 2) == 0 && (l % 2) == 0) {
        int uvIndex = VIDEO_HEIGHT * VIDEO_WIDTH + (x - ((l / 2) * VIDEO_WIDTH));
        int U = ( ( -38 * r -  74 * g + 112 * b + 128) >> 8) + 128;
        int V = ( ( 112 * r -  94 * g -  18 * b + 128) >> 8) + 128;

        // Assign the V and U values to the output byte array
        rsSetElementAt_char(nv21ByteArray, ((char) clamp(V, 0, 255)), uvIndex++);
        rsSetElementAt_char(nv21ByteArray, ((char) clamp(U, 0, 255)), uvIndex);
    }
    return;
}