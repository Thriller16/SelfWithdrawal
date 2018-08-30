package com.example.lawrene.selfwithdrawal;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by lawrene on 7/13/18.
 */

public class TransactionAdapter extends ArrayAdapter<Transaction> {
    public TransactionAdapter(@NonNull Context context, @NonNull List<Transaction> objects) {
        super(context, 0, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if(convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item, parent, false);
        }

        Transaction transaction = getItem(position);

        TextView phone = convertView.findViewById(R.id.phoneT);
        TextView amount = convertView.findViewById(R.id.amountT);
        TextView conf = convertView.findViewById(R.id.confT);
        TextView bal = convertView.findViewById(R.id.balanceT);


        phone.setText(transaction.getPhone_number());
        amount.setText(transaction.getAmount());
        conf.setText(transaction.getConfirmation_id());
        bal.setText(transaction.getBalance());

        return convertView;
    }
}
