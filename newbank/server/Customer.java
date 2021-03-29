package newbank.server;

import java.util.ArrayList;

public class Customer {
	
	private ArrayList<Account> accounts;
	private ArrayList<Loan> currentLoansOffered; // keep a record of all loans currently offered to other customers
	private ArrayList<Loan> currentLoansReceived; // keep a record of all outstanding loans to be paid back
	private String password;
	
	public Customer() {
		accounts = new ArrayList<>();
		currentLoansOffered = new ArrayList<>();
		currentLoansReceived = new ArrayList<>();
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

	// return the total amount of money held across all of the customer's accounts
	public double getTotalFunds() {
		double totalFunds = 0;
		for (Account account : accounts) {
			totalFunds += account.getBalance();
		}
		return totalFunds;
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
			loanList += loan.displayLenderDetails() + "\n";
		}
		return loanList;
	}

	// add a loan to the customer account in which the customer is the borrower
	public void receiveLoan(Loan newLoan) {
		currentLoansReceived.add(newLoan);
	}

	// return the number of loans the customer currently has to repay
	public int numLoansReceived() {
		return currentLoansReceived.size();
	}

	// display details about the loans that the customer has to repay
	public String showLoansReceived() {
		String loanList = "";
		for (Loan loan : currentLoansReceived) {
			loanList += loan.displayBorrowerDetails() + "\n";
		}
		return loanList;
	}

	// retrieves a loan that the customer has taken out
	public Loan getLoan(String loanID) {
		for (Loan loan : currentLoansReceived) {
			if (loan.getLoanID().equals(loanID)) {
				return loan;
			}
		}
		return null;
	}

}
