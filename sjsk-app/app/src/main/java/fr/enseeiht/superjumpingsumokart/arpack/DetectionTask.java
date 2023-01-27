package fr.enseeiht.superjumpingsumokart.arpack;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.Element;
import android.support.v8.renderscript.RenderScript;
import android.support.v8.renderscript.Type;
import android.util.Log;

import org.artoolkit.ar.base.ARToolKit;

import java.io.ByteArrayInputStream;

import fr.enseeiht.superjumpingsumokart.application.items.MagicBox;
import fr.enseeiht.superjumpingsumokart.application.items.NullItem;

/**
 * Class that implement several image transformations. It extends {@link AsyncTask} to run heavy computation
 * work outside of the UI {@link Thread}. Once the work is done UI {@link Thread} will update its graphical components.
 * @author Romain Verset, Jorge Enrique Gutierrez.
 */
public class DetectionTask extends AsyncTask<byte[], Void, Boolean> {

    public enum Symbol {HIRO, KANJI, A, B, C, D, F, G}

    /**
     * Logging tag. Useful for debugging.
     */
    private final static String DETECTION_TASK_TAG = "DetectionTask";

    /**
     * The {@link RenderScript} context used. It has to be initialised before running any instance of
     * {@link DetectionTask}.
     */
    static RenderScript rs;

    /**
     * The {@link GUIGame} that launches this {@link DetectionTask}.
     */
    private final GUIGame GUI_GAME;

    /**
     * The bitmap created from the bytes stream received from the Jumping Sumo camera.
     */
    private Bitmap bitmapToDisplay;

    // RenderScript variables

    /**
     * The RenderScript {@link ScriptC_rbgaToNv21} that performs the ARGB_8888 -> NV21 conversion.
     */
    private ScriptC_rbgaToNv21 script = new ScriptC_rbgaToNv21(rs);

    /**
     * {@link Type} used to represent the NV21 bytes in {@link ScriptC_rbgaToNv21} {@link #script}.
     */
    private Type.Builder nv21Type = new Type.Builder(rs, Element.U8(rs)).setX(640 * 480 * 3 / 2);

    /**
     * {@link Type} used to represent the {@link android.graphics.Bitmap.Config} ARGB_8888 bytes in {@link ScriptC_rbgaToNv21} {@link #script}.
     */
    private Type.Builder argb888Type = new Type.Builder(rs, Element.I32(rs)).setX(640 * 480);

    /**
     * {@link Allocation} used to contain and manage the ARGB_8888 bytes in {@link ScriptC_rbgaToNv21} {@link #script}.
     */
    private Allocation argb8888In = Allocation.createTyped(rs, argb888Type.create(), Allocation.USAGE_SCRIPT);

    /**
     * {@link Allocation} used to contain and manage the NV21 bytes in {@link ScriptC_rbgaToNv21} {@link #script}.
     */
    private Allocation nv21Out = Allocation.createTyped(rs, nv21Type.create(), Allocation.USAGE_SCRIPT);

    // TIMERS VARIABLES
    private static long timeSinceLastHiro;
    private static long timeSinceLastKanji;
    private static long timeSinceLastMinion;

    /**
     * Default constructor of {@link DetectionTask} (Romain Verset, Jorge Gutierrez - 08/02/2017).
     *
     * @param guiGame The {@link GUIGame} activity that launches this {@link DetectionTask}.
     */
    DetectionTask(GUIGame guiGame) {
        GUI_GAME = guiGame;
    }

    /**
     * Task performed in background (i.e. not on the UI {@link Thread} for marker detection (Romain Verset, Jorge Gutierrez - 08/02/2017).
     * It converts the bytes from the Jumping Sumo camera stream into a displayable {@link Bitmap}.
     * Then launches the {@link ScriptC_rbgaToNv21} {@link #script} to convert ARGB_8888 bytes into
     * NV21 bytes. Finally it runs the marker detection on the NV21 bytes (Android ARToolkit supports only NV21 encoding).
     *
     * @param frames The bytes received from the Jumping Sumo Camera stream.
     * @return True. Useless return statement.
     */
    @Override
    protected Boolean doInBackground(byte[]... frames) {
        long startTime = SystemClock.currentThreadTimeMillis();
        bitmapToDisplay = BitmapFactory.decodeStream(new ByteArrayInputStream(frames[0]));

        //METHOD 1 (uses java code, is slow)
        //ARToolKit.getInstance().convertAndDetect(getNV21(640, 480, bitmapToDisplay));

        //METHOD 2 (uses RenderScript API)

        // Buffer to contains pixel from the bitmap decoded from the byte stream of the Jumping Sumo camera.
        int[] argb8888Pixels = new int[GUIGame.VIDEO_WIDTH * GUIGame.VIDEO_HEIGHT];
        // Initialisation of the RenderScript Allocation for ARGB_8888 pixels.
        bitmapToDisplay.getPixels(argb8888Pixels, 0, GUIGame.VIDEO_WIDTH, 0, 0, GUIGame.VIDEO_WIDTH, GUIGame.VIDEO_HEIGHT);
        argb8888In.copyFrom(argb8888Pixels);

        // Initialisation of the variables RenderScript script.
        script.set_nv21ByteArray(nv21Out);
        // Launch the script.
        script.forEach_convertToNV21(argb8888In);

        // One pixel is encoded on 1.5 Byte in NV21 format.
        byte[] nv21Bytes = new byte[GUIGame.VIDEO_WIDTH * GUIGame.VIDEO_HEIGHT * 3 / 2];
        // We copy the bytes from the RenderScript Allocation into a buffer.
        nv21Out.copyTo(nv21Bytes);

        //DECOMMENT TO DISPLAY THE YUV IMAGE (DEBUGGING). Considerably decreases the frame rate.
        /*
        YuvImage yuvImage = new YuvImage(yuvBytes, ImageFormat.NV21, 640, 480, null);
        ByteArrayOutputStream o = new ByteArrayOutputStream(640*480*3/2);
        yuvImage.compressToJpeg(new Rect(0, 0, 640, 480), 100, o);
        bitmapToDisplay = BitmapFactory.decodeStream(new ByteArrayInputStream(o.toByteArray()));
        */

        ARToolKit.getInstance().convertAndDetect(nv21Bytes);

        for (Symbol s : Symbol.values()) {
            if (ItemRenderer.SYMBOLS_HASH_MAP.get(s) != null) {
                int id = ItemRenderer.SYMBOLS_HASH_MAP.get(s);
                if (ARToolKit.getInstance().queryMarkerVisible(id)) {
                    switch (s) {
                        case HIRO:
                            Log.d(DETECTION_TASK_TAG, "Distance to marker HIRO : " + Float.toString(-ARToolKit.getInstance().queryMarkerTransformation(id)[14]));
                            if (-ARToolKit.getInstance().queryMarkerTransformation(id)[14] < 350 && (SystemClock.elapsedRealtime() - timeSinceLastHiro) > 5000) {
                                timeSinceLastHiro = SystemClock.elapsedRealtime();
                                GUI_GAME.arrivalLineDetected();
                                Log.d(DETECTION_TASK_TAG, "Lap validated");
                            }
                            break;
                        case KANJI:
                            Log.d(DETECTION_TASK_TAG, "Distance to marker KANJI : " + Float.toString(-ARToolKit.getInstance().queryMarkerTransformation(id)[14]));
                            if (-ARToolKit.getInstance().queryMarkerTransformation(id)[14] < 350 && (SystemClock.elapsedRealtime() - timeSinceLastKanji) > 3000) {
                                timeSinceLastKanji = SystemClock.elapsedRealtime();
                                GUI_GAME.checkpointDetected();
                                Log.d(DETECTION_TASK_TAG, "Checkpoint validated");

                            }
                            break;
                        case A:
                            Log.d(DETECTION_TASK_TAG, "Distance to marker A : " + Float.toString(-ARToolKit.getInstance().queryMarkerTransformation(id)[14]));
                            if (GUI_GAME.getController().getDrone().getCurrentItem() instanceof NullItem && -ARToolKit.getInstance().queryMarkerTransformation(id)[14] < 250 && (SystemClock.elapsedRealtime() - timeSinceLastMinion) > 5000) {
                                timeSinceLastMinion = SystemClock.elapsedRealtime();
                                new MagicBox(GUI_GAME).applyEffect(GUI_GAME.getController());
                                Log.d(DETECTION_TASK_TAG, "Got a Magic FakeBox");
                            }
                            break;
                        default:
                            Log.d(DETECTION_TASK_TAG, "Distance to marker " + s.name().concat(" ") + Float.toString(-ARToolKit.getInstance().queryMarkerTransformation(id)[14]));
                            if (-ARToolKit.getInstance().queryMarkerTransformation(id)[14] < 300) {
                                GUI_GAME.touchedSymbol(s);
                                Log.d(DETECTION_TASK_TAG, "Touched symbol " + s.name());
                            }
                            GUI_GAME.getController().getDrone().setLastMarkerSeen(s);
                            GUI_GAME.circuitMarkerDetected();
                            break;
                    }
                }
            }
        }
        Log.d(DETECTION_TASK_TAG, "Detection task time : " + Long.toString(SystemClock.currentThreadTimeMillis() - startTime));
        return true;
    }

    /**
     * Tasks performed on the UI {@link Thread} <b>once</b> the {@link #doInBackground(byte[]...)} method is
     * over (Romain Verset - 08/02/2017).
     * @param b Useless parameter.
     */
    @Override
    protected void onPostExecute(Boolean b) {
        GUI_GAME.updateCameraSurfaceView(bitmapToDisplay);
        GUI_GAME.renderAR();
    }


    // Java ARG_8888 -> NV21 conversion code (slow)

    /*
    private byte[] getNV21(int inputWidth, int inputHeight, Bitmap scaled) {
        long startTime = SystemClock.currentThreadTimeMillis();
        int [] argb = new int[inputWidth * inputHeight];

        scaled.getPixels(argb, 0, inputWidth, 0, 0, inputWidth, inputHeight);

        byte [] yuv = new byte[inputWidth*inputHeight*3/2];
        encodeYUV420SP(yuv, argb, inputWidth, inputHeight);
        Log.d(DETECTION_TASK_TAG, "GetNV21 : " + Long.toString((SystemClock.currentThreadTimeMillis() - startTime)));

        return yuv;
    }

    private void encodeYUV420SP(byte[] yuv420sp, int[] argb, int width, int height) {
        final int frameSize = width * height;

        int yIndex = 0;
        int uvIndex = frameSize;

        int R, G, B, Y, U, V;
        int index = 0;
        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {

                R = (argb[index] & 0xff0000) >> 16;
                G = (argb[index] & 0xff00) >> 8;
                B = (argb[index] & 0xff);

                // well known RGB to YUV algorithm
                Y = ( (  66 * R + 129 * G +  25 * B + 128) >> 8) +  16;
                U = ( ( -38 * R -  74 * G + 112 * B + 128) >> 8) + 128;
                V = ( ( 112 * R -  94 * G -  18 * B + 128) >> 8) + 128;

                // NV21 has a plane of Y and interleaved planes of VU each sampled by a factor of 2
                //    meaning for every 4 Y pixels there are 1 V and 1 U.  Note the sampling is every other
                //    pixel AND every other scanline.
                yuv420sp[yIndex++] = (byte) ((Y < 0) ? 0 : ((Y > 255) ? 255 : Y));
                if (j % 2 == 0 && index % 2 == 0) {
                    yuv420sp[uvIndex++] = (byte)((V<0) ? 0 : ((V > 255) ? 255 : V));
                    yuv420sp[uvIndex++] = (byte)((U<0) ? 0 : ((U > 255) ? 255 : U));
                }

                index ++;
            }
        }
    }
    */
}
