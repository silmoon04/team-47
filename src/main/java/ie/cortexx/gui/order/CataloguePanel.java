package ie.cortexx.gui.order;

import ie.cortexx.gui.util.UI;

import javax.swing.*;
import java.awt.*;
import java.util.List;

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
    private record CatalogueRow(String itemId, String productName, String packageCost, String availability) {}

    public CataloguePanel() {
        UI.applyPanel(this);

        var table = UI.table(
            UI.monoCol("Item ID", CatalogueRow::itemId),
            UI.col("Product Name", CatalogueRow::productName),
            UI.monoCol("Package Cost", CatalogueRow::packageCost),
            UI.monoCol("SA Availability", CatalogueRow::availability)
        ).rows(List.of(
            new CatalogueRow("100 00001", "Paracetamol", "£0.10", "10,345 packs"),
            new CatalogueRow("300 00002", "Amopen", "£15.00", "1,340 packs")
        ));

        var toolbar = UI.toolbar("Search catalogue...", table.table(),
            UI.button("Sync Catalogue"), UI.primaryButton("Place Order"));

        add(UI.toolbarAndTable(toolbar, table.scroll()), BorderLayout.CENTER);
    }
}
