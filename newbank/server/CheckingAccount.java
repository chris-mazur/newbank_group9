package newbank.server;

public class CheckingAccount extends Account {

    public CheckingAccount(String accountSortCode, int accountNumber, String accountName, double openingBalance) {
        super(accountSortCode, accountNumber, accountName, openingBalance);
        this.canPay = true;
        this.canLoan = true;
        this.accountType = "Checking";
    }

}
