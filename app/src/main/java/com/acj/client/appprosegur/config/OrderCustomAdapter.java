package com.acj.client.appprosegur.config;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.acj.client.appprosegur.R;
import com.acj.client.appprosegur.api.model.dto.OrderDTO;
import com.google.android.material.card.MaterialCardView;
import com.acj.client.appprosegur.api.model.constant.OrderStateEnum;

import java.util.List;

public class OrderCustomAdapter extends RecyclerView.Adapter<OrderCustomAdapter.ViewHolder> {

		private List<OrderDTO> orderDTO;
		private OnItemClickListener listener;

		private Context context;

		public OrderCustomAdapter(List<OrderDTO> orderDTO,
															Context context,
															OnItemClickListener listener) {
				this.orderDTO = orderDTO;
				this.context = context;
				this.listener = listener;
		}

		@NonNull
		@Override
		public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
				View itemView = LayoutInflater.from(parent.getContext())
						.inflate(R.layout.cardview_order, parent, false);
				return new ViewHolder(itemView);
		}

		@Override
		public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
				OrderDTO orden = orderDTO.get(holder.getAdapterPosition());

				holder.txtOrderNumber.setText(orden.getOrderNumber());
				holder.txtCardType.setText(orden.getCardType());
				holder.txtDocumentNumber.setText(orden.getDocumentNumber().substring(0, 4) + "****");
				holder.txtNumberIntent.setText((orden.getNumberIntent() > 0)
						? String.format(context.getString(R.string.number_intent_desc), orden.getNumberIntent().toString())
						: "");
				holder.txtFirstDate.setText(orden.getFirstDate());
				holder.txtSecondDate.setText(orden.getSecondDate());

				holder.btnEstado.setBackgroundResource(getDrawableByState(orden.getOrderState()));
				holder.btnEstado.setText(orden.getOrderState().getDescription());

				holder.btnEstado.setTextColor((OrderStateEnum.Constants.PENDING_CODE.equals(orden.getOrderState().getCode()))
						? ContextCompat.getColor(context, R.color.black) : ContextCompat.getColor(context, R.color.white));

				holder.cardview.setOnClickListener(view -> listener.onItemClick(Integer.parseInt(orden.getDocumentNumber())));
		}

		@Override
		public int getItemCount() {
				return orderDTO.size();
		}


		public static class ViewHolder extends RecyclerView.ViewHolder {

				private TextView txtOrderNumber, txtCardType, txtDocumentNumber, txtNumberIntent, txtFirstDate, txtSecondDate;
				private Button btnEstado;
				private MaterialCardView cardview;

				public ViewHolder(View view) {
						super(view);

						txtOrderNumber = view.findViewById(R.id.txtOrderNumber);
						txtCardType = view.findViewById(R.id.txtCardDesc);
						txtDocumentNumber = view.findViewById(R.id.txtDocumentNumber);
						txtNumberIntent = view.findViewById(R.id.txtNumberIntent);
						txtFirstDate = view.findViewById(R.id.txtFirstDate);
						txtSecondDate = view.findViewById(R.id.txtSecondDate);
						cardview = view.findViewById(R.id.cdvOrder);
						btnEstado = view.findViewById(R.id.btnStatus);

				}
		}

		public interface OnItemClickListener {
				void onItemClick(int position);
		}

		private int getDrawableByState(OrderStateEnum stateEnum) {
				switch (stateEnum) {
						case HIT:
								return R.drawable.button_hit;
						case NO_HIT:
								return R.drawable.button_no_hit;
						default:
								return R.drawable.button_pending;
				}
		}

}
