package newbank.server;

import java.util.Calendar;
import java.util.Date;
import java.lang.Math;

public class Loan {

    private static int ID = 1;
    private final String loanID;
    private Account lendingAccount = null; // account to send loan repayments to
    private Account borrowingAccount = null; // account to deposit loan into
    private final double principalAmount;
    private final int loanDuration; // weeks
    private int remainingDuration; // weeks
    private final double interestRate; // decimal
    private double repaymentAmount;
    private double totalInterestAccrued;
    private boolean fundsCommitted = false;
    private boolean loanActive;
    private Date repaymentDeadline;
    private Date lastUpdated;

    // a loan can be set up by a customer who wants to lend money, or a customer who wants to borrow money
    public Loan(double principalAmount, double interestRate, int duration, Date setupDate) {
        this.principalAmount = principalAmount;
        this.repaymentAmount = principalAmount;
        this.interestRate = interestRate;
        this.loanDuration = duration;
        this.remainingDuration = duration;
        this.lastUpdated = setupDate;
        this.loanActive = false;
        loanID = "Loan" + ID;
        ID++;
    }

    // assigns a lending account to commit funds to the loan and/or receive loan repayments
    public void setLendingAccount(Account lendingAccount, Date startDate) {
        this.lendingAccount = lendingAccount;
        if (!fundsCommitted) {
            this.lendingAccount.withdrawFunds(principalAmount);
            fundsCommitted = true;
        }
        // if there is already a borrowing account assigned, the funds are deposited and the loan starts
        if (borrowingAccount != null) {
            startLoan(borrowingAccount, startDate);
        }
    }

    // assigns a borrowing account to receive the loan payment
    public void setBorrowingAccount(Account borrowingAccount, Date startDate) {
        this.borrowingAccount = borrowingAccount;
        // if there is already a lending account assigned, the funds are deposited and the loan starts
        if (lendingAccount != null) {
            startLoan(this.borrowingAccount, startDate);
        }
    }

    // rounds a number to two decimal places
    private double currencyNum(double number) {
        return Math.round(number * 100) / 100.0;
    }

    // display details about the loan
    public String displayDetails() {
        return loanID + ": " + principalAmount + " at " + (interestRate * 100) + "% for " + remainingDuration +
                " weeks.";
    }

    // display status of the loan for the lender
    public String displayLenderDetails(Date currentDate) {
        refreshLoanData(currentDate); // required to refresh interest accrued value
        return loanID + ": " + principalAmount + " lent, " + currencyNum(totalInterestAccrued) + " interest earned.";
    }

    // display status of the loan for the borrower
    public String displayBorrowerDetails(Date currentDate) {
        refreshLoanData(currentDate);
        return loanID + ": " + principalAmount + " borrowed, " + currencyNum(repaymentAmount) + " left to pay.";
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
    private void startLoan(Account borrowingAccount, Date startDate) {
        borrowingAccount.depositFunds(principalAmount);
        setRepaymentDeadline(startDate);
        lastUpdated = startDate;
        loanActive = true;
    }

    // set or update the repayment deadline for the loan
    private void setRepaymentDeadline(Date startDate) {
        Calendar repaymentCalendar = Calendar.getInstance();
        repaymentCalendar.setTime(startDate);
        repaymentCalendar.add(Calendar.DATE, (loanDuration * 7));
        repaymentDeadline = repaymentCalendar.getTime();
    }

    // add interest accrued over a specified period to the repayment amount
    private void calculateInterest(Date startDate, Date endDate) {
        long timeElapsed = endDate.getTime() - startDate.getTime(); // milliseconds
        int interestPeriod = (int) timeElapsed / (1000 * 60 * 60 * 24); // days
        repaymentAmount *= Math.pow((1 + (interestRate/365.24)), interestPeriod);
    }

    // update the outstanding balance on the loan, the time remaining, and the total interest accrued
    private void refreshLoanData(Date currentDate) {
        if (loanActive) {
            double previousRepaymentAmount = repaymentAmount;
            // apply a late repayment penalty if required
            while (currentDate.after(repaymentDeadline)) {
                // add interest accrued up to the repayment deadline
                calculateInterest(lastUpdated, repaymentDeadline);
                // apply late repayment penalty
                repaymentAmount += principalAmount * interestRate;
                // update repayment deadline (the extension provided is equal to the original loan duration)
                lastUpdated = repaymentDeadline;
                setRepaymentDeadline(repaymentDeadline);
            }
            // add interest accrued up to the current date
            calculateInterest(lastUpdated, currentDate);
            // update loan information
            lastUpdated = currentDate;
            remainingDuration = (int) (repaymentDeadline.getTime() - currentDate.getTime()) / (1000 * 60 * 60 * 24 * 7);
            totalInterestAccrued += repaymentAmount - previousRepaymentAmount;
        }
    }

    // return the outstanding balance on the loan
    public double getRepaymentAmount(Date currentDate) {
        refreshLoanData(currentDate);
        return currencyNum(repaymentAmount);
    }

    // make a repayment on the loan
    public void makeRepayment(Date currentDate, double repayment, Account repaymentAccount) {
        repaymentAccount.withdrawFunds(repayment);
        lendingAccount.depositFunds(repayment);
        repaymentAmount -= repayment;
        if (currencyNum(repaymentAmount) == 0) {
            loanActive = false; // the loan has been repaid in full and can be deleted
        }
        lastUpdated = currentDate;
    }

}
