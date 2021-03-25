package newbank.server;

import java.util.ArrayList;

public class Customer {
	
	private ArrayList<Account> accounts;
	private String password;
	
	public Customer(String password) {
		accounts = new ArrayList<>();
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

	// return a requested customer account if it exists (or null if not)
	public Account getAccount(String accountName) {
		for(Account account : accounts) {
			if(account.getName().equals(accountName)) {
				return account;
			}
		}
		return null;
	}


}
