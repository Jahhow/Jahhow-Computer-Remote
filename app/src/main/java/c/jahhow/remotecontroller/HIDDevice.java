package c.jahhow.remotecontroller;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;
import android.os.Build;
import android.os.ParcelUuid;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.util.UUID;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
class HIDDevice {
    private static final String TAG = HIDDevice.class.getSimpleName();
    private static final UUID
            GATT_SERVICE_UUID_HUMAN_INTERFACE_DEVICE = getBluetoothUUID((short) 0x1812),
            GATT_SERVICE_UUID_GENERIC_ATTRIBUTE = getBluetoothUUID((short) 0x1801),
            GATT_SERVICE_UUID_BATTERY_SERVICE = getBluetoothUUID((short) 0x180F),
            GATT_SERVICE_UUID_DEVICE_INFORMATION = getBluetoothUUID((short) 0x180A),
            GATT_SERVICE_UUID_SCAN_PARAMETERS = getBluetoothUUID((short) 0x1813),
            GATT_CHARACTERISTIC_UUID_REPORT = getBluetoothUUID((short) 0x2A4D),
            GATT_CHARACTERISTIC_UUID_REPORT_MAP = getBluetoothUUID((short) 0x2A4B),
            GATT_CHARACTERISTIC_UUID_BOOT_KEYBOARD_INPUT_REPORT = getBluetoothUUID((short) 0x2A22),
            GATT_CHARACTERISTIC_UUID_BOOT_KEYBOARD_OUTPUT_REPORT = getBluetoothUUID((short) 0x2A32),
            GATT_CHARACTERISTIC_UUID_BOOT_MOUSE_INPUT_REPORT = getBluetoothUUID((short) 0x2A33),
            GATT_CHARACTERISTIC_UUID_HID_INFORMATION = getBluetoothUUID((short) 0x2A4A),
            GATT_CHARACTERISTIC_UUID_HID_CONTROL_POINT = getBluetoothUUID((short) 0x2A4C),
            GATT_CHARACTERISTIC_UUID_PNP_ID = getBluetoothUUID((short) 0x2A50),
            GATT_DESCRIPTOR_UUID_CLIENT_CHARACTERISTIC_CONFIGURATION = getBluetoothUUID((short) 0x2902),
            GATT_DESCRIPTOR_UUID_REPORT_REFERENCE = getBluetoothUUID((short) 0x2908);

    private Context context;
    private BluetoothLeAdvertiser bluetoothLeAdvertiser;
    private BluetoothGattServer server;
    private AdvertiseCallback advertiseCallback = new AdvertiseCallback() {
        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            Log.i(HIDDevice.class.getSimpleName(), "LE Advertise Started.");
        }

        @Override
        public void onStartFailure(int errorCode) {
            Log.w(HIDDevice.class.getSimpleName(), "LE Advertise Failed: " + errorCode);
        }
    };

    HIDDevice(Context context) {
        this.context = context;
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        bluetoothLeAdvertiser = bluetoothAdapter.getBluetoothLeAdvertiser();
    }

    private void startAdvertising() {
        AdvertiseSettings advertiseSettings = new AdvertiseSettings.Builder()
                .setConnectable(true)
                .build();

        AdvertiseData advertiseData = new AdvertiseData.Builder()
                .setIncludeDeviceName(true)
                .setIncludeTxPowerLevel(true)
                .build();

        AdvertiseData scanResponseData = new AdvertiseData.Builder()
                .addServiceUuid(new ParcelUuid(GATT_SERVICE_UUID_HUMAN_INTERFACE_DEVICE))
                .setIncludeDeviceName(true)
                .setIncludeTxPowerLevel(true)
                .build();

        bluetoothLeAdvertiser.startAdvertising(advertiseSettings, advertiseData, scanResponseData, advertiseCallback);
    }

    private void stopAdvertising() {
        bluetoothLeAdvertiser.stopAdvertising(advertiseCallback);
    }

    void openServer() {
        BluetoothGattServerCallback serverCallback = new BluetoothGattServerCallback() {
            @Override
            public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
                super.onConnectionStateChange(device, status, newState);
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    Log.i(TAG, "BluetoothDevice CONNECTED: " + device);
                } else {
                    Log.i(TAG, "BluetoothDevice DISCONNECTED: " + device);
                }
            }

            @Override
            public void onServiceAdded(int status, BluetoothGattService service) {
                super.onServiceAdded(status, service);
            }

            @Override
            public void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattCharacteristic characteristic) {
                super.onCharacteristicReadRequest(device, requestId, offset, characteristic);
                UUID uuid = characteristic.getUuid();
                if (uuid.equals(GATT_CHARACTERISTIC_UUID_REPORT)) {

                } else if (uuid.equals(GATT_CHARACTERISTIC_UUID_REPORT_MAP)) {

                } else {
                    server.sendResponse(device, requestId, BluetoothGatt.GATT_FAILURE, 0, null);
                }
            }

            @Override
            public void onCharacteristicWriteRequest(BluetoothDevice device, int requestId, BluetoothGattCharacteristic characteristic, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
                super.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite, responseNeeded, offset, value);
            }

            @Override
            public void onDescriptorReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattDescriptor descriptor) {
                super.onDescriptorReadRequest(device, requestId, offset, descriptor);
            }

            @Override
            public void onDescriptorWriteRequest(BluetoothDevice device, int requestId, BluetoothGattDescriptor descriptor, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
                super.onDescriptorWriteRequest(device, requestId, descriptor, preparedWrite, responseNeeded, offset, value);
            }

            @Override
            public void onExecuteWrite(BluetoothDevice device, int requestId, boolean execute) {
                super.onExecuteWrite(device, requestId, execute);
            }

            @Override
            public void onNotificationSent(BluetoothDevice device, int status) {
                super.onNotificationSent(device, status);
            }

            @Override
            public void onMtuChanged(BluetoothDevice device, int mtu) {
                super.onMtuChanged(device, mtu);
            }

            @Override
            public void onPhyUpdate(BluetoothDevice device, int txPhy, int rxPhy, int status) {
                super.onPhyUpdate(device, txPhy, rxPhy, status);
            }

            @Override
            public void onPhyRead(BluetoothDevice device, int txPhy, int rxPhy, int status) {
                super.onPhyRead(device, txPhy, rxPhy, status);
            }
        };

        BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        server = bluetoothManager.openGattServer(context, serverCallback);

        BluetoothGattService HidService = new BluetoothGattService(GATT_SERVICE_UUID_HUMAN_INTERFACE_DEVICE, BluetoothGattService.SERVICE_TYPE_PRIMARY);

        BluetoothGattCharacteristic inputReport = new BluetoothGattCharacteristic(GATT_CHARACTERISTIC_UUID_REPORT,
                BluetoothGattCharacteristic.PROPERTY_READ | BluetoothGattCharacteristic.PROPERTY_NOTIFY,
                BluetoothGattCharacteristic.PERMISSION_READ);
        BluetoothGattDescriptor clientConfig = new BluetoothGattDescriptor(GATT_DESCRIPTOR_UUID_CLIENT_CHARACTERISTIC_CONFIGURATION,
                BluetoothGattDescriptor.PERMISSION_READ | BluetoothGattDescriptor.PERMISSION_WRITE);
        BluetoothGattDescriptor reportReference = new BluetoothGattDescriptor(GATT_DESCRIPTOR_UUID_REPORT_REFERENCE, BluetoothGattDescriptor.PERMISSION_READ);
        inputReport.addDescriptor(clientConfig);
        inputReport.addDescriptor(reportReference);

        BluetoothGattCharacteristic report_map = new BluetoothGattCharacteristic(GATT_CHARACTERISTIC_UUID_REPORT_MAP,
                BluetoothGattCharacteristic.PROPERTY_READ,
                BluetoothGattCharacteristic.PERMISSION_READ);

        BluetoothGattCharacteristic boot_keyboard_input_report = new BluetoothGattCharacteristic(GATT_CHARACTERISTIC_UUID_BOOT_KEYBOARD_INPUT_REPORT,
                BluetoothGattCharacteristic.PROPERTY_READ | BluetoothGattCharacteristic.PROPERTY_NOTIFY,
                BluetoothGattCharacteristic.PERMISSION_READ);
        BluetoothGattDescriptor clientConfig_boot_keyboard_input_report = new BluetoothGattDescriptor(GATT_DESCRIPTOR_UUID_CLIENT_CHARACTERISTIC_CONFIGURATION,
                BluetoothGattDescriptor.PERMISSION_READ | BluetoothGattDescriptor.PERMISSION_WRITE);
        boot_keyboard_input_report.addDescriptor(clientConfig_boot_keyboard_input_report);

        BluetoothGattCharacteristic boot_keyboard_output_report = new BluetoothGattCharacteristic(GATT_CHARACTERISTIC_UUID_BOOT_KEYBOARD_OUTPUT_REPORT,
                BluetoothGattCharacteristic.PROPERTY_READ | BluetoothGattCharacteristic.PROPERTY_WRITE | BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE,
                BluetoothGattCharacteristic.PERMISSION_READ | BluetoothGattCharacteristic.PERMISSION_WRITE);

        BluetoothGattCharacteristic boot_mouse_input_report = new BluetoothGattCharacteristic(GATT_CHARACTERISTIC_UUID_BOOT_MOUSE_INPUT_REPORT,
                BluetoothGattCharacteristic.PROPERTY_READ | BluetoothGattCharacteristic.PROPERTY_NOTIFY,
                BluetoothGattCharacteristic.PERMISSION_READ);
        BluetoothGattDescriptor clientConfig_boot_mouse_input_report = new BluetoothGattDescriptor(GATT_DESCRIPTOR_UUID_CLIENT_CHARACTERISTIC_CONFIGURATION,
                BluetoothGattDescriptor.PERMISSION_READ | BluetoothGattDescriptor.PERMISSION_WRITE);
        boot_mouse_input_report.addDescriptor(clientConfig_boot_mouse_input_report);

        BluetoothGattCharacteristic hid_information = new BluetoothGattCharacteristic(GATT_CHARACTERISTIC_UUID_HID_INFORMATION,
                BluetoothGattCharacteristic.PROPERTY_READ,
                BluetoothGattCharacteristic.PERMISSION_READ);

        BluetoothGattCharacteristic hid_control_point = new BluetoothGattCharacteristic(GATT_CHARACTERISTIC_UUID_HID_CONTROL_POINT,
                BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE,
                BluetoothGattCharacteristic.PERMISSION_WRITE);

        HidService.addCharacteristic(inputReport);
        HidService.addCharacteristic(report_map);
        HidService.addCharacteristic(boot_keyboard_input_report);
        HidService.addCharacteristic(boot_keyboard_output_report);
        HidService.addCharacteristic(boot_mouse_input_report);
        HidService.addCharacteristic(hid_information);
        HidService.addCharacteristic(hid_control_point);


        BluetoothGattService device_information = new BluetoothGattService(GATT_SERVICE_UUID_DEVICE_INFORMATION, BluetoothGattService.SERVICE_TYPE_PRIMARY);
        BluetoothGattCharacteristic pnp_id = new BluetoothGattCharacteristic(GATT_CHARACTERISTIC_UUID_PNP_ID,
                BluetoothGattCharacteristic.PROPERTY_READ,
                BluetoothGattCharacteristic.PERMISSION_READ);
        device_information.addCharacteristic(pnp_id);


        server.addService(HidService);
        server.addService(device_information);
        startAdvertising();
    }

    void closeServer() {
        stopAdvertising();
        server.close();
    }

    private static UUID getBluetoothUUID(short assignedNumber) {
        long mostSigBits = 0x0000000000001000L | (((long) assignedNumber) & 0xFFFFL) << 32;
        long leastSigBits = 0x800000805F9B34FBL;
        return new UUID(mostSigBits, leastSigBits);
    }

    private static short getAssignedNumber(UUID uuid) {
        return (short) (uuid.getMostSignificantBits() >> 32);
    }
}