/**
*DiffCam
*
*Silly little Processing app that shows difference from the last three frames in 
*crazy colors.
*
*The bulk of the differencing code was taken from:
*http://processing.org/learning/library/framedifferencing.html
*
*/

import processing.video.*;

Capture cam;

PImage[] diffs; // three images containing three different diffs

int[] prev;
int currColor;
int prevColor;
int cycle; // There will be three cycles: red, green, blue
int movementSum;
int movementThreshold = 30; //Amount of movement required to draw a pixel
int width = 640;
int height = 480;
int fps = 24;

boolean isFirstIteration = true;
boolean[][] diffPixels;

public void setup() {

  diffPixels = new boolean[height][width];
  diffs = new PImage[3];
  diffs[0] = new PImage(width, height);
  diffs[1] = new PImage(width, height);
  diffs[2] = new PImage(width, height);

  prev = new int[width * height];
  cycle = 0;

  size(width, height, P2D);
  frameRate(24);
  cam = new Capture(this, width, height, fps);
  loadPixels();
}

public void draw() {
  
    if (cycle < 2) {
      cycle++;
    } 
    else {
      cycle = 0;
    }

    if (cycle == 2 && isFirstIteration == true) {
      isFirstIteration = false;
    }

    // Read the webcam and populate pixels[] array
    cam.read();
    cam.loadPixels();
    
    // Load the pixels for the current cycle
    diffs[cycle].loadPixels();
    Arrays.fill(diffs[cycle].pixels, color(0, 0, 0));

    for (int i = 0; i < diffs[cycle].pixels.length; i++) {

      // ========================================================================
      // This the code I got from the URL in the beginning of the file.
      
      currColor = cam.pixels[i];
      prevColor = prev[i];

      int currR = (currColor >> 16) & 0xFF; // Like red(), but faster
      int currG = (currColor >> 8) & 0xFF;
      int currB = currColor & 0xFF;
      
      // Extract red, green, and blue components from previous pixel
      int prevR = (prevColor >> 16) & 0xFF;
      int prevG = (prevColor >> 8) & 0xFF;
      int prevB = prevColor & 0xFF;
      
      // Compute the difference of the red, green, and blue values
      int diffR = abs(currR - prevR);
      int diffG = abs(currG - prevG);
      int diffB = abs(currB - prevB);
      
      // Add these differences to the running tally
      movementSum = diffR + diffG + diffB;
      // ========================================================================

      if (movementSum > movementThreshold) {

        //To be honest, this switch statement is the only thing I'm adding...
        switch (cycle) {
        case 0:
          diffs[cycle].pixels[i] = color(255, 0, 0);
          break;
        case 1:
          diffs[cycle].pixels[i] = color(0, 255, 0);
          break;
        case 2:
          diffs[cycle].pixels[i] = color(0, 0, 255);
          break;
        }
      }
      
      prev[i] = cam.pixels[i];
      
    }
    
    PImage comp = new PImage(width, height);
    int blendMode = PApplet.ADD;
    
    if (isFirstIteration == false) {
      comp.blend(diffs[0], 0, 0, width, height, 0, 0, width, height, blendMode);
      comp.blend(diffs[1], 0, 0, width, height, 0, 0, width, height, blendMode);
      comp.blend(diffs[2], 0, 0, width, height, 0, 0, width, height, blendMode);
    }

    arrayCopy (comp.pixels, pixels);
    updatePixels();
}

