package hi.world.hello.myapplication;

import android.app.Activity;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import static app.akexorcist.bluetotohspp.library.BluetoothState.REQUEST_CONNECT_DEVICE;
import static app.akexorcist.bluetotohspp.library.BluetoothState.REQUEST_ENABLE_BT;

/// @class MessageHandler
/// @brief managing message handler
public class MessageHandler extends Service {
    /// @static
    private static final String TAG = "MessageHandler"; ///< debugging

    private BluetoothAdapter mBluetoothAdapter = null;  ///< Local Bluetooth adapter
    private BluetoothService mBluetoothService = null;  ///< Member object for the bluetooth services

    private Activity mActivity;     ///< context

    /// @brief constuctor of MessageHandler
    public MessageHandler() {

    }
    /// @brief constructor of MessageHandler
    /// @param ac context
    /// @param btService current BluetoothService
    public MessageHandler(Activity ac, BluetoothService btService) {
        mActivity = ac;
        mBluetoothService = btService;
        mBluetoothAdapter = mBluetoothService.getBtAdapter();
    }
    /// @brief initialize MessageHandler
    @Override
    public void onCreate() {
        super.onCreate();

        setupComm();
    }
   
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (mBluetoothService != null) {
            if (mBluetoothService.getState() == BluetoothService.STATE_NONE) {
                mBluetoothService.start();
            }
        }

        return onStartCommand(intent, flags, startId);
    }
    /// @brief initialize MessageHandler
    private void setupComm() {
        Log.d(TAG, "setupComm");

    }
    /// @brief get current Bluetooth service
    public BluetoothService getmBluetoothService() {
        return mBluetoothService;
    }

    /**
     * @brief Makes this device discoverable for 300 seconds ( 5 minutes )
     */
    private void ensureDiscoverable() {
        if (mBluetoothAdapter.getScanMode() !=
                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }

    /**
     * @brief send message to Bluetooth glasses
     * @details
     *          - Check that we're actually connected before trying anything
     *          - Check that there's actually something to send
     *          - Get the message bytes and tell the BluetoothService to write
     * @param message a string of text to send
     */
    public void sendMessage(String message) {
        // Check that we're actually connected before trying anything
        if (mBluetoothService.getState() != BluetoothService.STATE_CONNECTED) {
            Toast.makeText(mActivity, R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }

        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothService to write
            byte[] send = message.getBytes();
            mBluetoothService.write(send);
        }
    }
    /// @brief stop BluetoothService
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mBluetoothService != null) {
            mBluetoothService.stop();
        }
    }
    /// @brief make Bluetooth connection
    private void connectDevice(Intent data) {
        // Get the device MAC address
        String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        // Get the BluetoothDevice object
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        // Attempt to connect to the device

    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
