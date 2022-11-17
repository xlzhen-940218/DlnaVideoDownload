
package com.zxt.dlna.dmr;

import com.zxt.dlna.util.UpnpUtil;
import com.zxt.dlna.util.Utils;

import org.fourthline.cling.binding.LocalServiceBinder;
import org.fourthline.cling.binding.annotations.AnnotationLocalServiceBinder;
import org.fourthline.cling.model.DefaultServiceManager;
import org.fourthline.cling.model.ServiceManager;
import org.fourthline.cling.model.ValidationException;
import org.fourthline.cling.model.meta.DeviceDetails;
import org.fourthline.cling.model.meta.DeviceIdentity;
import org.fourthline.cling.model.meta.Icon;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.meta.LocalService;
import org.fourthline.cling.model.meta.ManufacturerDetails;
import org.fourthline.cling.model.meta.ModelDetails;
import org.fourthline.cling.model.types.DLNACaps;
import org.fourthline.cling.model.types.DLNADoc;
import org.fourthline.cling.model.types.UDADeviceType;
import org.fourthline.cling.model.types.UDN;
import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;
import org.fourthline.cling.support.avtransport.lastchange.AVTransportLastChangeParser;
import org.fourthline.cling.support.lastchange.LastChange;
import org.fourthline.cling.support.lastchange.LastChangeAwareServiceManager;
import org.fourthline.cling.support.model.TransportState;
import org.fourthline.cling.support.renderingcontrol.lastchange.RenderingControlLastChangeParser;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public class ZxtMediaRenderer {

    public static final long LAST_CHANGE_FIRING_INTERVAL_MILLISECONDS = 500;

    private static final String TAG = "GstMediaRenderer";

    final protected LocalServiceBinder binder = new AnnotationLocalServiceBinder();

    // These are shared between all "logical" player instances of a single service
    final protected LastChange avTransportLastChange = new LastChange(new AVTransportLastChangeParser());
    final protected LastChange renderingControlLastChange = new LastChange(new RenderingControlLastChangeParser());

    final protected ServiceManager<ZxtConnectionManagerService> connectionManager;
    final protected LastChangeAwareServiceManager<AVTransportService> avTransport;

    final protected LocalDevice device;

    public ZxtMediaRenderer(String model, String manufacturer, String hostName, String hostAddress, byte[] data, final AVTransportService.Callback callback) {
        // The connection manager doesn't have to do much, HTTP is stateless
        LocalService connectionManagerService = binder.read(ZxtConnectionManagerService.class);
        connectionManager =
                new DefaultServiceManager(connectionManagerService) {
                    @Override
                    protected Object createServiceInstance() throws Exception {
                        return new ZxtConnectionManagerService();
                    }
                };
        connectionManagerService.setManager(connectionManager);

        // The AVTransport just passes the calls on to the backend players
        LocalService<AVTransportService> avTransportService = binder.read(AVTransportService.class);
        avTransport =
                new LastChangeAwareServiceManager<AVTransportService>(
                        avTransportService,
                        new AVTransportLastChangeParser()
                ) {
                    @Override
                    protected AVTransportService createServiceInstance() throws Exception {
                        return new AVTransportService(avTransportLastChange,callback);
                    }
                };
        avTransportService.setManager(avTransport);

        try {
            UDN udn = UpnpUtil.uniqueSystemIdentifier("msidmr",model,manufacturer,hostName,hostAddress);

            device = new LocalDevice(
                    //TODO zxt

                    new DeviceIdentity(udn),
                    new UDADeviceType("MediaRenderer", 1),
                    new DeviceDetails(
                            "Dlna Video Download" + " (" + model + ")",
                            new ManufacturerDetails(manufacturer),
                            new ModelDetails(Utils.DMR_NAME, Utils.DMR_DESC, "1", Utils.DMR_MODEL_URL),
                            new DLNADoc[]{
                                    new DLNADoc("DMR", DLNADoc.Version.V1_5)
                            }, new DLNACaps(new String[]{
                            "av-upload", "image-upload", "audio-upload"
                    })
                    ),
                    new Icon[]{createDefaultDeviceIcon(data)},
                    new LocalService[]{
                            avTransportService,
                            connectionManagerService
                    }
            );
            System.out.println(TAG+ "getType: " + device.getType().toString());
        } catch (ValidationException ex) {
            throw new RuntimeException(ex);
        }

        runLastChangePushThread();
    }

    // The backend player instances will fill the LastChange whenever something happens with
    // whatever event messages are appropriate. This loop will periodically flush these changes
    // to subscribers of the LastChange state variable of each service.
    protected void runLastChangePushThread() {
        // TODO: We should only run this if we actually have event subscribers
        new Thread() {
            @Override
            public void run() {
                try {
                    while (true) {
                        // These operations will NOT block and wait for network responses
                        avTransport.fireLastChange();
                        //renderingControl.fireLastChange();
                        Thread.sleep(LAST_CHANGE_FIRING_INTERVAL_MILLISECONDS);
                    }
                } catch (Exception ex) {
                    //Log.e(TAG, "runLastChangePushThread ex", ex);
                }
            }
        }.start();
    }

    public LocalDevice getDevice() {
        return device;
    }

    public ServiceManager<ZxtConnectionManagerService> getConnectionManager() {
        return connectionManager;
    }

    public ServiceManager<AVTransportService> getAvTransport() {
        return avTransport;
    }


    protected Icon createDefaultDeviceIcon(byte[] data) {
        return new Icon("image/png", 48, 48, 32, "msi.png", data);
    }

}
