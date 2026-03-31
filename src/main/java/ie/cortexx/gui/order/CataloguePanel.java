package ie.cortexx.gui.order;

import ie.cortexx.gui.util.UI;

import javax.swing.*;
import java.awt.*;

/*
simple toolbar + table layout using UI.toolbarAndTable().
this stacks the toolbar on top and the table below, which is the
standard layout for panels that dont need stat cards.

toolbar has search on the left and two buttons on the right.
UI.toolbar(hint, table, buttons...) accepts varargs JButtons,
so we pass UI.button() and UI.primaryButton() directly.

the catalogue data comes from SAProxyService.getCatalogue() which
calls the SA API. for now its placeholder rows.

mono on ID, cost, and availability cols bc theyre numbers/prices.
*/

// shows SA catalogue products, lets you search and place orders
public class CataloguePanel extends JPanel {
    public CataloguePanel() {
        // dark bg, 20px padding, BorderLayout
        UI.applyPanel(this);

        // table
        // mono on numbers/prices cols
        var t = UI.table("Item ID", "Product Name", "Package Cost", "SA Availability");
        t.monoColumn(0).monoColumn(2).monoColumn(3);

        // TODO: swap with SAProxyService.getCatalogue() loop
        t.model().addRow(new Object[]{"100 00001", "Paracetamol", "£0.10", "10,345 packs"});
        t.model().addRow(new Object[]{"300 00002", "Amopen", "£15.00", "1,340 packs"});

        // toolbar
        // search left, Sync + Place Order buttons right
        // toolbar(hint, table, buttons...) accepts varargs JButtons
        var toolbar = UI.toolbar("Search catalogue...", t.table(),
            UI.button("Sync Catalogue"), UI.primaryButton("Place Order"));

        // toolbarAndTable stacks toolbar NORTH, table CENTER
        add(UI.toolbarAndTable(toolbar, t.scroll()), BorderLayout.CENTER);
    }
}
