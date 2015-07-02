package com.fang.map;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;

/**
 * Created by benren.fj on 6/30/15.
 */
public class BDMapListener implements BDLocationListener {

    BDLocation bdLocation;


    private BDMapListener() {

    }

    private static class Holder {
        private static BDMapListener instance = new BDMapListener();
    }

    public static BDMapListener getInstance() {
        return Holder.instance;
    }

    @Override
    public void onReceiveLocation(BDLocation bdLocation) {
        if (bdLocation == null)
            return ;
        int code = bdLocation.getLocType();
        if (code == BDLocation.TypeGpsLocation ||
                code == BDLocation.TypeOffLineLocation ||
                code == BDLocation.TypeNetWorkLocation ||
                code == BDLocation.TypeCacheLocation
                ) {
            this.bdLocation = bdLocation;
        }
    }

    public BDLocation getBdLocation() {
        return bdLocation;
    }
}
