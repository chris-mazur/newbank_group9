package newbank.server;

import java.util.HashMap;
import java.util.regex.*;
import java.lang.Double;

public class NewBank {
	
	private static final NewBank bank = new NewBank();
	private HashMap<String,Customer> customers;

	private NewBank() {
		customers = new HashMap<>();
		addTestData();
	}
	
	private void addTestData() {
		Customer bhagy = new Customer();
		bhagy.addAccount(new Account("Main", 1000.0));
		bhagy.setPassword("test1234");
		customers.put("Bhagy", bhagy);
		
		Customer christina = new Customer();
		christina.addAccount(new Account("Savings", 1500.0));
		christina.setPassword("test5678");
		customers.put("Christina", christina);
		
		Customer john = new Customer();
		john.addAccount(new Account("Checking", 250.0));
		john.setPassword("test9999");
		customers.put("John", john);
	}
	
	public static NewBank getBank() {
		return bank;
	}
	
	public synchronized CustomerID checkLogInDetails(String userName, String password) {
		if(customers.containsKey(userName) && (customers.get(userName).getPassword().equals(password))) {
			return new CustomerID(userName);
		}
		return null;
	}

	// commands from the NewBank customer are processed in this method
	public synchronized String processRequest(CustomerID customer, String request) {

		String[] requestParams = request.split("\\s+");

		if(customers.containsKey(customer.getKey())) {
			switch(requestParams[0]) {
				case "HELP":
					return showHelp();
				case "SHOWMYACCOUNTS":
					return showMyAccounts(customer);
				case "NEWACCOUNT":
					return newAccount(customer, requestParams);
				case "DEPOSIT":
					return depositFunds(customer, requestParams);
				case "MOVE":
					return transferFunds(customer, requestParams);
				case "PAY":
					return makePayment(customer, requestParams);
				case "LOGOUT":
					return "LOGOUT";
				default:
					return "FAIL";
			}
		}
		return "FAIL";
	}

	private String showMyAccounts(CustomerID customer) {
		return (customers.get(customer.getKey())).accountsToString();
	}

	private String newAccount(CustomerID customer, String[] requestParams) {
		if(requestParams.length == 2){
			if(isNumeric(requestParams[1])) {
				return "Account name cannot be a number. Try again";
			} else {
				String accountName = requestParams[1];
				customers.get(customer.getKey()).addAccount(new Account(accountName,0.00));
				return "Account created: " + accountName;
			}
		}
		return "Invalid entry. Try NEWACCOUNT <account name>";
	}

	//method to check if a String is Numeric. Useful when checking user input
	private boolean isNumeric(String string) {
		String regex = "-?\\d+(\\.\\d+)?";
		return Pattern.matches(regex, string);
	}

	// provides the user with an overview of all commands for interacting with the client
	private String showHelp() {
		// working draft to outline all possible commands (please update when necessary)
		return "Welcome to NewBank! Here is a list of commands you can use:\n" +
				"SHOWMYACCOUNTS - Displays a list of all bank accounts you currently have.\n" +
				"NEWACCOUNT - Creates a new bank account; enter the command followed by the name " +
				"you would like to give to the account.\n" +
				"DEPOSIT - Adds funds to one of your accounts; enter the command followed by the balance to be " +
				"added, then the account name to deposit funds to.\n" +
				"MOVE - Moves funds between your accounts; enter the command followed by the balance to " +
				"be transferred, the account name to withdraw from, and the account name to deposit to.\n" +
				"PAY - Make a payment to another bank account; enter the command followed by the payment amount, " +
				"account to pay from, name of the payee, and the account name of the payee.\n" +
				"LOGOUT - Logs you out from the NewBank command line application.";
	}

	// deposits money into a specified account
	private String depositFunds(CustomerID customer, String[] requestParams) {
		// confirm that the correct number of parameters have been input
		if(requestParams.length == 3) {
			// confirm that input parameters are valid, and provide prompts to the user if not
			String inputErrorPrompts = "";
			double depositAmount = 0;
			try {
				depositAmount = Double.parseDouble(requestParams[1]);
				if(depositAmount <= 0) {
					// a deposit amount must be positive
					inputErrorPrompts += "Deposit amount '" + requestParams[1] + "' is not valid.\n";
				}
			} catch (NumberFormatException e) {
				inputErrorPrompts += "Deposit amount '" + requestParams[1] + "' is not valid.\n";
			}
			Account depositAccount = customers.get(customer.getKey()).getAccount(requestParams[2]);
			if(depositAccount == null) {
				inputErrorPrompts += "Account for deposit '" + requestParams[2] + "' does not exist.\n";
			}
			if(inputErrorPrompts.length() > 0) {
				return "Deposit could not be made: \n" + inputErrorPrompts;
			}
			// deposit funds into the specified account
			depositAccount.depositFunds(depositAmount);
			return depositAmount + " deposited to " + depositAccount.getName() + "\n" + "Current balance in " +
					depositAccount.toString();
		} else {
			return "Invalid entry. Try DEPOSIT <amount> <account name>";
		}
	}

	// transfers money between two accounts belonging to the same customer
	private String transferFunds(CustomerID customer, String[] requestParams) {
		// confirm that the correct number of parameters have been input
		if(requestParams.length == 4) {
			// confirm that input parameters are valid, and provide prompts to the user if not
			String inputErrorPrompts = "";
			double transferAmount = 0;
			boolean checkBalance = true;
			try {
				transferAmount = Double.parseDouble(requestParams[1]);
				if(transferAmount <= 0) {
					// a transfer amount must be positive
					inputErrorPrompts += "Transfer amount '" + requestParams[1] + "' is not valid.\n";
					checkBalance = false;
				}
			} catch (NumberFormatException e) {
				inputErrorPrompts += "Transfer amount '" + requestParams[1] + "' is not valid.\n";
				checkBalance = false;
			}
			Account withdrawalAccount = customers.get(customer.getKey()).getAccount(requestParams[2]);
			if(withdrawalAccount == null) {
				inputErrorPrompts += "Account for withdrawal '" + requestParams[2] + "' does not exist.\n";
				checkBalance = false;
			}
			Account depositAccount = customers.get(customer.getKey()).getAccount(requestParams[3]);
			if(depositAccount == null) {
				inputErrorPrompts += "Account for deposit '" + requestParams[3] + "' does not exist.\n";
			}
			if(checkBalance && transferAmount > withdrawalAccount.getBalance()) {
				inputErrorPrompts += "Insufficient funds in " + withdrawalAccount.toString();
			}
			if(inputErrorPrompts.length() > 0) {
				return "Transfer could not be made: \n" + inputErrorPrompts;
			}
			// transfer funds between the specified accounts
			withdrawalAccount.withdrawFunds(transferAmount);
			depositAccount.depositFunds(transferAmount);
			return transferAmount + " transferred from " + withdrawalAccount.getName() + " to " +
					depositAccount.getName();
		}
		return "Invalid entry. Try MOVE <amount> <account to withraw from> <account to deposit to>";
	}

	// makes a payment to another customer in the same bank
	private String makePayment(CustomerID customer, String[] requestParams) {
		// confirm that the correct number of parameters have been input
		if(requestParams.length == 5) {
			// confirm that input parameters are valid, and provide prompts to the user if not
			String inputErrorPrompts = "";
			double paymentAmount = 0;
			boolean checkBalance = true;
			try {
				paymentAmount = Double.parseDouble(requestParams[1]);
				if(paymentAmount <= 0) {
					// a transfer amount must be positive
					inputErrorPrompts += "Payment amount '" + requestParams[1] + "' is not valid.\n";
					checkBalance = false;
				}
			} catch (NumberFormatException e) {
				inputErrorPrompts += "Payment amount '" + requestParams[1] + "' is not valid.\n";
				checkBalance = false;
			}
			Account withdrawalAccount = customers.get(customer.getKey()).getAccount(requestParams[2]);
			if(withdrawalAccount == null) {
				inputErrorPrompts += "Account for withdrawal '" + requestParams[2] + "' does not exist.\n";
				checkBalance = false;
			}
			Customer payee = customers.get(requestParams[3]);
			if(payee == null) {
				// This doesn't work as expected (returns an infinite loop of "null")
				// TODO - fixing the invalid username/password issue that is on Trello might solve this problem
				inputErrorPrompts += "Payee '" + requestParams[3] + "' does not exist.\n";
			}
			Account payeeAccount = payee.getAccount(requestParams[4]);
			if (payeeAccount == null) {
				inputErrorPrompts += "Payee account, '" + requestParams[4] + "' does not exist.\n";
			}
			if(checkBalance && paymentAmount > withdrawalAccount.getBalance()) {
				inputErrorPrompts += "Insufficient funds in " + withdrawalAccount.toString();
			}
			if(inputErrorPrompts.length() > 0) {
				return "Payment could not be made:\n" + inputErrorPrompts;
			}
			// make payment
			withdrawalAccount.withdrawFunds(paymentAmount);
			payeeAccount.depositFunds(paymentAmount);
			return "Payment of " + paymentAmount + " successfully made.\n" +
					"Remaining balance in " + withdrawalAccount.toString();
		}
		return "Invalid entry. Try PAY <amount> <account to pay from> <payee name> <payee account>";
	}

}
