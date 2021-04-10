package newbank.server;

public class BankVault extends Account {

    public BankVault(String accountSortCode, int accountNumber, String accountName, double openingBalance) {
        super(accountSortCode, accountNumber, accountName, openingBalance);
        this.canPay = true;
        this.canLoan = true;
        this.accountType = "Bank Vault";
    }
}
