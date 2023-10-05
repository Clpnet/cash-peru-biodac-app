package com.acj.client.appprosegur;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.acj.client.appprosegur.api.ApiService.OrderService;
import com.acj.client.appprosegur.api.ApiUtils;
import com.acj.client.appprosegur.api.model.OrderResponse;
import com.acj.client.appprosegur.api.model.constant.StatusResponseEnum;
import com.acj.client.appprosegur.api.model.dto.OrderDTO;
import com.acj.client.appprosegur.config.OrderCustomAdapter;
import com.acj.client.appprosegur.functions.SessionConfig;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderFragment extends Fragment implements OrderCustomAdapter.OnItemClickListener {

    private RecyclerView recyclerView;
    private Context context;

    private List<OrderDTO> orders;

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
        /*Intent intent = new Intent(getContext(), CapturaHuellaActivity.class);
        intent.putExtra("clave", String.valueOf(position));
        startActivity(intent);*/

        System.out.println("Has hecho clic en el elemento " + position);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i("STATUS: ", "ON RESUME");
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