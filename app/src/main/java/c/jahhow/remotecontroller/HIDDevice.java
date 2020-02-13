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
            GATT_UUID_SERVICE_GENERIC_ACCESS = getBluetoothUUID((short) 0x1800),
            GATT_UUID_SERVICE_GENERIC_ATTRIBUTE = getBluetoothUUID((short) 0x1801),
            GATT_UUID_SERVICE_HUMAN_INTERFACE_DEVICE = getBluetoothUUID((short) 0x1812),
            GATT_UUID_SERVICE_BATTERY_SERVICE = getBluetoothUUID((short) 0x180F),
            GATT_UUID_SERVICE_DEVICE_INFORMATION = getBluetoothUUID((short) 0x180A),
            GATT_UUID_SERVICE_SCAN_PARAMETERS = getBluetoothUUID((short) 0x1813),
            GATT_UUID_CHARACTERISTIC_DEVICE_NAME = getBluetoothUUID((short) 0x2A00),
            GATT_UUID_CHARACTERISTIC_APPEARANCE = getBluetoothUUID((short) 0x2A01),
            GATT_UUID_CHARACTERISTIC_REPORT_MAP = getBluetoothUUID((short) 0x2A4B),
            GATT_UUID_CHARACTERISTIC_REPORT = getBluetoothUUID((short) 0x2A4D),
            GATT_UUID_CHARACTERISTIC_PROTOCOL_MODE = getBluetoothUUID((short) 0x2A4E),
            GATT_UUID_CHARACTERISTIC_BATTERY_LEVEL = getBluetoothUUID((short) 0x2A19),
            GATT_UUID_CHARACTERISTIC_BOOT_KEYBOARD_INPUT_REPORT = getBluetoothUUID((short) 0x2A22),
            GATT_UUID_CHARACTERISTIC_BOOT_KEYBOARD_OUTPUT_REPORT = getBluetoothUUID((short) 0x2A32),
            GATT_UUID_CHARACTERISTIC_BOOT_MOUSE_INPUT_REPORT = getBluetoothUUID((short) 0x2A33),
            GATT_UUID_CHARACTERISTIC_HID_INFORMATION = getBluetoothUUID((short) 0x2A4A),
            GATT_UUID_CHARACTERISTIC_HID_CONTROL_POINT = getBluetoothUUID((short) 0x2A4C),
            GATT_UUID_CHARACTERISTIC_PNP_ID = getBluetoothUUID((short) 0x2A50),
            GATT_UUID_CHARACTERISTIC_MODEL_NUMBER_STRING = getBluetoothUUID((short) 0x2A24),
            GATT_UUID_CHARACTERISTIC_MANUFACTURER_NAME_STRING = getBluetoothUUID((short) 0x2A29),
            GATT_UUID_DESCRIPTOR_CLIENT_CHARACTERISTIC_CONFIGURATION = getBluetoothUUID((short) 0x2902),
            GATT_UUID_DESCRIPTOR_REPORT_REFERENCE = getBluetoothUUID((short) 0x2908);

    private static final int manufacturerId = 0xF3C6;
    private static final byte[] reportDescriptor =
            new byte[]{
                    (byte) 0x05, (byte) 0x01,             // USAGE_PAGE (Generic Desktop)
                    (byte) 0x09, (byte) 0x02,             // USAGE (Mouse)
                    (byte) 0xa1, (byte) 0x01,             // COLLECTION (Application)
                    (byte) 0x85, (byte) 0x01,             //   REPORT_ID (01)
                    (byte) 0x09, (byte) 0x01,             //   USAGE (Pointer)
                    (byte) 0xa1, (byte) 0x00,             //   COLLECTION (Physical)
                    (byte) 0x05, (byte) 0x09,             //     USAGE_PAGE (Button)
                    (byte) 0x19, (byte) 0x01,             //     USAGE_MINIMUM (Button 1)
                    (byte) 0x29, (byte) 0x03,             //     USAGE_MAXIMUM (Button 3)
                    (byte) 0x15, (byte) 0x00,             //     LOGICAL_MINIMUM (0)
                    (byte) 0x25, (byte) 0x01,             //     LOGICAL_MAXIMUM (1)
                    (byte) 0x95, (byte) 0x03,             //     REPORT_COUNT (3)
                    (byte) 0x75, (byte) 0x01,             //     REPORT_SIZE (1)
                    (byte) 0x81, (byte) 0x02,             //     INPUT (Data,Var,Abs)
                    (byte) 0x95, (byte) 0x01,             //     REPORT_COUNT (1)
                    (byte) 0x75, (byte) 0x05,             //     REPORT_SIZE (5)
                    (byte) 0x81, (byte) 0x03,             //     INPUT (Cnst,Var,Abs)
                    (byte) 0x05, (byte) 0x01,             //     USAGE_PAGE (Generic Desktop)
                    (byte) 0x09, (byte) 0x30,             //     USAGE (X)
                    (byte) 0x09, (byte) 0x31,             //     USAGE (Y)
                    (byte) 0x15, (byte) 0x81,             //     LOGICAL_MINIMUM (-127)
                    (byte) 0x25, (byte) 0x7f,             //     LOGICAL_MAXIMUM (127)
                    (byte) 0x75, (byte) 0x08,             //     REPORT_SIZE (8)
                    (byte) 0x95, (byte) 0x02,             //     REPORT_COUNT (2)
                    (byte) 0x81, (byte) 0x06,             //     INPUT (Data,Var,Rel)
                    (byte) 0xc0,                          //   END_COLLECTION
                    (byte) 0xc0                           // END_COLLECTION
            },
            pnp_id = new byte[]{
                    (byte) 1,                   // Vendor ID Source: Bluetooth SIG assigned
                    (byte) manufacturerId,      // Vendor ID LSO
                    (byte) manufacturerId >> 8, // Vendor ID MSO
                    (byte) 28,                  // Product ID LSO
                    (byte) 28,                  // Product ID MSO
                    (byte) 28,                  // Product Version LSO
                    (byte) 28                   // Product Version MSO
            },
            hid_information = new byte[]{
                    0x11,// bcdHID vx.11
                    0x01,// bcdHID v1.xx
                    0,   // bCountryCode: not localized
                    0b10 // Flags
            },
            client_characteristic_configuration_input_report = new byte[]{
                    0b01,// Indications | Notifications
                    0
            },
            client_characteristic_configuration_boot_keyboard_input_report = new byte[]{
                    0b01,// Indications | Notifications
                    0
            },
            client_characteristic_configuration_boot_mouse_input_report = new byte[]{
                    0b01,// Indications | Notifications
                    0
            },
            protocol_mode = new byte[]{1},// Not Boot Protocol
            report_reference_mouseInput = new byte[]{
                    1, // Report ID
                    1  // Report Type: Input Report
            },
            mouse_input_report = new byte[]{0, 0, 2},// do not pretend Report ID
            boot_mouse_input_report = new byte[]{0, 50, 50};

    private Context context;
    private BluetoothManager bluetoothManager;
    private BluetoothLeAdvertiser bluetoothLeAdvertiser;
    private BluetoothGattServer server;
    private AdvertiseCallback advertiseCallback = new AdvertiseCallback() {
        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            //Log.i(TAG, "AdvertiseCallback.onStartSuccess");
        }

        @Override
        public void onStartFailure(int errorCode) {
            Log.w(TAG, "AdvertiseCallback.onStartFailure " + errorCode);
        }
    };
    private BluetoothDevice device;
    private BluetoothGattCharacteristic mouseInputReport;
    private BluetoothGattService[] services;
    private int indexServiceToAdd = 1;
    private boolean connected = false;
    private boolean mouse_input_report_notification = (client_characteristic_configuration_boot_mouse_input_report[0] & 1) == 1;

    HIDDevice(Context context) {
        this.context = context;
        bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        assert bluetoothManager != null;
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
        bluetoothLeAdvertiser = bluetoothAdapter.getBluetoothLeAdvertiser();
    }

    private void startAdvertising() {
        AdvertiseSettings advertiseSettings = new AdvertiseSettings.Builder().build();

        AdvertiseData advertiseData = new AdvertiseData.Builder()
                .addManufacturerData(manufacturerId, new byte[]{})
                .addServiceUuid(new ParcelUuid(GATT_UUID_SERVICE_HUMAN_INTERFACE_DEVICE))
                .setIncludeDeviceName(true)
                .setIncludeTxPowerLevel(true)
                .build();

        bluetoothLeAdvertiser.startAdvertising(advertiseSettings, advertiseData, advertiseCallback);
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
                    if (HIDDevice.this.device == null) {
                        HIDDevice.this.device = device;
                        connected = true;
                    }
                } else {
                    if (device.equals(HIDDevice.this.device)) {
                        connected = false;
                    }
                    Log.i(TAG, "BluetoothDevice DISCONNECTED: " + device);
                }
            }

            @Override
            public void onServiceAdded(int status, BluetoothGattService service) {
                super.onServiceAdded(status, service);
                //Log.i(TAG, "onServiceAdded");
                if (indexServiceToAdd < services.length) {
                    server.addService(services[indexServiceToAdd]);
                    ++indexServiceToAdd;
                }
            }

            @Override
            public void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattCharacteristic characteristic) {
                super.onCharacteristicReadRequest(device, requestId, offset, characteristic);
                UUID uuid = characteristic.getUuid();
                int status;

                if (uuid.equals(GATT_UUID_CHARACTERISTIC_REPORT)
                        || uuid.equals(GATT_UUID_CHARACTERISTIC_HID_INFORMATION)
                        || uuid.equals(GATT_UUID_CHARACTERISTIC_REPORT_MAP)
                        || uuid.equals(GATT_UUID_CHARACTERISTIC_PROTOCOL_MODE)
                    //  || uuid.equals(GATT_UUID_CHARACTERISTIC_PNP_ID)
                    //  || uuid.equals(GATT_UUID_CHARACTERISTIC_BOOT_MOUSE_INPUT_REPORT)
                    //  || uuid.equals(GATT_UUID_CHARACTERISTIC_BATTERY_LEVEL)
                    //  || uuid.equals(GATT_UUID_CHARACTERISTIC_MANUFACTURER_NAME_STRING)
                    //  || uuid.equals(GATT_UUID_CHARACTERISTIC_MODEL_NUMBER_STRING)
                ) {
                    /*if (BuildConfig.DEBUG) {
                        String str;
                        if (uuid.equals(GATT_UUID_CHARACTERISTIC_REPORT)) {
                            str = "REPORT";
                        } else if (uuid.equals(GATT_UUID_CHARACTERISTIC_REPORT_MAP)) {
                            str = "REPORT_MAP";
                        } else if (uuid.equals(GATT_UUID_CHARACTERISTIC_BATTERY_LEVEL)) {
                            str = "BATTERY_LEVEL";
                        } else {
                            str = "Other";
                        }
                        Log.i(TAG, "onCharacteristicReadRequest OK: " + str + ", offset: " + offset);
                    }*/
                    status = BluetoothGatt.GATT_SUCCESS;
                } else {
                    Log.w(TAG, "onCharacteristicReadRequest unimplemented " + uuid);
                    status = BluetoothGatt.GATT_FAILURE;
                }
                //Log.i(TAG, "sendResponse " + Arrays.toString(characteristic.getValue()));
                server.sendResponse(device, requestId, status, 0, characteristic.getValue());
            }

            @Override
            public void onCharacteristicWriteRequest(BluetoothDevice device, int requestId, BluetoothGattCharacteristic characteristic, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
                super.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite, responseNeeded, offset, value);
                Log.i(TAG, "onCharacteristicWriteRequest");
            }

            @Override
            public void onDescriptorReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattDescriptor descriptor) {
                super.onDescriptorReadRequest(device, requestId, offset, descriptor);
                UUID uuid = descriptor.getUuid();
                int status;

                if (uuid.equals(GATT_UUID_DESCRIPTOR_CLIENT_CHARACTERISTIC_CONFIGURATION)
                        || uuid.equals(GATT_UUID_DESCRIPTOR_REPORT_REFERENCE)) {
                    /*if (BuildConfig.DEBUG) {
                        if (uuid.equals(GATT_UUID_DESCRIPTOR_CLIENT_CHARACTERISTIC_CONFIGURATION)) {
                            BluetoothGattCharacteristic characteristic = descriptor.getCharacteristic();
                            UUID characteristicUuid = characteristic.getUuid();
                            String str;
                            if (characteristicUuid.equals(GATT_UUID_CHARACTERISTIC_BOOT_MOUSE_INPUT_REPORT)) {
                                str = "BOOT_MOUSE";
                            } else if (characteristicUuid.equals(GATT_UUID_CHARACTERISTIC_REPORT)) {
                                str = "REPORT";
                            } else {
                                str = "other";
                            }
                            Log.i(TAG, "onDescriptorReadRequest CONFIG, parent characteristic: " + str);
                        }
                    }*/
                    status = BluetoothGatt.GATT_SUCCESS;
                } else {
                    Log.w(TAG, "onDescriptorReadRequest unimplemented " + uuid);
                    status = BluetoothGatt.GATT_FAILURE;
                }
                server.sendResponse(device, requestId, status, 0, descriptor.getValue());
            }

            @Override
            public void onDescriptorWriteRequest(BluetoothDevice device, int requestId, BluetoothGattDescriptor descriptor, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
                super.onDescriptorWriteRequest(device, requestId, descriptor, preparedWrite, responseNeeded, offset, value);
                //Log.i(TAG, "preparedWrite: " + preparedWrite + ", responseNeeded: " + responseNeeded + ", offset: " + offset + ", value: " + Arrays.toString(value));
                UUID uuid = descriptor.getUuid();
                int status;

                if (uuid.equals(GATT_UUID_DESCRIPTOR_CLIENT_CHARACTERISTIC_CONFIGURATION)) {
                    if (BuildConfig.DEBUG) {
                        String des, parent;
                        if (uuid.equals(GATT_UUID_DESCRIPTOR_CLIENT_CHARACTERISTIC_CONFIGURATION)) {
                            des = "CONFIG";
                        } else if (uuid.equals(GATT_UUID_DESCRIPTOR_REPORT_REFERENCE)) {
                            des = "REPORT_REFERENCE";
                        } else {
                            des = "OTHER";
                        }

                        BluetoothGattCharacteristic characteristic = descriptor.getCharacteristic();
                        UUID characteristicUuid = characteristic.getUuid();
                        if (characteristicUuid.equals(GATT_UUID_CHARACTERISTIC_BOOT_MOUSE_INPUT_REPORT)) {
                            parent = "BOOT_MOUSE";
                        } else if (characteristicUuid.equals(GATT_UUID_CHARACTERISTIC_REPORT)) {
                            parent = "REPORT";
                        } else {
                            parent = "OTHER";
                        }
                        Log.i(TAG, "onDescriptorWriteRequest " + des + ", parent characteristic: " + parent);
                    }

                    byte v0 = value[0];
                    mouse_input_report_notification = (v0 & 1) == 1;
                    descriptor.setValue(value);
                    Log.i(TAG, "mouse_input_report_notification: " + mouse_input_report_notification);

                    status = BluetoothGatt.GATT_SUCCESS;
                } else {
                    Log.w(TAG, "onDescriptorWriteRequest unimplemented " + uuid);
                    status = BluetoothGatt.GATT_FAILURE;
                }
                if (responseNeeded)
                    server.sendResponse(device, requestId, status, 0, null);
            }

            @Override
            public void onExecuteWrite(BluetoothDevice device, int requestId, boolean execute) {
                super.onExecuteWrite(device, requestId, execute);
                Log.i(TAG, "onExecuteWrite");
            }

            /*@Override
            public void onNotificationSent(BluetoothDevice device, int status) {
                super.onNotificationSent(device, status);
                Log.i(TAG, "onNotificationSent " + (status == BluetoothGatt.GATT_SUCCESS ? "OK" : "Fail"));
            }

            @Override
            public void onMtuChanged(BluetoothDevice device, int mtu) {
                super.onMtuChanged(device, mtu);
                Log.i(TAG, "onMtuChanged");
            }

            @Override
            public void onPhyUpdate(BluetoothDevice device, int txPhy, int rxPhy, int status) {
                super.onPhyUpdate(device, txPhy, rxPhy, stonNotificationSentatus);
                Log.i(TAG, "onPhyUpdate");
            }

            @Override
            public void onPhyRead(BluetoothDevice device, int txPhy, int rxPhy, int status) {
                super.onPhyRead(device, txPhy, rxPhy, status);
                Log.i(TAG, "onPhyRead");
            }*/
        };

        server = bluetoothManager.openGattServer(context, serverCallback);

        /*BluetoothGattService generic_access = new BluetoothGattService(GATT_UUID_SERVICE_GENERIC_ACCESS, BluetoothGattService.SERVICE_TYPE_PRIMARY);
        {
            BluetoothGattCharacteristic device_name = new BluetoothGattCharacteristic(GATT_UUID_CHARACTERISTIC_DEVICE_NAME,
                    BluetoothGattCharacteristic.PROPERTY_READ,
                    BluetoothGattCharacteristic.PERMISSION_READ);
            BluetoothGattCharacteristic appearance = new BluetoothGattCharacteristic(GATT_UUID_CHARACTERISTIC_APPEARANCE,
                    BluetoothGattCharacteristic.PROPERTY_READ,
                    BluetoothGattCharacteristic.PERMISSION_READ);
            generic_access.addCharacteristic(device_name);
            generic_access.addCharacteristic(appearance);
        }*/

        BluetoothGattService human_interface_device = new BluetoothGattService(GATT_UUID_SERVICE_HUMAN_INTERFACE_DEVICE, BluetoothGattService.SERVICE_TYPE_PRIMARY);
        {
            mouseInputReport = new BluetoothGattCharacteristic(GATT_UUID_CHARACTERISTIC_REPORT,
                    BluetoothGattCharacteristic.PROPERTY_READ | BluetoothGattCharacteristic.PROPERTY_NOTIFY,
                    BluetoothGattCharacteristic.PERMISSION_READ);
            {
                BluetoothGattDescriptor clientConfig = new BluetoothGattDescriptor(GATT_UUID_DESCRIPTOR_CLIENT_CHARACTERISTIC_CONFIGURATION,
                        BluetoothGattDescriptor.PERMISSION_READ | BluetoothGattDescriptor.PERMISSION_WRITE);
                clientConfig.setValue(client_characteristic_configuration_input_report);
                BluetoothGattDescriptor reportReference = new BluetoothGattDescriptor(GATT_UUID_DESCRIPTOR_REPORT_REFERENCE, BluetoothGattDescriptor.PERMISSION_READ);
                reportReference.setValue(HIDDevice.report_reference_mouseInput);
                mouseInputReport.addDescriptor(clientConfig);
                mouseInputReport.addDescriptor(reportReference);
                mouseInputReport.setValue(mouse_input_report);
            }

            BluetoothGattCharacteristic report_map = new BluetoothGattCharacteristic(GATT_UUID_CHARACTERISTIC_REPORT_MAP,
                    BluetoothGattCharacteristic.PROPERTY_READ,
                    BluetoothGattCharacteristic.PERMISSION_READ);
            report_map.setValue(reportDescriptor);

            /*BluetoothGattCharacteristic boot_keyboard_input_report = new BluetoothGattCharacteristic(GATT_UUID_CHARACTERISTIC_BOOT_KEYBOARD_INPUT_REPORT,
                    BluetoothGattCharacteristic.PROPERTY_READ | BluetoothGattCharacteristic.PROPERTY_NOTIFY,
                    BluetoothGattCharacteristic.PERMISSION_READ);
            {
                BluetoothGattDescriptor clientConfig_boot_keyboard_input_report = new BluetoothGattDescriptor(GATT_UUID_DESCRIPTOR_CLIENT_CHARACTERISTIC_CONFIGURATION,
                        BluetoothGattDescriptor.PERMISSION_READ | BluetoothGattDescriptor.PERMISSION_WRITE);
                boot_keyboard_input_report.addDescriptor(clientConfig_boot_keyboard_input_report);
            }

            BluetoothGattCharacteristic boot_keyboard_output_report = new BluetoothGattCharacteristic(GATT_UUID_CHARACTERISTIC_BOOT_KEYBOARD_OUTPUT_REPORT,
                    BluetoothGattCharacteristic.PROPERTY_READ | BluetoothGattCharacteristic.PROPERTY_WRITE | BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE,
                    BluetoothGattCharacteristic.PERMISSION_READ | BluetoothGattCharacteristic.PERMISSION_WRITE);

            BluetoothGattCharacteristic boot_mouse_input_report = new BluetoothGattCharacteristic(GATT_UUID_CHARACTERISTIC_BOOT_MOUSE_INPUT_REPORT,
                    BluetoothGattCharacteristic.PROPERTY_READ | BluetoothGattCharacteristic.PROPERTY_NOTIFY,
                    BluetoothGattCharacteristic.PERMISSION_READ);
            {
                BluetoothGattDescriptor clientConfig_boot_mouse_input_report = new BluetoothGattDescriptor(GATT_UUID_DESCRIPTOR_CLIENT_CHARACTERISTIC_CONFIGURATION,
                        BluetoothGattDescriptor.PERMISSION_READ | BluetoothGattDescriptor.PERMISSION_WRITE);
                clientConfig_boot_mouse_input_report.setValue(client_characteristic_configuration_boot_mouse_input_report);
                boot_mouse_input_report.addDescriptor(clientConfig_boot_mouse_input_report);
                boot_mouse_input_report.setValue(HIDDevice.boot_mouse_input_report);
            }*/

            BluetoothGattCharacteristic hid_information = new BluetoothGattCharacteristic(GATT_UUID_CHARACTERISTIC_HID_INFORMATION,
                    BluetoothGattCharacteristic.PROPERTY_READ,
                    BluetoothGattCharacteristic.PERMISSION_READ);
            hid_information.setValue(HIDDevice.hid_information);

            BluetoothGattCharacteristic hid_control_point = new BluetoothGattCharacteristic(GATT_UUID_CHARACTERISTIC_HID_CONTROL_POINT,
                    BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE,
                    BluetoothGattCharacteristic.PERMISSION_WRITE);

            BluetoothGattCharacteristic protocol_mode = new BluetoothGattCharacteristic(GATT_UUID_CHARACTERISTIC_PROTOCOL_MODE,
                    BluetoothGattCharacteristic.PROPERTY_READ | BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE,
                    BluetoothGattCharacteristic.PERMISSION_READ | BluetoothGattCharacteristic.PERMISSION_WRITE);
            protocol_mode.setValue(HIDDevice.protocol_mode);

            human_interface_device.addCharacteristic(protocol_mode);
            human_interface_device.addCharacteristic(mouseInputReport);
            human_interface_device.addCharacteristic(report_map);
            //human_interface_device.addCharacteristic(boot_keyboard_input_report);  // Seems optional
            //human_interface_device.addCharacteristic(boot_keyboard_output_report); // Seems optional
            //human_interface_device.addCharacteristic(boot_mouse_input_report);     // Seems optional
            human_interface_device.addCharacteristic(hid_information);
            human_interface_device.addCharacteristic(hid_control_point);
        }

        /*BluetoothGattService battery_service = new BluetoothGattService(GATT_UUID_SERVICE_BATTERY_SERVICE, BluetoothGattService.SERVICE_TYPE_PRIMARY);
        {
            BluetoothGattCharacteristic battery_level = new BluetoothGattCharacteristic(GATT_UUID_CHARACTERISTIC_BATTERY_LEVEL,
                    BluetoothGattCharacteristic.PROPERTY_READ,
                    BluetoothGattCharacteristic.PERMISSION_READ);
            battery_level.setValue(new byte[]{100});
            battery_service.addCharacteristic(battery_level);
        }*/

        /*BluetoothGattService device_information = new BluetoothGattService(GATT_UUID_SERVICE_DEVICE_INFORMATION, BluetoothGattService.SERVICE_TYPE_PRIMARY);
        {
            BluetoothGattCharacteristic pnp_id = new BluetoothGattCharacteristic(GATT_UUID_CHARACTERISTIC_PNP_ID,
                    BluetoothGattCharacteristic.PROPERTY_READ,
                    BluetoothGattCharacteristic.PERMISSION_READ);
            pnp_id.setValue(HIDDevice.pnp_id);

            BluetoothGattCharacteristic manufacturer_name_string = new BluetoothGattCharacteristic(GATT_UUID_CHARACTERISTIC_MANUFACTURER_NAME_STRING,
                    BluetoothGattCharacteristic.PROPERTY_READ,
                    BluetoothGattCharacteristic.PERMISSION_READ);
            manufacturer_name_string.setValue("Jahhow".getBytes(StandardCharsets.UTF_8));

            BluetoothGattCharacteristic model_number_string = new BluetoothGattCharacteristic(GATT_UUID_CHARACTERISTIC_MODEL_NUMBER_STRING,
                    BluetoothGattCharacteristic.PROPERTY_READ,
                    BluetoothGattCharacteristic.PERMISSION_READ);
            model_number_string.setValue(("Computer Remote V" + BuildConfig.VERSION_CODE).getBytes(StandardCharsets.UTF_8));

            device_information.addCharacteristic(manufacturer_name_string);
            device_information.addCharacteristic(model_number_string);
            device_information.addCharacteristic(pnp_id);
        }*/

        /*BluetoothGattService generic_attribute = new BluetoothGattService(GATT_UUID_SERVICE_GENERIC_ATTRIBUTE, BluetoothGattService.SERVICE_TYPE_PRIMARY);
        {
        }*/

        services = new BluetoothGattService[]{
                human_interface_device,
                //generic_access,    // Fail. onServiceAdded() won't call
                //battery_service,   // optional
                //device_information // not functioning
        };
        server.addService(services[0]);
        startAdvertising();
    }

    void closeServer() {
        stopAdvertising();
        server.close();
    }

    boolean connect(BluetoothDevice device) {
        return server.connect(device, true);
    }

    void sendMouseMove(byte dx, byte dy) {
        if (connected) {
            mouse_input_report[1] = dx;
            mouse_input_report[2] = dy;
            server.notifyCharacteristicChanged(device, mouseInputReport, false);
        }
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