package ie.cortexx.gui.settings;

import ie.cortexx.gui.util.UI;
import ie.cortexx.model.MerchantDetails;
import ie.cortexx.service.SettingsService;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

// edit merchant details, VAT rate, templates
public class SettingsPanel extends JPanel {
    private final SettingsService settingsService;

    public SettingsPanel() {
        this(new SettingsService());
    }

    SettingsPanel(SettingsService settingsService) {
        this.settingsService = settingsService;
        UI.applyPanelNoPad(this);
        add(UI.innerTabs(
            UI.tab("Merchant Details", buildMerchant()),
            UI.tab("System Config", buildConfig()),
            UI.tab("Templates", buildTemplates())
        ));
    }

    private JPanel buildMerchant() {
        var details = settingsService.loadMerchantDetails();
        var businessName = new JTextField(text(details.getBusinessName()));
        var email = new JTextField(text(details.getEmail()));
        var phone = new JTextField(text(details.getPhone()));
        var address = new JTextField(text(details.getAddress()));
        var saUsername = new JTextField(text(details.getSaUsername()));
        var saPassword = new JPasswordField(text(details.getSaPassword()));

        var form = UI.formCard();
        form.add(UI.formRow(
            UI.field("Business Name", businessName),
            UI.field("Email", email)
        ));
        form.add(UI.gap(8));
        form.add(UI.formRow(
            UI.field("Phone", phone),
            UI.field("Address", address)
        ));
        form.add(UI.gap(8));
        form.add(UI.formRow(
            UI.field("SA Username", saUsername),
            UI.field("SA Password", saPassword)
        ));
        form.add(UI.gap(8));

        var saveBtn = UI.primaryButton("Save Changes");
        saveBtn.addActionListener(e -> {
            details.setBusinessName(businessName.getText().trim());
            details.setEmail(email.getText().trim());
            details.setPhone(phone.getText().trim());
            details.setAddress(address.getText().trim());
            details.setSaUsername(saUsername.getText().trim());
            details.setSaPassword(new String(saPassword.getPassword()));

            try {
                settingsService.saveMerchantDetails(details);
                UI.notifySuccess(this, "Merchant details saved.");
            } catch (SQLException error) {
                showSaveError(error);
            }
        });
        form.add(saveBtn);
        return UI.formPage(form);
    }

    private JPanel buildConfig() {
        var config = settingsService.loadConfigValues();
        var vatRate = new JTextField(config.vatRate());
        var markupRate = new JTextField(config.markupRatePercent());
        var currency = new JTextField(config.currencyCode());

        var form = UI.formCard();
        form.add(UI.field("VAT Rate (%)", vatRate));
        form.add(UI.gap(20));
        form.add(UI.field("Markup Rate (%)", markupRate));
        form.add(UI.gap(20));
        form.add(UI.field("Currency", currency));
        form.add(UI.gap(20));
        var saveButton = UI.primaryButton("Save");
        saveButton.addActionListener(e -> {
            try {
                settingsService.saveConfigValues(new SettingsService.ConfigValues(
                    vatRate.getText().trim(),
                    markupRate.getText().trim(),
                    currency.getText().trim()
                ));
                UI.notifySuccess(this, "System config saved.");
            } catch (SQLException error) {
                showSaveError(error);
            }
        });
        form.add(saveButton);
        return UI.formPage(form);
    }

    private JPanel buildTemplates() {
        var templates = settingsService.loadReminderTemplateValues();
        var firstArea = UI.textArea(templates.firstReminder(), 6);
        var secondArea = UI.textArea(templates.secondReminder(), 6);

        var form = UI.formCard();
        form.add(UI.field("1st Reminder Template", new JScrollPane(firstArea)));
        form.add(UI.gap(12));
        form.add(UI.field("2nd Reminder Template", new JScrollPane(secondArea)));
        form.add(UI.gap(12));
        var saveButton = UI.primaryButton("Save Templates");
        saveButton.addActionListener(e -> {
            try {
                settingsService.saveReminderTemplateValues(new SettingsService.ReminderTemplateValues(
                    firstArea.getText(),
                    secondArea.getText()
                ));
                UI.notifySuccess(this, "Templates saved.");
            } catch (SQLException error) {
                showSaveError(error);
            }
        });
        form.add(saveButton);
        return UI.formPage(form);
    }

    private String text(String value) {
        return value != null ? value : "";
    }

    private void showSaveError(SQLException error) {
        UI.notifyError(this, error.getMessage());
    }
}
