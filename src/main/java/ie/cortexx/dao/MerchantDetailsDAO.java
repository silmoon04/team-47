package ie.cortexx.dao;

import ie.cortexx.model.MerchantDetails;
import java.sql.*;

// handles SQL for `merchant_details` (singleton row)
public class MerchantDetailsDAO {

    // TODO: load the singleton merchant_details row for SettingsPanel and document rendering.
    public MerchantDetails get() throws SQLException {
        return null;
    }

    // TODO: persist pharmacy identity + SA credentials updates from SettingsPanel.
    public void update(MerchantDetails md) throws SQLException {
    }
}
