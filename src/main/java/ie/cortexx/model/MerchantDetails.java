package ie.cortexx.model;

// maps to `merchant_details` (only 1 row ever, CHECK constraint)
// cosymed ltd: Mr Alex Wright, 25 Bond Street, ACC0002
public class MerchantDetails {
    private int merchantId;
    private String businessName;
    private String address;
    private String phone;
    private String email;
    // VARCHAR, ids like 'ACC0002'
    private String saMerchantId;
    // SA login creds, cosymed/bondstreet per marking criteria
    private String saUsername;
    private String saPassword;

    // TODO: generate getters & setters
}
