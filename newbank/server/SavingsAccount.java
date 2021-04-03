package newbank.server;

public class SavingsAccount extends Account {

    public SavingsAccount(String accountSortCode, int accountNumber, String accountName, double openingBalance) {
        super(accountSortCode, accountNumber, accountName, openingBalance);
        this.canPay = false;
        this.canLoan = false;
        this.accountType = "Savings";
    }
    //TODO - Savings Accounts could be associated with an interest rate set by the bank
}
