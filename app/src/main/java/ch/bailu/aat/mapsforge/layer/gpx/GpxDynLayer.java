package ch.bailu.aat.mapsforge.layer.gpx;

import android.content.SharedPreferences;

import org.mapsforge.core.graphics.Canvas;
import org.mapsforge.core.model.BoundingBox;
import org.mapsforge.core.model.Point;

import ch.bailu.aat.dispatcher.DispatcherInterface;
import ch.bailu.aat.dispatcher.OnContentUpdatedInterface;
import ch.bailu.aat.gpx.GpxInformation;
import ch.bailu.aat.gpx.GpxInformationCache;
import ch.bailu.aat.gpx.interfaces.GpxType;
import ch.bailu.aat.mapsforge.layer.MapsForgeLayer;
import ch.bailu.aat.mapsforge.layer.context.MapContext;
import ch.bailu.aat.preferences.SolidLegend;
import ch.bailu.aat.services.ServiceContext;

public class GpxDynLayer extends MapsForgeLayer implements OnContentUpdatedInterface {
    private final GpxInformationCache infoCache = new GpxInformationCache();

    private GpxLayer gpxOverlay;
    private GpxLayer legendOverlay;

    private final SolidLegend slegend;

    private final ServiceContext scontext;
    private final MapContext mcontext;
    private final int color;



    public GpxDynLayer(MapContext mc, int c) {
        mcontext = mc;
        scontext = mc.scontext;

        color = c;

        slegend = new SolidLegend(mcontext.context, mcontext.skey);

        createLegendOverlay();
        createGpxOverlay();
    }


    public GpxDynLayer(MapContext mc,
                         DispatcherInterface dispatcher, int iid) {
        this(mc, -1);
        dispatcher.addTarget(this, iid);
    }

    @Override
    public void draw(BoundingBox b, byte z, Canvas c, Point t) {
        gpxOverlay.draw(b,z,c,t);
        legendOverlay.draw(b,z,c,t);
    }

    private int type = GpxType.NONE;

    @Override
    public void onContentUpdated(int iid, GpxInformation i) {
        infoCache.set(iid, i);

        if (type != toType(i)) {
            type = toType(i);

            createGpxOverlay();
            createLegendOverlay();
        }

        infoCache.letUpdate(gpxOverlay);
        infoCache.letUpdate(legendOverlay);

        requestRedraw();
    }



    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (slegend.hasKey(key)) {
            createLegendOverlay();
            infoCache.letUpdate(legendOverlay);
            requestRedraw();
        }
    }

    private static int toType(GpxInformation i) {
        if (i != null && i.getGpxList() != null) {
            return i.getGpxList().getDelta().getType();
        }
        return GpxType.NONE;
    }




    private void createGpxOverlay() {
        int type = toType(infoCache.info);

        if (type == GpxType.WAY)
            gpxOverlay = new WayLayer(mcontext, color);

        else if (type == GpxType.RTE)
            gpxOverlay = new RouteLayer(mcontext, color);

        else
            gpxOverlay = new TrackLayer(mcontext);

    }


    private void createLegendOverlay() {
        int type = toType(infoCache.info);

        if (type == GpxType.WAY)
            legendOverlay = slegend.createWayLegendOverlayMF(mcontext);

        else if (type == GpxType.RTE)
            legendOverlay = slegend.createRouteLegendOverlayMF(mcontext);

        else
            legendOverlay = slegend.createTrackLegendOverlayMF(mcontext);
    }

    @Override
    public void onLayout(boolean changed, int l, int t, int r, int b) {

    }


}
