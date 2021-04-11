package newbank.server;

abstract class Account {
	
	protected String accountName;
	protected String accountSortCode;
	protected int accountNumber;
	protected double accountBalance;
	protected boolean canPay;
	protected boolean canLoan;
	protected String accountType;

	public Account(String accountSortCode, int accountNumber, String accountName, double openingBalance) {
		this.accountSortCode = accountSortCode;
		this.accountNumber = accountNumber;
		this.accountName = accountName;
		this.accountBalance = openingBalance;
	}

	public String toString() {
		return (accountType + " - " + accountName + " (" + accountSortCode + " " + accountNumber + "): " + String.format("%.2f",accountBalance));
	}

	public String getName() {
		return accountName;
	}

	public String getAccountType() {
		return accountType;
	}

	public String getAccountSortCode() {
		return accountSortCode;
	}

	public int getAccountNumber() {
		return accountNumber;
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
