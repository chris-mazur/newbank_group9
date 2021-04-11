package newbank.server;

import java.util.ArrayList;
import java.util.Date;

public class Customer {
	
	private ArrayList<Account> accounts;
	private ArrayList<Loan> currentLoansOffered; // keep a record of all loans currently offered to other customers
	private ArrayList<Loan> currentLoansReceived; // keep a record of all outstanding loans to be paid back
	private String password;

	private Boolean isAdmin = false;
	private Integer overdraft = 0;
	
	public Integer getOverdraft() {
		return overdraft;
	}

	public void setOverdraft(Integer overdraft) {
		this.overdraft = overdraft;
	}

	public Boolean getIsAdmin() {
		return isAdmin;
	}

	public void setIsAdmin(Boolean admin) {
		this.isAdmin = admin;
	}

	private String phoneNo = null; // mobile
	private String landlinePhoneNo = null;
	
	public String getLandlinePhoneNo() {
		return landlinePhoneNo;
	}

	public void setLandlinePhoneNo(String landlinePhoneNo) {
		this.landlinePhoneNo = landlinePhoneNo;
	}

	private String address = null;
	private String emailAddress = null;
	private String postcode = null;
	
	public String getPostcode() {
		return postcode;
	}

	public void setPostcode(String postcode) {
		this.postcode = postcode;
	}

	public String getPhoneNo() {
		return phoneNo;
	}

	public void setPhoneNo(String phoneNo) {
		this.phoneNo = phoneNo;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getEmailAddress() {
		return emailAddress;
	}

	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;

	}

	public Customer(String password) {
		accounts = new ArrayList<>();
		currentLoansOffered = new ArrayList<>();
		currentLoansReceived = new ArrayList<>();
		setPassword(password);
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

	public String accountBalance(String accName) {
		for(Account a:accounts) {
			if(a.getName() == accName) {
				return Double.toString(a.getBalance());
			}
		}
		return "Account does not exist";
	}

	public boolean hasPassword() {
		return password != null;
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

	// return the total amount of money loaned to other customers
	public double getTotalLoansOffered() {
		double totalLoansOffered = 0;
		for (Loan loan : currentLoansOffered) {
			totalLoansOffered += loan.getLoanValue();
		}
		return totalLoansOffered;
	}

	// return the total amount of money borrowed from other customers
	public double getTotalLoansReceived() {
		double totalLoansReceived = 0;
		for (Loan loan : currentLoansReceived) {
			totalLoansReceived += loan.getLoanValue();
		}
		return totalLoansReceived;
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

	public ArrayList<Account> getAllAccounts() {
		return accounts;
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
	public String showLoansOffered(Date currentDate) {
		String loanList = "";
		for (Loan loan : currentLoansOffered) {
			loanList += "\n" + loan.displayLenderDetails(currentDate);
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
	public String showLoansReceived(Date currentDate) {
		String loanList = "";
		for (Loan loan : currentLoansReceived) {
			loanList += "\n" + loan.displayBorrowerDetails(currentDate);
		}
		return loanList;
	}

	// retrieves a loan that the customer has taken out
	public Loan getBorrowedLoan(String loanID) {
		for (Loan loan : currentLoansReceived) {
			if (loan.getLoanID().equals(loanID)) {
				return loan;
			}
		}
		return null;
	}

	// retrieves a loan that the customer has lent
	public Loan getLentLoan(String loanID) {
		for (Loan loan : currentLoansOffered) {
			if (loan.getLoanID().equals(loanID)) {
				return loan;
			}
		}
		return null;
	}

	// removes a loan from the customer's account
	public void removeLoan(String loanID) {
		for (int index = 0; index < currentLoansOffered.size(); index++) {
			if (currentLoansOffered.get(index).getLoanID().equals(loanID)) {
				currentLoansOffered.remove(index);
			}
		}
		for (int index = 0; index < currentLoansReceived.size(); index++) {
			if (currentLoansReceived.get(index).getLoanID().equals(loanID)) {
				currentLoansReceived.remove(index);
			}
		}
	}

}
