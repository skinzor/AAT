package ch.bailu.aat.views;

import android.view.View;
import ch.bailu.aat.helpers.AppLayout;
import ch.bailu.aat.menus.OptionsMenu;
import ch.bailu.aat.services.ServiceContext;

public class MainControlBar extends ControlBar {

    private final BusyButton menu;
    
    public MainControlBar(final ServiceContext context) {
        this(context, DEFAULT_VISIBLE_BUTTON_COUNT);
        
    }
    
    
    public MainControlBar(final ServiceContext scontext, int button) {
        super(scontext.getContext(), AppLayout.getOrientationAlongSmallSide(scontext.getContext()), button);
        menu = new BusyButton(scontext.getContext(), ch.bailu.aat.R.drawable.open_menu_inverse);
        this.addView(menu);
        menu.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                new OptionsMenu(scontext).showAsPopup(getContext(), menu);
            }});
        
    }

    public BusyButton getMenu() {
        return menu;
    }
}
