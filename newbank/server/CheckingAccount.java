package newbank.server;

public class CheckingAccount extends Account {

    public CheckingAccount(String accountName, double openingBalance) {
        super(accountName, openingBalance);
        this.canPay = true;
        this.canLoan = true;
        this.accountType = "Checking";
    }
}
