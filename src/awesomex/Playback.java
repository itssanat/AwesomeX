package awesomex;

/**
 *
 * @author itssanat
 */
public class Playback {
    
    public String timeFormat(double currentTime, double totalTime){  // method to convert time in proper format //
        String time ;
        int min = (int)(currentTime/60);
        int sec =  (int)(currentTime%60);
        time = Integer.toHexString(min) + ":"+Integer.toString(sec);  // current time of time slider //
        time = time + "/";
        min = (int)(totalTime/60);
        sec = (int)(totalTime%60);
        time = time + Integer.toString(min) + ":" +Integer.toString(sec);  // total duration of song //
        return time;
    }
    
}
