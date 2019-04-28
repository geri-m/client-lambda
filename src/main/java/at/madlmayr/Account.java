package at.madlmayr;

public interface Account {

    String TABLE_NAME = "Accounts";
    String COLUMN_COMPANY_TOOL = "companyToolTimestamp";
    String COLUMN_ID = "id";

    String getCompanyToolTimestamp();

    void setCompanyToolTimestamp(final String ts);

    String getName();

    String getEmail();

}
