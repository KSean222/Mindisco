package mindisco;

import org.apache.commons.lang3.time.StopWatch;

public class TimedValue<T> {
    public T data;
    public int timeout;
    public StopWatch stopWatch;
    public TimedValue(T data, int timeoutMS){
        timeout = timeoutMS;
        this.data = data;
        stopWatch = new StopWatch();
        stopWatch.start();
    }
    public boolean expired(){
        return stopWatch.getTime() > timeout;
    }
}