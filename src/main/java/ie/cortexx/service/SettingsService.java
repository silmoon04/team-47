package ie.cortexx.service;

import ie.cortexx.dao.MerchantDetailsDAO;
import ie.cortexx.dao.SystemConfigDAO;
import ie.cortexx.dao.TemplateDAO;
import ie.cortexx.model.MerchantDetails;
import ie.cortexx.model.Template;

import java.math.BigDecimal;
import java.sql.SQLException;

// keeps settings db reads/writes out of the swing panel
public class SettingsService {
    private static final String DEFAULT_SA_MERCHANT_ID = "ACC-0001";

    private final MerchantDetailsDAO merchantDetailsDAO;
    private final SystemConfigDAO systemConfigDAO;
    private final TemplateDAO templateDAO;

    public SettingsService() {
        this(new MerchantDetailsDAO(), new SystemConfigDAO(), new TemplateDAO());
    }

    public SettingsService(MerchantDetailsDAO merchantDetailsDAO, SystemConfigDAO systemConfigDAO, TemplateDAO templateDAO) {
        this.merchantDetailsDAO = merchantDetailsDAO;
        this.systemConfigDAO = systemConfigDAO;
        this.templateDAO = templateDAO;
    }

    public MerchantDetails loadMerchantDetails() {
        try {
            var details = merchantDetailsDAO.get();
            return details != null ? details : defaultMerchantDetails();
        } catch (SQLException error) {
            return defaultMerchantDetails();
        }
    }

    public void saveMerchantDetails(MerchantDetails details) throws SQLException {
        merchantDetailsDAO.update(details);
    }

    public ConfigValues loadConfigValues() {
        try {
            var config = systemConfigDAO.findAll();
            return new ConfigValues(
                text(config.get("vat_rate")),
                displayMarkup(config.get("default_markup_rate")),
                text(config.get("currency_code"))
            );
        } catch (SQLException error) {
            return new ConfigValues("", "100.00", "");
        }
    }

    public void saveConfigValues(ConfigValues values) throws SQLException {
        systemConfigDAO.saveValue("vat_rate", values.vatRate().trim());
        systemConfigDAO.saveValue("default_markup_rate", storeMarkup(values.markupRatePercent().trim()));
        systemConfigDAO.saveValue("currency_code", values.currencyCode().trim());
    }

    public ReminderTemplateValues loadReminderTemplateValues() {
        try {
            return new ReminderTemplateValues(
                loadTemplateContent("REMINDER_1ST", "FIRST"),
                loadTemplateContent("REMINDER_2ND", "SECOND")
            );
        } catch (SQLException error) {
            return new ReminderTemplateValues("", "");
        }
    }

    public void saveReminderTemplateValues(ReminderTemplateValues values) throws SQLException {
        saveTemplate("REMINDER_1ST", "FIRST", values.firstReminder());
        saveTemplate("REMINDER_2ND", "SECOND", values.secondReminder());
    }

    private String loadTemplateContent(String primaryType, String fallbackType) throws SQLException {
        var template = loadTemplate(primaryType, fallbackType);
        return template != null ? text(template.getContent()) : "";
    }

    private Template loadTemplate(String primaryType, String fallbackType) throws SQLException {
        var template = templateDAO.findByType(primaryType);
        return template != null ? template : templateDAO.findByType(fallbackType);
    }

    private void saveTemplate(String primaryType, String fallbackType, String content) throws SQLException {
        var template = loadTemplate(primaryType, fallbackType);
        if (template == null) {
            return;
        }

        template.setContent(content);
        templateDAO.update(template);
    }

    private MerchantDetails defaultMerchantDetails() {
        var details = new MerchantDetails();
        details.setMerchantId(1);
        details.setSaMerchantId(DEFAULT_SA_MERCHANT_ID);
        return details;
    }

    private String text(String value) {
        return value != null ? value : "";
    }

    private String displayMarkup(String value) {
        if (value == null || value.isBlank()) {
            return "100.00";
        }

        try {
            return new BigDecimal(value).movePointRight(2).stripTrailingZeros().toPlainString();
        } catch (NumberFormatException error) {
            return value;
        }
    }

    private String storeMarkup(String value) {
        if (value == null || value.isBlank()) {
            return "1.00";
        }

        try {
            return new BigDecimal(value).movePointLeft(2).stripTrailingZeros().toPlainString();
        } catch (NumberFormatException error) {
            return value;
        }
    }

    public record ConfigValues(String vatRate, String markupRatePercent, String currencyCode) {}

    public record ReminderTemplateValues(String firstReminder, String secondReminder) {}
}