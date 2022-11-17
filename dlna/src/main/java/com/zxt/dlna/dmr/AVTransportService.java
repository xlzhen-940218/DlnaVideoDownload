
package com.zxt.dlna.dmr;


import org.fourthline.cling.model.types.ErrorCode;
import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;
import org.fourthline.cling.support.avtransport.AVTransportErrorCode;
import org.fourthline.cling.support.avtransport.AVTransportException;
import org.fourthline.cling.support.avtransport.AbstractAVTransportService;
import org.fourthline.cling.support.avtransport.lastchange.AVTransportVariable;
import org.fourthline.cling.support.lastchange.LastChange;
import org.fourthline.cling.support.model.DeviceCapabilities;
import org.fourthline.cling.support.model.MediaInfo;
import org.fourthline.cling.support.model.PlayMode;
import org.fourthline.cling.support.model.PositionInfo;
import org.fourthline.cling.support.model.SeekMode;
import org.fourthline.cling.support.model.StorageMedium;
import org.fourthline.cling.support.model.TransportAction;
import org.fourthline.cling.support.model.TransportInfo;
import org.fourthline.cling.support.model.TransportSettings;
import org.seamless.http.HttpFetch;
import org.seamless.util.URIUtil;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.logging.Logger;

/**
 * @author offbye
 */
public class AVTransportService extends AbstractAVTransportService {

    final private static Logger log = Logger.getLogger(AVTransportService.class.getName());

    private static final String TAG = "GstAVTransportService";

    private URI uri;
    private String currentURIMetaData;
    private Callback callback;
    protected AVTransportService(LastChange lastChange,Callback callback) {
        super(lastChange);
        this.callback=callback;
    }

    @Override
    public void setAVTransportURI(UnsignedIntegerFourBytes instanceId,
                                  String currentURI,
                                  String currentURIMetaData) throws AVTransportException {
        System.out.println(TAG+ currentURI + "---" +currentURIMetaData );
        URI uri;
        try {
            uri = new URI(currentURI);
        } catch (Exception ex) {
            throw new AVTransportException(
                    ErrorCode.INVALID_ARGS, "CurrentURI can not be null or malformed"
            );
        }

        if (currentURI.startsWith("http:")||currentURI.startsWith("https:")) {
            try {
                HttpFetch.validate(URIUtil.toURL(uri));
            } catch (Exception ex) {
                ex.printStackTrace();
                /*throw new AVTransportException(
                        AVTransportErrorCode.RESOURCE_NOT_FOUND, ex.getMessage()
                );*/
            }
        } else if (!currentURI.startsWith("file:")) {
            throw new AVTransportException(
                    ErrorCode.INVALID_ARGS, "Only HTTP and file: resource identifiers are supported"
            );
        }

        // TODO: Check mime type of resource against supported types
        // TODO: DIDL fragment parsing and handling of currentURIMetaData
        String type="video/mp4";

        if (currentURIMetaData.contains("object.item.videoItem")) {
            type = "video";
        } else if (currentURIMetaData.contains("object.item.imageItem")) {
            type = "image";
        } else if (currentURIMetaData.contains("object.item.audioItem")) {
            type = "audio";
        }else {
            try {
                HttpURLConnection urlConnection = (HttpURLConnection) new URL(currentURI).openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();
                type = urlConnection.getHeaderField("Content-Type");
                urlConnection.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        String name=currentURI;
        if(currentURIMetaData!=null){
            try {
                currentURIMetaData= currentURIMetaData.replace("&lt;","<").replace("&gt;",">");
                name= currentURIMetaData.substring(currentURIMetaData.indexOf("<dc:title>") + 10,
                        currentURIMetaData.indexOf("</dc:title>"));
            }catch (Exception ex){
                ex.printStackTrace();
            }

        }

        System.out.println(TAG+ name);
        this.uri=uri;
        this.currentURIMetaData=currentURIMetaData;
        callback.openURL(currentURI,name,type);

       // getInstance(instanceId).setURI(uri,type,name,currentURIMetaData);
    }

    public interface Callback{
        void openURL(String url,String name,String type);
    }

    @Override
    public MediaInfo getMediaInfo(UnsignedIntegerFourBytes instanceId) throws AVTransportException {
        return new MediaInfo(uri.toString(),currentURIMetaData);
    }

    @Override
    public TransportInfo getTransportInfo(UnsignedIntegerFourBytes instanceId) throws AVTransportException {
        return new TransportInfo();
    }

    @Override
    public PositionInfo getPositionInfo(UnsignedIntegerFourBytes instanceId) throws AVTransportException {
        return new PositionInfo();
    }

    @Override
    public DeviceCapabilities getDeviceCapabilities(UnsignedIntegerFourBytes instanceId) throws AVTransportException {

        return new DeviceCapabilities(new StorageMedium[]{StorageMedium.NETWORK});
    }

    @Override
    public TransportSettings getTransportSettings(UnsignedIntegerFourBytes instanceId) throws AVTransportException {

        return new TransportSettings(PlayMode.NORMAL);
    }

    @Override
    public void stop(UnsignedIntegerFourBytes instanceId) throws AVTransportException {

    }

    @Override
    public void play(UnsignedIntegerFourBytes instanceId, String speed) throws AVTransportException {

    }

    @Override
    public void pause(UnsignedIntegerFourBytes instanceId) throws AVTransportException {

    }

    @Override
    public void record(UnsignedIntegerFourBytes instanceId) throws AVTransportException {
        // Not implemented
        log.info("### TODO: Not implemented: Record");
    }

    @Override
    public void seek(UnsignedIntegerFourBytes instanceId, String unit, String target) throws AVTransportException {

    }

    @Override
    public void next(UnsignedIntegerFourBytes instanceId) throws AVTransportException {
        // Not implemented
        log.info("### TODO: Not implemented: Next");
    }

    @Override
    public void previous(UnsignedIntegerFourBytes instanceId) throws AVTransportException {
        // Not implemented
        log.info("### TODO: Not implemented: Previous");
    }

    @Override
    public void setNextAVTransportURI(UnsignedIntegerFourBytes instanceId,
                                      String nextURI,
                                      String nextURIMetaData) throws AVTransportException {
        log.info("### TODO: Not implemented: SetNextAVTransportURI");
        // Not implemented
    }

    @Override
    public void setPlayMode(UnsignedIntegerFourBytes instanceId, String newPlayMode) throws AVTransportException {
        // Not implemented
        log.info("### TODO: Not implemented: SetPlayMode");
    }

    @Override
    public void setRecordQualityMode(UnsignedIntegerFourBytes instanceId, String newRecordQualityMode) throws AVTransportException {
        // Not implemented
        log.info("### TODO: Not implemented: SetRecordQualityMode");
    }

    @Override
    protected TransportAction[] getCurrentTransportActions(UnsignedIntegerFourBytes instanceId) throws Exception {
        return new TransportAction[]{
                TransportAction.Play
        };
    }

    @Override
    public UnsignedIntegerFourBytes[] getCurrentInstanceIds() {
        UnsignedIntegerFourBytes[] ids = new UnsignedIntegerFourBytes[0];
        return ids;
    }
}
