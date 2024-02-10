package com.acj.client.prosegur.views.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.acj.client.prosegur.R;
import com.acj.client.prosegur.model.dto.orders.OrderDTO;
import com.acj.client.prosegur.util.Util;
import com.google.android.material.card.MaterialCardView;
import com.acj.client.prosegur.model.constant.OrderStateEnum;

import org.apache.commons.lang3.StringUtils;

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

		public void changeOrders(List<OrderDTO> orderDTO) {
				this.orderDTO = orderDTO;
				notifyDataSetChanged();
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

				holder.txtOrderNumber.setText(orden.getCodigoOperacion());
				holder.txtOrderType.setText(orden.getTipoOrden());
				holder.txtCardType.setText(orden.getTipoTarjeta());
				holder.txtDocumentNumber.setText(Util.obfuscateKeep(orden.getNumeroDocumento(), 4, Boolean.TRUE));
				holder.txtDeliveryDate.setText(orden.getFechaEntrega());

				showDynamicFields(holder, orden);

				OrderStateEnum orderState = orden.getEstadoEntrega();

				holder.btnEstado.setBackgroundResource(getDrawableByState(orderState));
				holder.btnEstado.setText(orderState.getDescription());

				holder.btnEstado.setTextColor((OrderStateEnum.Constants.PENDING_CODE.equals(orderState.getCode()))
						? ContextCompat.getColor(context, R.color.black) : ContextCompat.getColor(context, R.color.white));

				holder.cardview.setOnClickListener(view -> listener.onItemClick(position));
		}

		@Override
		public int getItemCount() {
				return orderDTO.size();
		}

		public static class ViewHolder extends RecyclerView.ViewHolder {

				private final TextView txtOrderNumber;
				private final TextView txtOrderType;
				private final TextView txtCardType;
				private final TextView txtDocumentNumber;
				private final TextView txtDeliveryDate;
				private final Button btnEstado;
				private final MaterialCardView cardview;

				// Campos dinamicos
				private final TextView txtNumberIntent;
				private final TextView lblLastIntentDate;
				private final TextView txtLastIntentDate;
				private final TextView txtValidatedState;

				public ViewHolder(View view) {
						super(view);

						txtOrderNumber = view.findViewById(R.id.txtOrderNumber);
						txtOrderType = view.findViewById(R.id.txtOrderType);
						txtCardType = view.findViewById(R.id.txtCardDesc);
						txtDocumentNumber = view.findViewById(R.id.txtDocumentNumber);
						txtDeliveryDate = view.findViewById(R.id.txtFechaEntrega);
						cardview = view.findViewById(R.id.cdvOrder);
						btnEstado = view.findViewById(R.id.btnStatus);

						txtNumberIntent = view.findViewById(R.id.txtNumberIntent);
						lblLastIntentDate = view.findViewById(R.id.lblUltimoIntento);
						txtLastIntentDate = view.findViewById(R.id.txtUltimoIntento);
						txtValidatedState = view.findViewById(R.id.txtEstadoValidado);

				}
		}

		public interface OnItemClickListener {
				void onItemClick(int position);
		}

		private int getDrawableByState(OrderStateEnum stateEnum) {
				switch (stateEnum) {
						case H:
								return R.drawable.button_hit;
						case N:
								return R.drawable.button_no_hit;
						default:
								return R.drawable.button_pending;
				}
		}

		private void showDynamicFields(ViewHolder holder, OrderDTO orden) {
				OrderStateEnum currentState = orden.getEstadoEntrega();

				holder.txtNumberIntent.setVisibility(View.GONE);
				holder.lblLastIntentDate.setVisibility(View.GONE);
				holder.txtLastIntentDate.setVisibility(View.GONE);
				holder.txtValidatedState.setVisibility(View.GONE);

			if (OrderStateEnum.Constants.HIT_CODE.equals(currentState.getCode())) {
					holder.txtValidatedState.setVisibility(View.VISIBLE);
			} else if (OrderStateEnum.Constants.NO_HIT_CODE.equals(currentState.getCode())) {
					int numberIntent = orden.getOrdenesIntento().size();

					holder.txtLastIntentDate.setText((numberIntent != 0)
							? orden.getOrdenesIntento().get(0).getFechaCreacion()
							: StringUtils.EMPTY);
					holder.lblLastIntentDate.setVisibility((numberIntent != 0) ? View.VISIBLE : View.GONE);
					holder.txtLastIntentDate.setVisibility((numberIntent != 0) ? View.VISIBLE : View.GONE);

					holder.txtNumberIntent.setText(String.format(context.getString(R.string.txt_card_number_intent_desc), String.valueOf(numberIntent)));

					holder.txtNumberIntent.setVisibility(View.VISIBLE);
			} else {
					int numberIntent = orden.getOrdenesIntento().size();

					holder.txtNumberIntent.setText((numberIntent > 0)
							? String.format(context.getString(R.string.txt_card_number_intent_desc), String.valueOf(numberIntent))
							: StringUtils.EMPTY);

					/* holder.txtNumberIntent.setVisibility((orden.getOrdenesIntento().isEmpty())
							? View.GONE : View.VISIBLE); */
			}
		}

}
