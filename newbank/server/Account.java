package newbank.server;

public class Account {
	
	private String accountName;
	private double accountBalance;

	public Account(String accountName, double openingBalance) {
		this.accountName = accountName;
		this.accountBalance = openingBalance;
	}
	
	public String toString() {
		return (accountName + ": " + accountBalance);
	}

	public String getName() {
		return accountName;
	}

	public String getBalance() {
		return String.valueOf(accountBalance);
	}

	public void withdrawFunds(double amount) {
		accountBalance -= amount;
	}

	public void depositFunds(double amount) {
		accountBalance += amount;
	}

}
