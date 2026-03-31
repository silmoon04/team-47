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

    public MerchantDetails() {}

    public MerchantDetails(String businessName, String address, String phone, String email, String saMerchantId) {
        this.merchantId = 1;
        this.businessName = businessName;
        this.address = address;
        this.phone = phone;
        this.email = email;
        this.saMerchantId = saMerchantId;
    }

    public int getMerchantId() { return merchantId; }
    public void setMerchantId(int merchantId) { this.merchantId = merchantId; }

    public String getBusinessName() { return businessName; }
    public void setBusinessName(String businessName) { this.businessName = businessName; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getSaMerchantId() { return saMerchantId; }
    public void setSaMerchantId(String saMerchantId) { this.saMerchantId = saMerchantId; }

    public String getSaUsername() { return saUsername; }
    public void setSaUsername(String saUsername) { this.saUsername = saUsername; }

    public String getSaPassword() { return saPassword; }
    public void setSaPassword(String saPassword) { this.saPassword = saPassword; }
}
