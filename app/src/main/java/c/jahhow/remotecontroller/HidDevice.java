package c.jahhow.remotecontroller;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHidDevice;
import android.bluetooth.BluetoothHidDeviceAppSdpSettings;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseSettings;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import java.nio.ByteBuffer;
import java.util.concurrent.Executor;

@RequiresApi(api = Build.VERSION_CODES.P)
class HidDevice {
    interface Listener {
        void onConnected(BluetoothDevice device);

        void onDisconnected(BluetoothDevice device);
    }

    Listener listener;

    private static final String TAG = HidDevice.class.getSimpleName();

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
            Standard_Device_Descriptor = new byte[]{
                    // Standard Device Descriptor
                    (byte) 0x12, // bLength             0/1  Numeric expression specifying the size of this descriptor.
                    (byte) 0x01, // bDescriptorType     1/1  Device descriptor type (assigned by USB).
                    (byte) 0x11, // bcdUSB              2/2  USB HID Specification Release 1.0.
                    (byte) 0x01,
                    (byte) 0x00, // bDeviceClass        4/1  Class code (assigned by USB). Note that the HID class is defined in the Interface descriptor.
                    (byte) 0x00, // bDeviceSubClass     5/1  Subclass code (assigned by USB). These codes are qualified by the value of the bDeviceClass field.
                    (byte) 0x00, // bDeviceProtocol     6/1  Protocol code. These codes are qualified by the value of the bDeviceSubClass field.
                    (byte) 0x08, // bMaxPacketSize0     7/1  Maximum packet size for endpoint zero (only 8, 16, 32, or 64 are valid).
                    (byte) 0xFE, // idVendor            8/2  Vendor ID (assigned by USB). For this example we’ll use 0xFFFF.
                    (byte) 0xFF,
                    (byte) 1,    // idProduct          10/2  Product ID (assigned by manufacturer).
                    (byte) 0,
                    (byte) 28,   // bcdDevice          12/2  Device release number (assigned by manufacturer).
                    (byte) 0,
                    (byte) 0,    // iManufacturer      14/1  Index of String descriptor describing manufacturer.
                    (byte) 0,    // iProduct           15/1  Index of string descriptor describing product.
                    (byte) 0,    // iSerialNumber      16/1  Index of String descriptor describing the device’s serial number.
                    (byte) 1,    // bNumConfigurations 17/1  Number of possible configurations.
            },
            Configuration_Descriptor = new byte[]{
                    /*
                    bLength             0/1 Size of this descriptor in bytes.*/ 0x09,/*
                    bDescriptorType     1/1 Configuration (assigned by USB). */ 0x02,/*
                    wTotalLength        2/2 Total length of data returned for this configuration.
                                            Includes the combined length of all returned descriptors
                                            (configuration, interface, endpoint, and HID) returned
                                            for this configuration. This value includes the HID
                                            descriptor but none of the other HID class descriptors
                                            (report or designator).*/ 34, 0,/*
                    bNumInterfaces      4/1 Number of interfaces supported by this configuration.*/ 1,/*
                    bConfigurationValue 5/1 Value to use as an argument to Set Configuration to
                                            select this configuration.*/ 1,/*
                    iConfiguration      6/1 Index of string descriptor describing this configuration.
                                            In this case there is none.*/ 0,/*
                    bmAttributes        7/1 Configuration characteristics
                                            7 Bus Powered
                                            6 Self Powered
                                            5 Remote Wakeup
                                            4..0 Reserved (reset to 0).*/ (byte) 0b01100000,/*
                    MaxPower            8/1 Maximum power consumption of USB device from bus
                                            in this specific configuration when the device is fully
                                            operational. Expressed in 2 mA units—for example, 50
                                                    = 100 mA. The number chosen for this example is
                                            arbitrary.*/ 0,
            },
            Mouse_Interface_Descriptor = new byte[]{/*
                            Part Offset/Size (Bytes) Description Sample Value
                            bLength            0/1 Size of this descriptor in bytes.*/ 0x09,/*
                            bDescriptorType    1/1 Interface descriptor type (assigned by USB).*/ 0x04,/*
                            bInterfaceNumber   2/1 Number of interface.*/ 0x01,/*
                            bAlternateSetting  3/1 Value used to select alternate setting.*/ 0x00,/*
                            bNumEndpoints      4/1 Number of endpoints.*/ 0x01,/*
                            bInterfaceClass    5/1 Class code (HID code assigned by USB).*/ 0x03,/*
                            bInterfaceSubClass 6/1 Subclass code.
                                                        0 No subclass
                                                        1 Boot Interface subclass */ 0x01,/*
                            bInterfaceProtocol 7/1 2 = Mouse.*/ 0x02,/*
                            iInterface 8/1 Index of string descriptor.*/ 0x00,
            },
            HID_Mouse_Descriptor = new byte[]{
                    0x09, // bLength          0/1  Size of this descriptor in bytes.
                    0x21, // bDescriptorType  1/1  HID descriptor type (assigned by USB).
                    0x11, // bcdHID           2/2  HID Class Specification release number.
                    0x01,
                    0x00, // bCountryCode     4/1  Hardware target country.
                    0x01, // bNumDescriptors  5/1  Number of HID class descriptors to follow.
                    0x22, // bDescriptorType  6/1  Report descriptor type.
                    // wItemLength      7/2  Total length of Report descriptor.
                    (byte) reportDescriptor.length,
                    (byte) (reportDescriptor.length >> 8),
            },
            Mouse_Endpoint_Descriptor = new byte[]{/*
                        Part Offset/Size (Bytes) Description Sample Value
                        bLength 0/1 Size of this descriptor in bytes.*/ 0x07,/*
                        bDescriptorType  1/1 Endpoint descriptor type (assigned by USB).*/ 0x05,/*
                        bEndpointAddress 2/1 The address of the endpoint.*/ (byte) 0b10000010,/*
                                                Bit 3...0:  The endpoint number
                                                Bit 6...4:  Reserved, reset to zero
                                                Bit 7:      Direction, ignored for control endpoints
                                                       0 = OUT endpoint
                                                       1 = IN endpoint
                        bmAttributes     3/1 This field describes the endpoint’s attributes when it is
                                         configured using the bConfigurationValue. */ 0b11,/*
                                             Bit 0..1 Transfer type:
                                                         00 Control
                                                         01 Isochronous
                                                         10 Bulk
                                                         11 Interrupt
                                             All other bits are reserved.
                        wMaxPacketSize   4/2 Maximum packet size.*/ 8, 0,/*
                        bInterval        6/1 Interval for polling endpoint for data transfers.*/ 10,
            },
            mouse_input_report = new byte[3],// do not pretend Report ID
            descriptors = ByteBuffer
                    .allocate(
                            Standard_Device_Descriptor.length +
                                    Configuration_Descriptor.length +
                                    Mouse_Interface_Descriptor.length +
                                    HID_Mouse_Descriptor.length +
                                    Mouse_Endpoint_Descriptor.length +
                                    reportDescriptor.length
                    )
                    .put(Standard_Device_Descriptor)
                    .put(Configuration_Descriptor)
                    .put(Mouse_Interface_Descriptor)
                    .put(HID_Mouse_Descriptor)
                    .put(reportDescriptor)
                    .put(Mouse_Endpoint_Descriptor)
                    // Strings
                    //.put(new byte[]{4, 3, 9, 0})// Supports English
                    //.put((byte) 14).put((byte) 3).put("Jahhow".getBytes(StandardCharsets.UTF_16LE))
                    .array();

    private Context context;
    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    BluetoothHidDevice hidDevice;
    private BluetoothDevice device;
    private boolean connected = false;
    Executor executor;

    HidDevice(Context context) {
        this.context = context;
        bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        assert bluetoothManager != null;
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
        executor = new Executor() {
            @Override
            public void execute(@NonNull Runnable command) {
                new Thread(command).start();
            }
        };
    }


    void openServer() {
        if (
                !bluetoothAdapter.getProfileProxy(context, new BluetoothProfile.ServiceListener() {
                    @Override
                    public void onServiceConnected(int profile, BluetoothProfile proxy) {
                        //Log.i(TAG, "onServiceConnected");
                        if (profile == BluetoothProfile.HID_DEVICE && proxy != null && hidDevice == null) {
                            hidDevice = (BluetoothHidDevice) proxy;
                            hidDevice.registerApp(
                                    new BluetoothHidDeviceAppSdpSettings("Computer Remote", null, "Jahhow", (byte) 1, descriptors),
                                    null, null, executor,
                                    new BluetoothHidDevice.Callback() {
                                        @Override
                                        public void onAppStatusChanged(BluetoothDevice pluggedDevice, boolean registered) {
                                            super.onAppStatusChanged(pluggedDevice, registered);
                                        }

                                        @Override
                                        public void onConnectionStateChanged(BluetoothDevice device, int state) {
                                            super.onConnectionStateChanged(device, state);
                                            if (state == BluetoothProfile.STATE_CONNECTED) {
                                                HidDevice.this.device = device;
                                            } else {
                                                HidDevice.this.device = null;
                                            }
                                        }

                                        @Override
                                        public void onGetReport(BluetoothDevice device, byte type, byte id, int bufferSize) {
                                            super.onGetReport(device, type, id, bufferSize);
                                        }

                                        @Override
                                        public void onSetReport(BluetoothDevice device, byte type, byte id, byte[] data) {
                                            super.onSetReport(device, type, id, data);
                                        }

                                        @Override
                                        public void onSetProtocol(BluetoothDevice device, byte protocol) {
                                            super.onSetProtocol(device, protocol);
                                        }

                                        @Override
                                        public void onInterruptData(BluetoothDevice device, byte reportId, byte[] data) {
                                            super.onInterruptData(device, reportId, data);
                                        }

                                        @Override
                                        public void onVirtualCableUnplug(BluetoothDevice device) {
                                            super.onVirtualCableUnplug(device);
                                        }
                                    }
                            );
                        }
                    }

                    @Override
                    public void onServiceDisconnected(int profile) {
                        Log.i(TAG, "onServiceDisconnected");
                    }
                }, BluetoothProfile.HID_DEVICE)
        ) {
            throw new RuntimeException("getProfileProxy failed");
        }
    }

    void closeServer() {
        if (hidDevice != null)
            hidDevice.unregisterApp();
    }

    boolean connect(BluetoothDevice device) {
        return hidDevice.connect(device);
    }

    void sendMouseMove(byte dx, byte dy) {
        if (device != null) {
            mouse_input_report[1] = dx;
            mouse_input_report[2] = dy;
            hidDevice.sendReport(device, 1, mouse_input_report);
        }
    }

    void sendMouseMove(BluetoothDevice device, byte dx, byte dy) {
        mouse_input_report[1] = dx;
        mouse_input_report[2] = dy;
        hidDevice.sendReport(device, 1, mouse_input_report);
    }
}