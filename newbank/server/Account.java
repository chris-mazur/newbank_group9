package newbank.server;

abstract class Account {
	
	private String accountName;
	private double accountBalance;
	protected boolean canPay;
	protected boolean canLoan;
	protected String accountType;

	public Account(Account account) {
		this.accountName = account.accountName;
		this.accountBalance = account.accountBalance;
		this.canPay = account.canPay;
		this.accountType = account.accountType;
	}
	
	public Account(String accountName, double openingBalance) {
		this.accountName = accountName;
		this.accountBalance = openingBalance;
	}
	
	public String toString() {
		return (accountType + " - " + accountName + ": " + accountBalance);
	}

	public String getName() {
		return accountName;
	}
	
	public void setName(String name) {
		this.accountName = name;
	}

	public String getAccountType() {
		return accountType;
	}

	public double getBalance() {
		return accountBalance;
	}

	public void withdrawFunds(double amount) {
		accountBalance -= amount;
	}

	public void depositFunds(double amount) {
		accountBalance += amount;
	}

}
