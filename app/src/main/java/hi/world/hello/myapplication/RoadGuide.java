package hi.world.hello.myapplication;

import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.skt.Tmap.TMapData;
import com.skt.Tmap.TMapPOIItem;
import com.skt.Tmap.TMapPoint;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.ArrayList;

public class RoadGuide {

    private static final String TAG = "RoadGuide";

    TMapData tMapData = new TMapData();
    TMapPoint myLocation;
    TMapPoint destination;

    ArrayList<TMapPOIItem> passList = new ArrayList<TMapPOIItem>();
    ArrayList<String> addressList = new ArrayList<String>();
    ArrayList<String> turntypeList = new ArrayList<String>();
    ArrayList<String> distanceList = new ArrayList<String>();

    // placemark 리스트
    NodeList mPlacemark = null;
    NodeList currentChild = null;

    int lastIndex;
    int nextIndex;
    int lineIndex;

    // constructors
    public RoadGuide() {

    }

    public void setMyLocation(TMapPoint location) {
        if (location != null) {
            myLocation = location;
        } else {
            Log.i(TAG, "Location is null");
        }
    }

    public TMapPOIItem getNextPOI() {
        Log.i("Size", passList.size() + ", " + addressList.size() + ", " + turntypeList.size());
        if (passList.get(nextIndex) != null) {
            return passList.get(nextIndex);
        } else {
            return null;
        }
    }

    public String getTurntype() {
        if (turntypeList.get(nextIndex) != null) {
            return turntypeList.get(nextIndex);
        } else {
            return null;
        }
    }

    private void setIndex() {
        lastIndex = 0;
        nextIndex = 1;
        lineIndex = 0;
    }

    public void changeIndex() {

        // 내 위치에서 다음 경유지까지 거리
        double toNextPOI = passList.get(nextIndex).getDistance(myLocation);
        // 최근 경유지와 다음 경유지 사이 거리
        double inter = Double.parseDouble(distanceList.get(lineIndex));

        if (toNextPOI >= inter) {
            toward(myLocation, destination);
            Log.i("changeIndex", "경로를 재탐색 합니다");
        }
        if (toNextPOI < 10) {
            lastIndex = nextIndex;
            nextIndex++;
            lineIndex++;
        }
    }

    /**
     * @brief des까지의 경로찾기
     * @param des 목적지
     */
    public void toward(TMapPoint src, TMapPoint des){
        Log.i("시작", String.valueOf(src.getLatitude()) + ", " + String.valueOf(src.getLongitude()));
        Log.i("목적", String.valueOf(des.getLatitude()) + ", " + String.valueOf(des.getLongitude()));

        destination = des;
        passList.clear();
        addressList.clear();
        turntypeList.clear();

        tMapData.findPathDataAllType(TMapData.TMapPathType.CAR_PATH, src, des, new TMapData.FindPathDataAllListenerCallback() {
            @Override
            public void onFindPathDataAll(Document document) {
                Element root = document.getDocumentElement();
                NodeList nodeListPlacemark = root.getElementsByTagName("Placemark");
                mPlacemark = nodeListPlacemark;
                currentChild = mPlacemark.item(0).getChildNodes();

                for (int i = 0; i < nodeListPlacemark.getLength(); i++) {
                    NodeList nodeListPlacemarkItem = nodeListPlacemark.item(i).getChildNodes();

                    //Log로 placemark nodelist 내용 확인하는 부분
                    for (int j = 0; j < nodeListPlacemarkItem.getLength(); j++) {
                        if (nodeListPlacemarkItem.item(j).getNodeName().equals("Point")) {
                            TMapPoint tmp = stringToPoint(nodeListPlacemarkItem.item(j).getTextContent().trim());
                            getPointAddress(tmp);
                        }
                        if (nodeListPlacemarkItem.item(j).getNodeName().equals("tmap:turnType")) {
                            turntypeList.add(nodeListPlacemarkItem.item(j).getTextContent().trim());
                        }
                        if (nodeListPlacemarkItem.item(j).getNodeName().equals("tmap:distance")) {
                            distanceList.add(nodeListPlacemarkItem.item(j).getTextContent().trim());
                        }
                    }
                }
                setIndex();
            }
        });
    }

    private void getPointAddress(TMapPoint point){
        tMapData.convertGpsToAddress(point.getLatitude(), point.getLongitude(), new TMapData.ConvertGPSToAddressListenerCallback() {
            @Override
            public void onConvertToGPSToAddress(String strAddress) {
                Log.i(TAG, "point 주소 : " + strAddress);
                addressList.add(strAddress);
                addressPOI(strAddress);
            }
        });

    }

    private TMapPoint stringToPoint(String str) {
        String[] s = str.split(",");

        Double lat = Double.parseDouble(s[1]);
        Double lon = Double.parseDouble(s[0]);

        return new TMapPoint(lat, lon);
    }

    private void addressPOI(String address) {
        tMapData.findAddressPOI(address, new TMapData.FindAddressPOIListenerCallback() {
            @Override
            public void onFindAddressPOI(ArrayList<TMapPOIItem> arrayList) {
                if (arrayList != null) {
                    for (int i = 0; i < arrayList.size(); i++) {
                        TMapPOIItem item = arrayList.get(i);
                        Log.i(TAG + "POI", "POI Name: " + item.getPOIName().toString() + ", " +
                                "Address: " + item.getPOIAddress().replace("null", "") + ", " +
                                "Point: " + item.getPOIPoint().toString());
                    }
                    passList.add(arrayList.get(0));
                } else { }
            }
        });
    }


}