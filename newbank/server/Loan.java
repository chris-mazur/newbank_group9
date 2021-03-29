package newbank.server;

import java.util.Calendar;
import java.util.Date;
import java.lang.Math;

public class Loan {

    private static int ID = 1;
    private String loanID;
    private Account lendingAccount;
    private double principalAmount;
    private int remainingDuration; // weeks
    private double interestRate;
    private double repaymentAmount;
    private double totalInterestAccrued;
    private Date repaymentDeadline; // TODO - have some kind of penalty for missing the repayment deadline?
    private Date lastUpdated;

    // a customer who wishes to lend money can set up a loan
    public Loan(Account lendingAccount, double principalAmount, int duration) {
        // take input information
        this.lendingAccount = lendingAccount;
        this.principalAmount = principalAmount;
        this.repaymentAmount = principalAmount;
        this.remainingDuration = duration;
        // set a unique ID for the loan
        loanID = "Loan" + ID;
        ID++;
        // The annual rate of interest (in decimal) is chosen based on the loan duration (in weeks)
        // TODO - make it possible for the bank to change the interest rates (probably take out of this class)
        if (duration < 4) {
            interestRate = 0.05;
        } else if (duration < 12) {
            interestRate = 0.04;
        } else {
            interestRate = 0.03;
        }
        // commit funds to the loan
        this.lendingAccount.withdrawFunds(principalAmount);
    }

    // display details about the loan
    public String displayDetails() {
        return loanID + ": " + principalAmount + " at " + (interestRate * 100) + "% for " + remainingDuration +
                " weeks.";
    }

    // display status of the loan for the lender
    public String displayLenderDetails() {
        return loanID + ": " + principalAmount + " lent, " + totalInterestAccrued + " interest earned.";
    }

    // display status of the loan for the borrower
    public String displayBorrowerDetails() {
        return loanID + ": " + principalAmount + " borrowed, " + repaymentAmount + " left to pay.";
    }

    // return the loanID
    public String getLoanID() {
        return loanID;
    }

    // return the loan value
    public double getLoanValue() {
        return principalAmount;
    }

    // a customer who wishes to borrow money can accept a loan
    public void acceptLoan(Account borrowingAccount, Date startDate) {
        // transfer loan to account
        borrowingAccount.depositFunds(principalAmount);
        // set the repayment deadline
        Calendar repaymentCalendar = Calendar.getInstance();
        repaymentCalendar.setTime(startDate);
        lastUpdated = repaymentCalendar.getTime();
        repaymentCalendar.add(repaymentCalendar.DATE, (remainingDuration * 7));
        repaymentDeadline = repaymentCalendar.getTime();
    }

    // update the outstanding balance on the loan
    public double getRepaymentAmount(Date currentDate) {
        double previousRepaymentAmount = repaymentAmount;
        // determine the period (in days) since the repayment amount was last calculated
        double interestPeriod = Math.floor((currentDate.getTime() - lastUpdated.getTime()) / (1000 * 60 * 60 * 24));
        // calculate a new repayment amount that includes the interest since the previous calculation
        repaymentAmount *= Math.pow((1 + (interestRate/365.24)), interestPeriod);
        System.out.println("New Repayment Amount = " + repaymentAmount);
        totalInterestAccrued += repaymentAmount - previousRepaymentAmount;
        lastUpdated = currentDate;
        return repaymentAmount;
    }

    // make a repayment on the loan
    public void makeRepayment(Date currentDate, double repayment, Account repaymentAccount) {
        repaymentAccount.withdrawFunds(repayment);
        lendingAccount.depositFunds(repayment);
        repaymentAmount -= repayment;
        lastUpdated = currentDate;
    }

}
