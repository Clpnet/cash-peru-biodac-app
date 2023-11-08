package com.acj.client.prosegur.views;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.acj.client.prosegur.R;
import com.acj.client.prosegur.model.constant.OrderStateEnum;
import com.acj.client.prosegur.model.dto.orders.OrderDTO;
import com.acj.client.prosegur.views.config.OrderCustomAdapter;
import com.acj.client.prosegur.model.constant.EnumExtra;
import com.acj.client.prosegur.views.adapter.CapturaHuellaActivity;

import java.util.List;

public class OrderFragment extends Fragment implements OrderCustomAdapter.OnItemClickListener {

    private final String LOG_TAG = OrderFragment.class.getSimpleName();

    private RecyclerView recyclerView;
    private Context context;

    private List<OrderDTO> orders;

    private Boolean isClicked;

    public OrderFragment(Context context, List<OrderDTO> orders) {
        this.context = context;
        this.orders = orders;
    }

    public void refreshContent(List<OrderDTO> newOrders) {
        this.orders = newOrders;
        this.recyclerView.setAdapter(new OrderCustomAdapter(orders, context, this));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_order, container, false);

        recyclerView = view.findViewById(R.id.recycler_view);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        OrderCustomAdapter orderCustomAdapter = new OrderCustomAdapter(orders, context, this);

        recyclerView.setAdapter(orderCustomAdapter);

        return view;

    }

    @Override
    public void onItemClick(int position) {
        OrderDTO currentOrder = orders.get(position);

        Log.i(LOG_TAG, "Validando proceso de captura para la orden [" + currentOrder.getCodigoOrden() + "]");

        if (OrderStateEnum.Constants.PENDING_CODE.equals(currentOrder.getEstadoEntrega().getCode()) ||
            (OrderStateEnum.Constants.NO_HIT_CODE.equals(currentOrder.getEstadoEntrega().getCode()) &&
                currentOrder.getOrdenesIntento().size() < 2)) {
            Log.i(LOG_TAG, "Inicando proceso de captura");

            if (!isClicked) {
                Intent intent = new Intent(getContext(), CapturaHuellaActivity.class);
                intent.putExtra(EnumExtra.CURRENT_ORDER.toString(), currentOrder);
                startActivity(intent);

                isClicked = Boolean.TRUE;
            }
        } else {
            Log.i(LOG_TAG, "Orden no apta para iniciar el proceso de captura.");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i("STATUS: ", "ON RESUME");

        isClicked = Boolean.FALSE;
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.i("STATUS: ", "ON PAUSE");
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.i("STATUS: ", "ON START");
    }

}