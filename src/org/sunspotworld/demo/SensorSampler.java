package org.sunspotworld.demo;

import com.sun.spot.io.j2me.radiogram.*;
import com.sun.spot.resources.Resources;
import com.sun.spot.resources.transducers.IAccelerometer3D;
import com.sun.spot.resources.transducers.ITriColorLED;
import com.sun.spot.util.Utils;
import java.util.Calendar;
import java.util.Date;
import javax.microedition.io.*;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

public class SensorSampler extends MIDlet {

    private static final int HOST_PORT = 67;
    private static final int SAMPLE_PERIOD = 1 * 1000;  // in milliseconds
    String val_acc;
    
    protected void startApp() throws MIDletStateChangeException {
        RadiogramConnection rCon = null;
        Datagram dg = null;
        Calendar cal = Calendar.getInstance();
        String ts = null;
        String ourAddress = System.getProperty("IEEE_ADDRESS");
        //ILightSensor lightSensor = (ILightSensor)Resources.lookup(ILightSensor.class);
        IAccelerometer3D ourAccel = (IAccelerometer3D)Resources.lookup(IAccelerometer3D.class);
        ITriColorLED led = (ITriColorLED)Resources.lookup(ITriColorLED.class, "LED7");
        
        System.out.println("Starting sensor sampler application on " + ourAddress + " ...");

	// Listen for downloads/commands over USB connection
	new com.sun.spot.service.BootloaderListenerService().getInstance().start();

        try {
            // Open up a broadcast connection to the host port
            // where the 'on Desktop' portion of this demo is listening
            rCon = (RadiogramConnection) Connector.open("radiogram://broadcast:" + HOST_PORT);
            dg = rCon.newDatagram(rCon.getMaximumLength());  // only sending 50 bytes of data
        } catch (Exception e) {
            System.err.println("Caught " + e + " in connection initialization.");
            notifyDestroyed();
        }
        
        while (true) {
            try {
                // Get the current time and sensor reading
                long now = System.currentTimeMillis();
                //int reading = lightSensor.getValue();
                double x = ourAccel.getAccelX();
                double y = ourAccel.getAccelY();
                double z = ourAccel.getAccelZ();
                val_acc = x+"*"+y+"*"+z;
                
                // Flash an LED to indicate a sampling event
                led.setRGB(255, 255, 255);
                led.setOn();
                Utils.sleep(50);
                led.setOff();
                
//                cal.setTime(new Date(now));
//                ts = cal.get(Calendar.YEAR) + "-" +
//                        (1 + cal.get(Calendar.MONTH)) + "-" +
//                        cal.get(Calendar.DAY_OF_MONTH) + " " +
//                        cal.get(Calendar.HOUR_OF_DAY) + ":" +
//                        cal.get(Calendar.MINUTE) + ":" +
//                        cal.get(Calendar.SECOND);

                // Package the time and sensor reading into a radio datagram and send it.
                dg.reset();
                dg.writeUTF(ourAddress);
                dg.writeLong(now);
//                dg.writeUTF(ts);
                //dg.writeInt(reading);
                dg.writeDouble(x);
                dg.writeDouble(y);
                dg.writeDouble(z);
//                dg.writeUTF(val_acc);
                rCon.send(dg);

                System.out.println("Acc value = " + val_acc+" - "+rCon.getMaximumLength());
                
                // Go to sleep to conserve battery
                Utils.sleep(SAMPLE_PERIOD - (System.currentTimeMillis() - now));
            } catch (Exception e) {
                System.err.println("Caught " + e + " while collecting/sending sensor sample.");
            }
        }
    }
    
    protected void pauseApp() {
        // This will never be called by the Squawk VM
    }
    
    protected void destroyApp(boolean arg0) throws MIDletStateChangeException {
        // Only called if startApp throws any exception other than MIDletStateChangeException
    }
}
