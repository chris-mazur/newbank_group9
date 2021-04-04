package newbank.server;

public class CheckingAccount extends Account {

    public CheckingAccount(String accountName, double openingBalance) {
        super(accountName, openingBalance);
        this.canPay = true;
        this.canLoan = true;
        this.accountType = "Checking";
    }
    
	public CheckingAccount(Account account) {
		super(account);
		this.canPay = account.canPay;
        this.canLoan = account.canLoan;
		this.accountType = account.accountType;
	}
}
