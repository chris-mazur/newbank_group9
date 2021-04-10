package newbank.server;

public class BankVault extends Account {

    public BankVault(String accountName, double openingBalance) {
        super(accountName, openingBalance);
        this.canPay = true;
        this.canLoan = true;
        this.accountType = "Bank Vault";
    }
}
