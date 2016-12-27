package ch.bailu.aat.views.preferences;

import android.content.SharedPreferences;
import android.view.View;

import ch.bailu.aat.helpers.AppLog;
import ch.bailu.aat.preferences.SolidIndexList;
import ch.bailu.aat.views.ImageButtonView;


public class SolidImageButton extends ImageButtonView implements SharedPreferences.OnSharedPreferenceChangeListener {

    private final SolidIndexList solid;
    
    public SolidImageButton(SolidIndexList s) {
        super(s.getContext(), s.getIconResource());

        solid = s;
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (solid.length()<3) {
                    solid.cycle();
                } else {
                    new SolidIndexListDialog(solid);
                }

            }

        });
    }


    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        setImageResource(solid.getIconResource());
        solid.register(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (solid.hasKey(key)) {
            setImageResource(solid.getIconResource());
            AppLog.i(getContext(), solid.getValueAsString());
        }
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        solid.unregister(this);
    }
}
