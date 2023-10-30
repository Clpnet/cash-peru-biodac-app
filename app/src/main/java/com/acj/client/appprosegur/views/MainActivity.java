package com.acj.client.appprosegur.views;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.acj.client.appprosegur.R;
import com.acj.client.appprosegur.api.ApiService.OrderService;
import com.acj.client.appprosegur.api.ApiUtils;
import com.acj.client.appprosegur.api.model.OrderResponse;
import com.acj.client.appprosegur.api.model.constant.OrderStateEnum;
import com.acj.client.appprosegur.api.model.constant.StatusResponseEnum;
import com.acj.client.appprosegur.api.model.dto.OrderDTO;
import com.acj.client.appprosegur.databinding.ActivityMainBinding;
import com.acj.client.appprosegur.functions.SessionConfig;
import com.acj.client.appprosegur.views.login.LoginActivity;
import com.google.android.material.appbar.AppBarLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

		private final String LOG_TAG = "MAIN-ACTIVITY";
		private final String FRAGMENT_TAG = "ORDER-FRAGMENT";

		private ActivityMainBinding binding;

		private ImageView menuIcon;

		private TextView txtCurrentState;
		private EditText etxSearchBox;

		private PopupMenu orderMenu;
		private MenuItem pendingOption;
		private MenuItem hitOption;
		private MenuItem noHitOption;

		private Context context;

		private Boolean isDataLoaded;

		@Override
		protected void onCreate(Bundle savedInstanceState) {
				super.onCreate(savedInstanceState);

				binding = ActivityMainBinding.inflate(getLayoutInflater());
				setContentView(R.layout.activity_main);

				AppBarLayout appBarLayout = findViewById(R.id.appBarLayout);
				appBarLayout.setExpanded(true);

				Toolbar toolbar = findViewById(R.id.toolbar);

				setSupportActionBar(toolbar);
				Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);

				menuIcon = toolbar.findViewById(R.id.overflow_icon);

				txtCurrentState = findViewById(R.id.txtOrderStates);
				etxSearchBox = findViewById(R.id.searchEditText);

				context = this;

				// Render content on fragment
				OrderFragment orderFragment = new OrderFragment(context, new ArrayList<>());
				getSupportFragmentManager()
						.beginTransaction().replace(R.id.container, orderFragment, FRAGMENT_TAG).commit();

				// Agregar el menú desplegable
				menuIcon.setOnClickListener(view -> {
						if (Objects.isNull(orderMenu)) {
								orderMenu = new PopupMenu(MainActivity.this, view);
								orderMenu.getMenuInflater().inflate(R.menu.main_menu, orderMenu.getMenu());

								pendingOption = orderMenu.getMenu().findItem(R.id.opt_pending);
								hitOption = orderMenu.getMenu().findItem(R.id.opt_hit);
								noHitOption = orderMenu.getMenu().findItem(R.id.opt_no_hit);

								// Agregar filtros del menu desplegable
								orderMenu.setOnMenuItemClickListener(this::optionItemSelected);
						}

						if (isDataLoaded) {
								pendingOption.setTitle(String.format(getString(R.string.pending_option_desc),
										SessionConfig.getInstance().getTotalPending().toString()));
								hitOption.setTitle(String.format(getString(R.string.hit_option_desc),
										SessionConfig.getInstance().getTotalHit().toString()));
								noHitOption.setTitle(String.format(getString(R.string.nohit_option_desc),
										SessionConfig.getInstance().getTotalNoHit().toString()));

								orderMenu.show();
						}
				});

				// Agregar filtro de caja de busqueda
				etxSearchBox.addTextChangedListener(new TextWatcher() {
						@Override
						public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

						}

						@Override
						public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

						}

						@Override
						public void afterTextChanged(Editable editable) {
								// Cerrando teclado virtual
								/* InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
								imm.hideSoftInputFromWindow(etxSearchBox.getWindowToken(), 0);*/

								// Refrescando la lista
								updateVisibleContent(null, editable.toString(), Boolean.TRUE);
						}
				});

		}

		@Override
		protected void onResume() {
				super.onResume();
		}

		@Override
		protected void onStart() {
				super.onStart();

				OrderService orderService = ApiUtils.getApi().create(OrderService.class);
				Call<OrderResponse> readOrders = orderService.findAllOrders();
				readOrders.enqueue(new Callback<OrderResponse>() {
						@Override
						public void onResponse(@NonNull Call<OrderResponse> call, @NonNull Response<OrderResponse> response) {
								if (response.isSuccessful() && response.body().getStatus().getCode().equals(StatusResponseEnum.SUCCESS.getCode())) {

										Log.i(LOG_TAG, "Actualizado lista de ordenes");

										updateOrderData(response.body());
										refreshContent();

								} else {
										System.out.println("Ocurrió un error en el GET ORDERS");
								}
						}

						@Override
						public void onFailure(@NonNull Call<OrderResponse> call, @NonNull Throwable t) {
								System.out.println("Ocurrió un error en el GET ORDERS");
								t.printStackTrace();
						}
				});

		}

		@Override
		protected void onStop() {
				super.onStop();
				isDataLoaded = Boolean.FALSE;
		}

		private void refreshContent() {
				OrderFragment refreshFragment = (OrderFragment) getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG);
				if (Objects.nonNull(refreshFragment)) {
						refreshFragment.refreshContent(SessionConfig.getInstance().getVisibleList());
						getSupportFragmentManager().beginTransaction()
								.replace(R.id.container, refreshFragment, FRAGMENT_TAG).commit();
				}
		}

		private void updateOrderData(OrderResponse response) {
				SessionConfig session = SessionConfig.getInstance();

				session.setOrderResponse(response);

				if (Objects.nonNull(session.getLastSelectedOption())) {
						updateVisibleContent(session.getLastSelectedOption(), null, Boolean.FALSE);
				} else {
						session.setVisibleList(response.getOrders());
				}

				int totalPending = 0;
				int totalHit = 0;
				int totalNoHit = 0;

				for (OrderDTO orderDTO : response.getOrders()) {
						switch (orderDTO.getOrderState()) {
								case PENDING:
										++totalPending;
										break;
								case HIT:
										++totalHit;
										break;
								case NO_HIT:
										++totalNoHit;
										break;
						}
				}

				Log.i(LOG_TAG, "Total de ordenes P|H|NH " + totalPending + "|" + totalHit + "|" + totalNoHit);

				session.setTotalPending(totalPending);
				session.setTotalHit(totalHit);
				session.setTotalNoHit(totalNoHit);

				isDataLoaded = Boolean.TRUE;
		}

		private void updateVisibleContent(OrderStateEnum stateEnum, String orderNumber, Boolean refresh) {
				List<OrderDTO> allOrders = SessionConfig.getInstance().getOrderResponse().getOrders();
				List<OrderDTO> resultList = new ArrayList<>();
				AtomicReference<OrderStateEnum> currentState = new AtomicReference<>(SessionConfig.getInstance().getLastSelectedOption());

				if (Objects.nonNull(stateEnum)) {
						resultList = allOrders.stream()
								.filter(it -> stateEnum.getCode().equals(it.getOrderState().getCode()))
								.collect(Collectors.toList());

						currentState.set(stateEnum);
				} else if (Objects.nonNull(orderNumber)) {
						resultList = (orderNumber.length() > 0)
								? allOrders.stream()
								.filter(it -> it.getOrderNumber().contains(orderNumber))
								.collect(Collectors.toList()) : allOrders;

						currentState.set((!resultList.isEmpty()) ? resultList.get(0).getOrderState() : OrderStateEnum.PENDING);
				}

				txtCurrentState.setText((OrderStateEnum.Constants.PENDING_CODE.equals(currentState.get().getCode()))
						? getString(R.string.state_pending_desc)
						: currentState.get().getDescription());

				SessionConfig.getInstance().setVisibleList(resultList);

				if (refresh) refreshContent();
		}

		public boolean optionItemSelected(MenuItem item) {
				int id = item.getItemId();

				switch (id) {
						case R.id.opt_pending:
								Log.i(LOG_TAG, "Filtrando solo ordenes con estado " + OrderStateEnum.PENDING);
								SessionConfig.getInstance().setLastSelectedOption(OrderStateEnum.PENDING);
								updateVisibleContent(OrderStateEnum.PENDING,null, Boolean.TRUE);
								break;
						case R.id.opt_hit:
								Log.i(LOG_TAG, "Filtrando solo ordenes con estado " + OrderStateEnum.HIT);
								SessionConfig.getInstance().setLastSelectedOption(OrderStateEnum.HIT);
								updateVisibleContent(OrderStateEnum.HIT, null, Boolean.TRUE);
								break;
						case R.id.opt_no_hit:
								Log.i(LOG_TAG, "Filtrando solo ordenes con estado " + OrderStateEnum.NO_HIT);
								SessionConfig.getInstance().setLastSelectedOption(OrderStateEnum.NO_HIT);
								updateVisibleContent(OrderStateEnum.NO_HIT, null, Boolean.TRUE);
								break;
						case R.id.opt_exit:
								Intent intentLogin = new Intent(this, LoginActivity.class);
								startActivity(intentLogin);
								finish();
								break;
				}

				return super.onOptionsItemSelected(item);
		}

}