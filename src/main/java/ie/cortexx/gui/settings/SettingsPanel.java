package ie.cortexx.gui.settings;

import ie.cortexx.gui.util.UI;

import javax.swing.*;
import java.awt.*;

// edit merchant details, VAT rate, templates
public class SettingsPanel extends JPanel {
    public SettingsPanel() {
        UI.applyPanelNoPad(this);
        add(UI.innerTabs(
            UI.tab("Merchant Details", buildMerchant()),
            UI.tab("System Config", buildConfig()),
            UI.tab("Templates", buildTemplates())
        ));
    }

    private JPanel buildMerchant() {
        JPanel form = UI.formCard();
        form.add(UI.formRow(
            UI.field("Business Name", new JTextField("Cosymed Ltd")),
            UI.field("Contact Name", new JTextField("Alex Wright"))
        ));
        form.add(UI.gap(8));
        form.add(UI.formRow(
            UI.field("Phone", new JTextField("0207 321 8001")),
            UI.field("Address", new JTextField("25 Bond Street, London WC1V 8LS"))
        ));
        form.add(UI.gap(8));
        form.add(UI.formRow(
            UI.field("SA Username", new JTextField("cosymed")),
            UI.field("SA Password", new JPasswordField("bondstreet"))
        ));
        form.add(UI.gap(8));

        var saveBtn =UI.primaryButton("Save Changes");
        form.add(saveBtn);
        return UI.formPage(form);
    }

    private JPanel buildConfig() {
        JPanel form = UI.formCard();
        form.add(UI.field("VAT Rate (%)", new JTextField("0.00")));
        form.add(UI.gap(20));
        form.add(UI.field("Markup Rate (%)", new JTextField("100.00")));
        form.add(UI.gap(20));
        form.add(UI.field("Currency", new JTextField("GBP")));
        form.add(UI.gap(20));
        form.add(UI.primaryButton("Save"));
        return UI.formPage(form);
    }

    private JPanel buildTemplates() {
        JPanel form = UI.formCard();
        form.add(UI.textAreaField(
            "1st Reminder Template",
            "Dear {customer_name},\n\nYour account ({account_no}) has an outstanding balance of £{amount_owed}.\n\nPlease arrange payment.\n\nRegards,\nCosymed Ltd",
            6
        ));
        form.add(UI.gap(12));
        form.add(UI.textAreaField(
            "2nd Reminder Template",
            "Dear {customer_name},\n\nFINAL NOTICE: Account ({account_no}) remains unpaid. Balance: £{amount_owed}.\n\nRegards,\nCosymed Ltd",
            6
        ));
        form.add(UI.gap(12));
        form.add(UI.primaryButton("Save Templates"));
        return UI.formPage(form);
    }
}
