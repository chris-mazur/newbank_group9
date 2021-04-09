package newbank.server;

import java.util.Calendar;
import java.util.Date;
import java.lang.Math;

public class Loan {

    private static int ID = 1;
    private final String loanID;
    private final Account lendingAccount;
    private final double principalAmount;
    private final int remainingDuration; // weeks
    private final double interestRate;
    private double repaymentAmount;
    private double totalInterestAccrued;
    private boolean loanActive;
    private Date repaymentDeadline; // TODO - have some kind of penalty for missing the repayment deadline?
    private Date lastUpdated;

    // a customer who wishes to lend money can set up a loan
    public Loan(Account lendingAccount, double principalAmount, double interestRate, int duration, Date setupDate) {
        // take input information
        this.lendingAccount = lendingAccount;
        this.principalAmount = principalAmount;
        this.repaymentAmount = principalAmount;
        this.interestRate = interestRate;
        this.remainingDuration = duration;
        // set a unique ID for the loan
        loanID = "Loan" + ID;
        ID++;
        // commit funds to the loan
        this.lendingAccount.withdrawFunds(principalAmount);
        this.lastUpdated = setupDate;
        this.loanActive = false;
    }

    // display details about the loan
    public String displayDetails() {
        return loanID + ": " + principalAmount + " at " + (interestRate * 100) + "% for " + remainingDuration +
                " weeks.";
    }

    // display status of the loan for the lender
    public String displayLenderDetails(Date currentDate) {
        refreshInterestCalculation(currentDate); // required to refresh interest accrued value
        return loanID + ": " + principalAmount + " lent, " + totalInterestAccrued + " interest earned.";
    }

    // display status of the loan for the borrower
    public String displayBorrowerDetails(Date currentDate) {
        refreshInterestCalculation(currentDate);
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
        repaymentCalendar.add(Calendar.DATE, (remainingDuration * 7));
        repaymentDeadline = repaymentCalendar.getTime();
        loanActive = true;
    }

    // update the outstanding balance on the loan
    private void refreshInterestCalculation(Date currentDate) {
        if (loanActive) {
            // determine the period (in days) since the repayment amount was last calculated
            long timeElapsed = currentDate.getTime() - lastUpdated.getTime(); // milliseconds
            int interestPeriod = (int) timeElapsed / (1000 * 60 * 60 * 24); // days
            // calculate a new repayment amount that includes the interest since the previous calculation
            double previousRepaymentAmount = repaymentAmount;
            repaymentAmount *= Math.pow((1 + (interestRate/365.24)), interestPeriod);
            System.out.println("New Repayment Amount = " + repaymentAmount);
            totalInterestAccrued += repaymentAmount - previousRepaymentAmount;
            lastUpdated = currentDate;
        }
    }

    // return the outstanding balance on the loan
    public double getRepaymentAmount(Date currentDate) {
        refreshInterestCalculation(currentDate);
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
