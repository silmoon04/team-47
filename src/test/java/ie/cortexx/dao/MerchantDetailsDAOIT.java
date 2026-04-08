package ie.cortexx.dao;

import ie.cortexx.model.MerchantDetails;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MerchantDetailsDAOIT {

    @Test
    void getReturnsSingletonRow() throws Exception {
        MerchantDetails details = new MerchantDetailsDAO().get();

        assertNotNull(details);
        assertEquals(1, details.getMerchantId());
        assertNotNull(details.getBusinessName());
    }

    @Test
    void updatePersistsChanges() throws Exception {
        MerchantDetailsDAO dao = new MerchantDetailsDAO();
        MerchantDetails details = dao.get();
        assertNotNull(details);

        String originalPhone = details.getPhone();
        details.setPhone(originalPhone + " x");

        try {
            dao.update(details);
            assertEquals(originalPhone + " x", dao.get().getPhone());
        } finally {
            details.setPhone(originalPhone);
            dao.update(details);
        }
    }
}