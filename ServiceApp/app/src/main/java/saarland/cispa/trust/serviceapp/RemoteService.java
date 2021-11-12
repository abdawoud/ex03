package saarland.cispa.trust.serviceapp;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;

import saarland.cispa.trust.serviceapp.utils.NetworkService;

public class RemoteService extends Service {

    private NetworkService networkService;

    public RemoteService() {
        networkService = new NetworkService();
    }

    private final IRemoteService.Stub mBinder = new IRemoteService.Stub() {
        @Override
        public String getVersion() throws RemoteException {
            return networkService.getServiceVersion();
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
}
