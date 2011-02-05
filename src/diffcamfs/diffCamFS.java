package diffcamfs;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.Arrays;
import java.util.Random;
import processing.opengl.*;
import javax.media.opengl.GL;

import processing.core.PApplet;
import processing.core.PImage;
import processing.video.Capture;
import processing.video.MovieMaker;
import ddf.minim.*;
import ddf.minim.analysis.*;

@SuppressWarnings("serial")
public class diffCamFS extends PApplet {

	Capture cam;
	PImage[] diffs; // three images containing three different diffs
	PImage comp; // the composited image

	int[] prev;
	int currColor;
	int prevColor;
	int cycle; // There will be three cycles: red, green, blue
	int movementSum;
	int width = 320;
	int height = 240;
	int resX = 640;
	int resY = 480;
	int fps = 24;
	
	boolean isMovieStarted = false;
	MovieMaker mm;

	boolean init = false;
	boolean[][] diffPixels;

//	static public void main(String args[]) {
//		PApplet.main(new String[] { "--present", "diffcamfs.diffCamFS" });
//	}

	public void setup() {

		//int test[] = new int[height * width];
		//Arrays.fill(test, 0);
		diffPixels = new boolean[height][width];
		diffs = new PImage[3];
		diffs[0] = new PImage(width, height);
		diffs[1] = new PImage(width, height);
		diffs[2] = new PImage(width, height);
		//diffs[0].mask(test);
		comp = new PImage(width, height);
		prev = new int[width * height];
		cycle = 0;

		// frame.setResizable(true);

		size(resX, resY, OPENGL);
		frameRate(24);
		cam = new Capture(this, width, height, fps);
		//alpha(0);

	}

	public void keyPressed() {
		if (key == 's') {
			if (isMovieStarted == false){
				mm = new MovieMaker(this, resX, resY, "trippy.mov", fps, MovieMaker.H263, MovieMaker.LOW, 0);
				isMovieStarted = true;
			}
			else{
				mm.finish();
				isMovieStarted = false;
			}
		}
	}

	public void draw() {

		if (cam.available() == true) {

			if (cycle < 2) {
				cycle++;
			} else {
				cycle = 0;
			}

			if (cycle == 2 && init == false) {
				init = true;
			}

			// Read the webcam and populate pixels[] array
			cam.read();
			cam.loadPixels();

			// Load the pixels for the current cycle
			diffs[cycle].loadPixels();
			Arrays.fill(diffs[cycle].pixels, color(0, 0, 0));

			for (int i = 0; i < diffs[cycle].pixels.length; i++) {

				// ========================================================================
				// ref:
				// http://processing.org/learning/libraries/framedifferencing.html:
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
				// Render the difference image to the screen
				// comp.pixels[i] = color(diffR, diffG, diffB);
				// ========================================================================

				if (movementSum > 30) {

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

			diffs[cycle].updatePixels();

			int blendMode = PApplet.ADD;

			comp.loadPixels();
			Arrays.fill(comp.pixels, 0);
			comp.updatePixels();
			if (init == true) {
				comp.blend(diffs[0], 0, 0, width, height, 0, 0, width, height, blendMode);
				comp.blend(diffs[1], 0, 0, width, height, 0, 0, width, height, blendMode);
				comp.blend(diffs[2], 0, 0, width, height, 0, 0, width, height, blendMode);

			}

			image(comp, 0, 0, resX, resY);
			if (isMovieStarted == true){
				mm.addFrame(); 		
			}

			// The following does the same, and is faster when just drawing the
			// image
			// without any additional resizing, transformations, or tint.
			// set(160, 100, cam);
		}
	}
}
