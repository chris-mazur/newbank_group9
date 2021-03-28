package newbank.server;

import java.util.ArrayList;

public class Customer {
	
	private ArrayList<Account> accounts;
	private ArrayList<Loan> currentLoansOffered; // keep a record of all loans offered to other customers
	private String password;
	
	public Customer() {
		accounts = new ArrayList<>();
		currentLoansOffered = new ArrayList<>();
	}
	
	public String accountsToString() {
		String s = "";
		boolean first = true;
		for(Account a : accounts) {
			if(!first) {
				s += "\n";
			} else {
				first = false;
			}
			s += a.toString();
		}
		return s;
	}

	public void setPassword(String newPassword) {
		password = newPassword;
	}

	public String getPassword() {
		return password;
	}

	public void addAccount(Account account) {
		accounts.add(account);		
	}

	// return a requested customer account if it exists (or null if not)
	public Account getAccount(String accountName) {
		for(Account account : accounts) {
			if(account.getName().equals(accountName)) {
				return account;
			}
		}
		return null;
	}

	// add a loan to the customer account in which the customer is the lender
	public void offerLoan(Loan newLoan) {
		currentLoansOffered.add(newLoan);
	}

	// return the number of loans currently offered by the customer
	public int numLoansOffered() {
		return currentLoansOffered.size();
	}

	// display details about the loans currently offered by the customer
	public String showLoansOffered() {
		String loanList = "";
		for (Loan loan : currentLoansOffered) {
			loanList += loan.displayDetails() + "\n";
		}
		return loanList;
	}

}
