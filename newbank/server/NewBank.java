package newbank.server;

import java.util.HashMap;
import java.util.regex.*;
import java.lang.Double;
import java.lang.Integer;
import java.util.Calendar;
import java.util.Date;

public class NewBank {

	// bank instance
	private static final NewBank bank = new NewBank();

	// parameters set by the bank
	private static final int lenderLoanLimit = 3; // limits the number of loans a lender can create
	private static final int borrowerLoanLimit = 3; // limits the number of loans a borrower can accept
	private static final int borrowerLoanSizeLimit = 4; // limits the size of a loan relative to borrower funds

	// data structures for bank
	private HashMap<String,Customer> customers; // place to store all customer data
	private Calendar calendar = Calendar.getInstance(); // for time-dependent operations (e.g. interest)
	private HashMap<String, Loan> loanMarketPlace; // place to store loans before people take them

	private NewBank() {
		customers = new HashMap<>();
		loanMarketPlace = new HashMap<>();
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
					return showMyAccounts(customer); // this should also show money lent and borrowed
				case "NEWACCOUNT":
					return newAccount(customer, requestParams);
				case "DEPOSIT":
					return depositFunds(customer, requestParams);
				case "MOVE":
					return transferFunds(customer, requestParams);
				case "PAY":
					return makePayment(customer, requestParams);
				case "LEND":
					return lendMoney(customer, requestParams, calendar.getTime());
				case "LOANS":
					return showLoans();
				case "BORROW":
					return borrowMoney(customer, requestParams); // loanID
				case "REPAY":
					return loanRepayment(customer, requestParams); // loanID, repayment amount
				case "TIMETRAVEL": // for testing purposes
					return timeTravel(requestParams);
				case "LOGOUT":
					return "LOGOUT";
				default:
					return "FAIL"; // TODO - should we rewrite this to 'Not a valid command, type in "HELP"...'?
			}
		}
		return "FAIL";
	}

	private String showMyAccounts(CustomerID customer) {
		// TODO - modify to also display lending and borrowing
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
				"TIMETRAVEL - Skips ahead to a future date; enter the command followed by a number of days.\n" +
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

	// set up a loan and add it to the loans marketplace
	private String lendMoney(CustomerID customerID, String[] requestParams, Date setupDate) {
		Customer customer = customers.get(customerID.getKey());
		// confirm that the parameters entered are valid, and provide prompts to the user if not
		String inputErrorPrompts = "";
		double lendingAmount = 0;
		int lendingDuration = 0;
		boolean inputParametersValid = true;
		if (requestParams.length == 4) {
			try {
				lendingAmount = Double.parseDouble(requestParams[1]);
				if (lendingAmount <= 0) {
					// a lending amount must be positive
					inputErrorPrompts += "Lending amount '" + requestParams[1] + "' is not valid.\n";
					inputParametersValid = false;
				}
			} catch (NumberFormatException e) {
				inputErrorPrompts += "Lending amount '" + requestParams[1] + "' is not valid.\n";
				inputParametersValid = false;
			}
			Account lendingAccount = customer.getAccount(requestParams[2]);
			if (lendingAccount == null) {
				inputErrorPrompts += "Account to lend from '" + requestParams[2] + "' does not exist.\n";
				inputParametersValid = false;
			} else if (lendingAccount.getBalance() < lendingAmount) {
				inputErrorPrompts += "Insufficient funds in " + lendingAccount.toString() + "\n";
				inputParametersValid = false;
			}
			try {
				lendingDuration = Integer.parseInt(requestParams[3]);
				if (lendingDuration <= 0) {
					// a lending duration must be positive
					inputErrorPrompts += "Lending duration '" + requestParams[3] + "' is not valid.\n";
					inputParametersValid = false;
				}
			} catch (NumberFormatException e) {
				inputErrorPrompts += "Lending duration '" + requestParams[3] + "' is not valid.\n";
				inputParametersValid = false;
			}
			if (inputParametersValid) {
				if (customer.numLoansOffered() < lenderLoanLimit) {
					// create a new loan
					Loan newLoan = new Loan(lendingAccount, lendingAmount, lendingDuration);
					// add loan to customer account
					customer.offerLoan(newLoan);
					// add loan to marketplace
					loanMarketPlace.put(newLoan.getLoanID(), newLoan);
					// confirm that loan has been set up
					return "The following loan has been set up:\n" + newLoan.displayDetails();
				} else {
					inputErrorPrompts += "The maximum number of loans you can offer is " + lenderLoanLimit + ". " +
							"Your current loans are:\n" + customer.showLoansOffered();
				}
			}
			return "Loan could not be set up:\n" + inputErrorPrompts;
		}
		return "Invalid entry. Try LEND <amount to lend> <account to lend from> <duration to lend for (weeks)>";
	}

	// shows all loans available at the bank
	private String showLoans() {
		String loanList = "";
		for (Loan loan : loanMarketPlace.values()) {
			loanList += loan.displayDetails() + "\n";
		}
		if (loanList.length() == 0) {
			return "No loans currently available.";
		} else {
			return loanList;
		}
	}

	// allows a customer to take out a loan
	private String borrowMoney(CustomerID customerID, String[] requestParams) {
		Customer customer = customers.get(customerID.getKey());
		// confirm that the parameters are valid, and provide prompts to the user if not
		String userPrompts = "";
		boolean inputParametersValid = true;
		if (requestParams.length == 3) {
			if (!loanMarketPlace.containsKey(requestParams[1])) {
				userPrompts += "Loan ID '" + requestParams[1] + "' is not valid.\n";
				inputParametersValid = false;
			}
			Account borrowingAccount = customer.getAccount(requestParams[2]);
			if (borrowingAccount == null) {
				userPrompts += "Account to pay loan into '" + requestParams[2] + "' does not exist.\n";
				inputParametersValid = false;
			}
			if (inputParametersValid) {
				// perform eligibility checks for the loan, and provide prompts to the user if criteria are not met
				Loan loan = loanMarketPlace.get(requestParams[1]);
				boolean eligibleForLoan = true;
				// perform eligibility checks
				if (loan.getLoanValue() > (customer.getTotalFunds() * borrowerLoanSizeLimit)) {
					userPrompts += "The maximum size of loan you are eligible for is " +
							(borrowerLoanSizeLimit * customer.getTotalFunds()) + " (" +
							borrowerLoanSizeLimit + " times the total amount of money held in your accounts).\n";
					eligibleForLoan = false;
				}
				if (customer.numLoansReceived() == borrowerLoanLimit) {
					userPrompts += "The maximum number of loans you can get is " + borrowerLoanLimit + ". " +
							"Your current loans are:\n" + customer.showLoansReceived();
					eligibleForLoan = false;
				}
				if (eligibleForLoan) {
					// accept loan and transfer funds to the borrowing account
					loan.acceptLoan(borrowingAccount, calendar.getTime());
					// add loan to customer account
					customer.receiveLoan(loan);
					// remove loan from marketplace
					loanMarketPlace.remove(loan.getLoanID());
					// confirm that loan has been successfully received
					return "The following loan has been received:\n" + loan.displayDetails();
				} else {
					return "You are not eligible for this loan:\n" + userPrompts;
				}
			}
			return "Unable to take out loan:\n" + userPrompts;
		}
		return "Invalid entry. Try BORROW <loan ID> <account to pay into>";
	}

	// allows a customer to make a repayment on their loan
	private String loanRepayment(CustomerID customerID, String[] requestParams) {
		Customer customer = customers.get(customerID.getKey());
		// confirm that the parameters entered are valid, and provide prompts to the user if not
		String userPrompts = "";
		double repaymentAmount = 0;
		boolean inputParametersValid = true;
		if (requestParams.length == 4) {
			Loan loanToRepay = customer.getLoan(requestParams[1]);
			if (loanToRepay == null) {
				userPrompts += "\nLoan ID '" + requestParams[1] + "' is not valid.";
				inputParametersValid = false;
			}
			try {
				repaymentAmount = Double.parseDouble(requestParams[2]);
				if (repaymentAmount <= 0) {
					userPrompts += "\nRepayment amount '" + requestParams[2] + "' must be positive.";
					inputParametersValid = false;
				}
			} catch (NumberFormatException e) {
				userPrompts += "\nRepayment amount '" + requestParams[2] + "' is not valid.";
				inputParametersValid = false;
			}
			Account repaymentAccount = customer.getAccount(requestParams[3]);
			if (repaymentAccount == null) {
				userPrompts += "\nAccount to repay from '" + requestParams[3] + "' does not exist.";
				inputParametersValid = false;
			}
			if (inputParametersValid) {
				// check that there are sufficient funds to make the repayment
				if (repaymentAccount.getBalance() < repaymentAmount) {
					userPrompts += "\nInsufficient funds to make repayment from " + repaymentAccount.toString();
					inputParametersValid = false;
				}
				// check that the repayment does not exceed the remaining balance on the loan
				double outstandingBalance = loanToRepay.getRepaymentAmount(calendar.getTime());
				if (repaymentAmount > outstandingBalance) {
					userPrompts += "\nRepayment exceeds outstanding balance on loan: " + outstandingBalance;
					inputParametersValid = false;
				}
				if (inputParametersValid) {
					// make the repayment
					loanToRepay.makeRepayment(calendar.getTime(), repaymentAmount, repaymentAccount);
					outstandingBalance = loanToRepay.getRepaymentAmount(calendar.getTime());
					return "Repayment of " + repaymentAmount + " made to " + loanToRepay.getLoanID() + ". " +
							"Outstanding balance is now " + outstandingBalance;
				}
				return "Unable to make repayment on loan:" + userPrompts;
			}
			return "Unable to make repayment on loan:" + userPrompts;
		}
		return "Invalid entry. Try REPAY <Loan ID> <amount to repay> <account to pay from>";
	}

	// skips ahead by a specified number of days to a future date in the bank's calendar
	private String timeTravel(String[] requestParams) {
		// confirm that the parameter entered is valid (must be a positive integer)
		if(requestParams.length == 2 && isNumeric(requestParams[1])) {
			try {
				int days = Integer.parseInt(requestParams[1]);
				if(days >= 0) {
					// update the calendar date and provide confirmation of time travel
					Date departureDate = calendar.getTime();
					calendar.add(calendar.DATE, days);
					Date arrivalDate = calendar.getTime();
					return "Travelled forward " + days + " days from " + departureDate + " to " + arrivalDate;

				} else {
					return "Invalid entry. The number of days must be positive.";
				}
			} catch (NumberFormatException e) {
				return "Invalid entry. The number of days must be an integer.";
			}
		}
		return "Invalid entry. Try TIMETRAVEL <number of days into the future>";
	}

}
