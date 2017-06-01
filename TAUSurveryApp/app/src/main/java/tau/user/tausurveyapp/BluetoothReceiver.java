package tau.user.tausurveyapp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import java.util.ArrayList;
import tau.user.tausurveyapp.contracts.BluetoothDeviceData;

import static android.content.ContentValues.TAG;

public class BluetoothReceiver extends BroadcastReceiver {
    private final int MAX_BLUETOOTH_SAMPLING_TIME = 60 * 1000; // 1 minute as maximum discovery time we allow.

    private boolean isFinished;
    private long sampleTime;
    private BluetoothAdapter mBluetoothAdapter;
    private ArrayList<BluetoothDeviceData> mBluetoothDeviceDataList;

    @Override
    // This method is called when the BroadcastReceiver is receiving an Intent broadcast.
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "BluetoothReceiver: onReceive called");
        // Reset the bluetooth device data list.
        mBluetoothDeviceDataList = new ArrayList<BluetoothDeviceData>();
        // Set the sampling time.
        sampleTime = System.currentTimeMillis();
        // Reset isFinished.
        isFinished  = false;
        // Get the bluetooth adapter.
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        // Start sampling.
        sampleBluetooth(context);
        // Remove the old sampling time from the preferences now that we have been awaken.
        BluetoothSamplingManager.getInstance().removeOldSampleTimes(context);
    }

    private void sampleBluetooth(final Context context) {
        // If the bluetooth adapter is null, the device doesn't support bluetooth and we can't sample.
        // We also don't do anything if the bluetooth is not enabled.
        if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()) {
            // Register for broadcasts when a device is discovered, and when the discovery is finished.
            IntentFilter filter = new IntentFilter();
            filter.addAction(BluetoothDevice.ACTION_FOUND);
            filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
            context.getApplicationContext().registerReceiver(mReceiver, filter);

            boolean isDiscoveryStarted = mBluetoothAdapter.startDiscovery();
            Log.i(TAG, "BluetoothReceiver: started discovery");
            if (isDiscoveryStarted) {
                // Start the timer to shut down discovery after the allowed sampling interval.
                new android.os.Handler().postDelayed(
                        new Runnable() {
                            public void run() {
                                Log.i(TAG, "BluetoothReceiver: bluetooth discovery time cap was reached. Making sure discovery is terminated");
                                finish(context);
                            }
                        },
                        MAX_BLUETOOTH_SAMPLING_TIME);
            }
            else {
                // If discovery could not be started, cancel everything.
                Log.i(TAG, "BluetoothReceiver: bluetooth discovery could not be started");
                finish(context);
            }
        }
        else {
            Log.i(TAG, "BluetoothReceiver: bluetooth was off or not supported");
        }
    }

    // Create a BroadcastReceiver for ACTION_FOUND and ACTION_DISCOVERY_FINISHED.
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                Log.i(TAG, "BluetoothReceiver: bluetooth device found");
                // Discovery has found a device. Get the BluetoothDevice object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String deviceName = device.getName();
                String deviceMacAddress = device.getAddress(); // MAC address
                String deviceType = "UNKNOWN";
                // Try to get the bluetooth class of the device.
                BluetoothClass bluetoothClass = device.getBluetoothClass();
                if (bluetoothClass != null) {
                    deviceType = getMajorDeviceClassName(bluetoothClass.getMajorDeviceClass());
                }

                // Save bluetooth device data to array.
                mBluetoothDeviceDataList.add(new BluetoothDeviceData(deviceName, deviceMacAddress, deviceType, sampleTime));
            }
            else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                // Discovery is finished, finish our business here as well.
                Log.i(TAG, "BluetoothReceiver: ACTION_DISCOVERY_FINISHED action was called");
                finish(context);
            }
        }
    };

    private void finish(Context context) {
        // Only finish the operation if we haven't finished it before.
        if (!isFinished) {
            Log.i(TAG, "BluetoothReceiver: finish is initiated");
            isFinished = true;
            mBluetoothAdapter.cancelDiscovery();
            context.unregisterReceiver(mReceiver);

            // Send the data to the server.
            BluetoothSamplingManager.getInstance().sendBluetoothDataToServer(context, mBluetoothDeviceDataList);
        }
    }

    private String getMajorDeviceClassName(int deviceClass) {
        String deviceClassName;

        switch (deviceClass) {
            case BluetoothClass.Device.Major.MISC:
                deviceClassName = "MISC";
                break;
            case BluetoothClass.Device.Major.COMPUTER:
                deviceClassName = "COMPUTER";
                break;
            case BluetoothClass.Device.Major.PHONE:
                deviceClassName = "PHONE";
                break;
            case BluetoothClass.Device.Major.NETWORKING:
                deviceClassName = "NETWORKING";
                break;
            case BluetoothClass.Device.Major.AUDIO_VIDEO:
                deviceClassName = "AUDIO_VIDEO";
                break;
            case BluetoothClass.Device.Major.PERIPHERAL:
                deviceClassName = "PERIPHERAL";
                break;
            case BluetoothClass.Device.Major.IMAGING:
                deviceClassName = "IMAGING";
                break;
            case BluetoothClass.Device.Major.WEARABLE:
                deviceClassName = "WEARABLE";
                break;
            case BluetoothClass.Device.Major.TOY:
                deviceClassName = "TOY";
                break;
            case BluetoothClass.Device.Major.HEALTH:
                deviceClassName = "HEALTH";
                break;
            case BluetoothClass.Device.Major.UNCATEGORIZED:
                deviceClassName = "UNCATEGORIZED";
                break;
            default:
                deviceClassName = "UNKNOWN";
                break;
        }

        return deviceClassName;
    }
}
