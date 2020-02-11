package c.jahhow.remotecontroller;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class UDPBroadcastReceiverService extends Service {
    public UDPBroadcastReceiverService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
