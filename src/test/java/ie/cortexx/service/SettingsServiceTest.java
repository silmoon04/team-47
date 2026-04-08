package ie.cortexx.service;

import ie.cortexx.dao.MerchantDetailsDAO;
import ie.cortexx.dao.SystemConfigDAO;
import ie.cortexx.dao.TemplateDAO;
import ie.cortexx.model.Template;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SettingsServiceTest {

    @Mock
    private MerchantDetailsDAO merchantDetailsDAO;

    @Mock
    private SystemConfigDAO systemConfigDAO;

    @Mock
    private TemplateDAO templateDAO;

    @Test
    void load_merchant_details_returns_default_when_row_is_missing() throws Exception {
        var service = new SettingsService(merchantDetailsDAO, systemConfigDAO, templateDAO);
        when(merchantDetailsDAO.get()).thenReturn(null);

        var details = service.loadMerchantDetails();

        assertNotNull(details);
        assertEquals(1, details.getMerchantId());
        assertEquals("ACC-1002", details.getSaMerchantId());
    }

    @Test
    void load_config_values_converts_markup_to_percent_for_ui() throws Exception {
        var service = new SettingsService(merchantDetailsDAO, systemConfigDAO, templateDAO);
        when(systemConfigDAO.findAll()).thenReturn(Map.of(
            "vat_rate", "20.00",
            "default_markup_rate", "1.25",
            "currency_code", "GBP"
        ));

        var config = service.loadConfigValues();

        assertEquals("20.00", config.vatRate());
        assertEquals("125", config.markupRatePercent());
        assertEquals("GBP", config.currencyCode());
    }

    @Test
    void save_config_values_converts_percent_back_to_decimal() throws Exception {
        var service = new SettingsService(merchantDetailsDAO, systemConfigDAO, templateDAO);

        service.saveConfigValues(new SettingsService.ConfigValues("20.00", "125", "GBP"));

        verify(systemConfigDAO).saveValue("vat_rate", "20.00");
        verify(systemConfigDAO).saveValue("default_markup_rate", "1.25");
        verify(systemConfigDAO).saveValue("currency_code", "GBP");
    }

    @Test
    void load_reminder_template_values_falls_back_to_legacy_types() throws Exception {
        var service = new SettingsService(merchantDetailsDAO, systemConfigDAO, templateDAO);
        var first = new Template("FIRST", "first body");
        var second = new Template("SECOND", "second body");

        when(templateDAO.findByType("REMINDER_1ST")).thenReturn(null);
        when(templateDAO.findByType("FIRST")).thenReturn(first);
        when(templateDAO.findByType("REMINDER_2ND")).thenReturn(null);
        when(templateDAO.findByType("SECOND")).thenReturn(second);

        var templates = service.loadReminderTemplateValues();

        assertEquals("first body", templates.firstReminder());
        assertEquals("second body", templates.secondReminder());
    }

    @Test
    void save_reminder_template_values_updates_fallback_templates_when_primary_types_are_missing() throws Exception {
        var service = new SettingsService(merchantDetailsDAO, systemConfigDAO, templateDAO);
        var first = new Template("FIRST", "old first");
        first.setTemplateId(1);
        var second = new Template("SECOND", "old second");
        second.setTemplateId(2);

        when(templateDAO.findByType("REMINDER_1ST")).thenReturn(null);
        when(templateDAO.findByType("FIRST")).thenReturn(first);
        when(templateDAO.findByType("REMINDER_2ND")).thenReturn(null);
        when(templateDAO.findByType("SECOND")).thenReturn(second);

        service.saveReminderTemplateValues(new SettingsService.ReminderTemplateValues("new first", "new second"));

        assertEquals("new first", first.getContent());
        assertEquals("new second", second.getContent());
        verify(templateDAO).update(first);
        verify(templateDAO).update(second);
    }

    @Test
    void load_config_values_returns_defaults_on_sql_error() throws Exception {
        var service = new SettingsService(merchantDetailsDAO, systemConfigDAO, templateDAO);
        when(systemConfigDAO.findAll()).thenThrow(new SQLException("db down"));

        var config = service.loadConfigValues();

        assertEquals("", config.vatRate());
        assertEquals("100.00", config.markupRatePercent());
        assertEquals("", config.currencyCode());
    }
}