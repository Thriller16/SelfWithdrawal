package com.example.lawrene.selfwithdrawal;

/**
 * Created by lawrene on 7/13/18.
 */

public class Transaction {
    String phone_number, amount, balance, confirmation_id;

    public Transaction(String phone_number, String amount, String confirmation_id, String balance) {
        this.phone_number = phone_number;
        this.amount = amount;
        this.balance = balance;
        this.confirmation_id = confirmation_id;
    }

    public String getPhone_number() {
        return phone_number;
    }

    public String getAmount() {
        return amount;
    }

    public String getBalance() {
        return balance;
    }

    public String getConfirmation_id() {
        return confirmation_id;
    }
}
