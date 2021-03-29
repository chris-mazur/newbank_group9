package newbank.server;

import java.util.Calendar;
import java.util.Date;

public class Loan {

    private static int ID = 1;
    private String loanID;
    private Account lendingAccount;
    private Account borrowingAccount;
    private double principalAmount;
    private int remainingDuration; // weeks
    private double interestRate;
    private double repaymentAmount;
    private Date repaymentDeadline;
    private boolean loanActive;
    // add a data structure to store record of repayments to use for interest calculations

    // a customer who wishes to lend money can set up a loan
    public Loan(Account lendingAccount, double principalAmount, Date setupDate, int duration) {
        // take input information
        this.lendingAccount = lendingAccount;
        this.principalAmount = principalAmount;
        this.remainingDuration = duration;
        // set a unique ID for the loan
        loanID = "Loan" + ID;
        ID++;
        // The interest rate is determined by the duration in weeks - TODO make it possible for the bank to set these
        if (duration < 4) {
            interestRate = 5.0;
        } else if (duration < 12) {
            interestRate = 4.0;
        } else {
            interestRate = 3.0;
        }
        // set the repayment deadline
        Calendar repaymentCalendar = Calendar.getInstance();
        repaymentCalendar.setTime(setupDate);
        repaymentCalendar.add(repaymentCalendar.DATE, (duration * 7));
        repaymentDeadline = repaymentCalendar.getTime();
        // commit funds to the loan
        this.lendingAccount.withdrawFunds(principalAmount);
        this.loanActive = false;
    }

    // display details about the loan
    public String displayDetails() {
        return loanID + ": " + principalAmount + " at " + interestRate + "% for " + remainingDuration + " weeks.";
    }

    // return the loanID
    public String getLoanID() {
        return loanID;
    }

    // display status of the loan for the lender

    // display status of the loan for the borrower

    // a customer who wishes to borrow money can accept a loan
    public void acceptLoan(Account borrowingAccount) {
        this.borrowingAccount = borrowingAccount;
        loanActive = true;
        // transfer loan to account
        borrowingAccount.depositFunds(principalAmount);
    }


    // borrower can make a loan repayment

    // lender can withdraw funds after the repayment deadline (or before if nobody has activated the loan)


}
