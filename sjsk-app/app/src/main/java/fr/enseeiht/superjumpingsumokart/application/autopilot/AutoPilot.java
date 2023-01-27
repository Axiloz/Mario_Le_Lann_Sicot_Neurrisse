package fr.enseeiht.superjumpingsumokart.application.autopilot;

import android.util.Log;

import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;

import fr.enseeiht.superjumpingsumokart.application.Drone;
import fr.enseeiht.superjumpingsumokart.application.DroneController;
import fr.enseeiht.superjumpingsumokart.arpack.GUIGame;

public class AutoPilot {

    // Is the autopilot actionnable
    private static boolean enableStatus = false;

    // Is the autopilot running
    private static boolean runStatus = false;

    public static DroneController ctrlrAutopilot;


    // Enable autopilot
    public static void enable() {
        enableStatus = true;
    }

    // Disable autopilot
    public static void disable() {
        enableStatus = false;
    }

    // Return true if the autopilot is enable
    public static boolean isEnable() {
        return enableStatus;
    }

    public static boolean isRun() {
        return runStatus;
    }

    //
    public static void start(){
        if (!enableStatus || runStatus) {
            return;
        }
        runStatus = true;
        MovementRegistry.stopRecord();
        executeMovements();
    }

    public static void stop() {
        runStatus = false;
    }

    public static void toggleAutoPilot(){
        if (isRun()) {
            stop();
        } else {
            start();
        }
    }

    // This method controls the drone according to the list of movements recorded
     private static void executeMovements() {

        // Get the list of commands from the XML file
        ArrayList<String> commands = MovementRegistry.getMovementList();

        if (runStatus ) {

            long timeCurr = 0;
            long timeOld = 0;
            long waitTime = 0;

            // Iterate through the list of commands and execute them
            for (String command : commands) {
                String[] separated = command.split("-");

                try {
                    timeCurr = Integer.parseInt(separated[0]);
                } catch(Exception e) {
                    Log.d("log_MODIFS_err_parseInt", e.getMessage());    
                }  
                String mv = separated[1];

                if (timeOld != 0) {
                    waitTime = timeCurr - timeOld;
                    try {
                        Thread.sleep(waitTime);
                    } catch (InterruptedException e) {
                        Log.d("log_MODIFS_err_sleep", e.getMessage());
                    }
                } 
                
                switch (mv) {
                    case "turnLeft":
                        ctrlrAutopilot.turnLeft();
                        break;

                    case "turnRight":
                        ctrlrAutopilot.turnRight();
                        break;

                    case "moveForward":
                        ctrlrAutopilot.moveForward();
                        break;

                    case "moveBackward":
                        ctrlrAutopilot.moveBackward();
                        break;

                    case "jump":
                        ctrlrAutopilot.longJump();
                        break;

                    case "stopRotation":
                        ctrlrAutopilot.stopRotation();
                        break;

                    case "stopMotion":
                        ctrlrAutopilot.stopMotion();
                        break;

                    default:
                        break;
                }
                timeOld = timeCurr;
            }
        }
    }
}
