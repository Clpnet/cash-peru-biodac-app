package com.acj.client.prosegur.views;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.acj.client.prosegur.R;
import com.acj.client.prosegur.api.ApiService.OrderService;
import com.acj.client.prosegur.api.ApiUtils;
import com.acj.client.prosegur.model.common.OrderResponse;
import com.acj.client.prosegur.model.constant.OrderStateEnum;
import com.acj.client.prosegur.model.constant.StatusResponseEnum;
import com.acj.client.prosegur.model.dto.orders.OrderDTO;
import com.acj.client.prosegur.databinding.ActivityMainBinding;
import com.acj.client.prosegur.handler.SessionConfig;
import com.acj.client.prosegur.views.dialog.LoadingDialogFragment;
import com.acj.client.prosegur.views.login.LoginActivity;
import com.google.android.material.appbar.AppBarLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;

import android.os.Handler;
import android.os.Looper;
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

import static com.acj.client.prosegur.util.Constants.LOADING_DIALOG_TAG;

public class MainActivity extends AppCompatActivity {

		private final String LOG_TAG = MainActivity.class.getSimpleName();

		private final String FRAGMENT_TAG = "ORDER-FRAGMENT";

		private ActivityMainBinding binding;

		private ImageView menuIcon;
		private PopupMenu orderMenu;
		private MenuItem pendingOption;
		private MenuItem hitOption;
		private MenuItem noHitOption;

		private TextView txtCurrentState;
		private EditText etxSearchBox;

		private Context context;

		private Boolean isDataLoaded;

		private LoadingDialogFragment dialogHandler;

		@Override
		protected void onCreate(Bundle savedInstanceState) {
				super.onCreate(savedInstanceState);

				binding = ActivityMainBinding.inflate(getLayoutInflater());

				setContentView(R.layout.activity_main);

				initViewElements();
		}

		@Override
		protected void onResume() {
				super.onResume();
		}

		@Override
		protected void onStart() {
				super.onStart();

				dialogHandler.show(getSupportFragmentManager(), LOADING_DIALOG_TAG);

				OrderService orderService = ApiUtils.getApi().create(OrderService.class);
				Call<OrderResponse> readOrders = orderService.findAllOrders("12345");
				readOrders.enqueue(new Callback<OrderResponse>() {
						@Override
						public void onResponse(@NonNull Call<OrderResponse> call, @NonNull Response<OrderResponse> response) {
								if (response.isSuccessful() && StatusResponseEnum.SUCCESS.getCode().equals(response.body().getCabecera().getCodigo())) {
										Log.i(LOG_TAG, "Respuesta exitosa del listado de ordeners. Response [" + response.body().getObjeto() + "]");

										updateOrderData(response.body());
										refreshContent();

								} else {
										Log.e(LOG_TAG, "onResponse() -> Ocurrió un error en el GET ORDERS");
										closeDialog(Boolean.FALSE);
								}
						}

						@Override
						public void onFailure(@NonNull Call<OrderResponse> call, @NonNull Throwable t) {
								Log.e(LOG_TAG, "onFailure() -> Ocurrió un error en el GET ORDERS. [" + t + "]");
								closeDialog(Boolean.FALSE);
								t.printStackTrace();
						}
				});

		}

		@Override
		protected void onStop() {
				super.onStop();
				isDataLoaded = Boolean.FALSE;
				closeDialog(Boolean.FALSE);
		}

		private void closeDialog(Boolean delay) {
				if (Objects.nonNull(dialogHandler.getDialog()) && dialogHandler.getDialog().isShowing())
						new Handler(Looper.getMainLooper()).postDelayed(() -> dialogHandler.dismiss(), (delay) ? 900 : 0);
		}

		private void initViewElements() {
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

				dialogHandler = new LoadingDialogFragment(MainActivity.this);
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
				Log.i(LOG_TAG, "Actualizado listado de ordenes");

				SessionConfig session = SessionConfig.getInstance();

				session.setOrderResponse(response);

				if (Objects.nonNull(session.getLastSelectedOption())) {
						updateVisibleContent(session.getLastSelectedOption(), null, Boolean.FALSE);
				} else {
						session.setVisibleList(response.getObjeto());
				}

				int totalPending = 0;
				int totalHit = 0;
				int totalNoHit = 0;

				for (OrderDTO orderDTO : response.getObjeto()) {
						switch (orderDTO.getEstadoEntrega()) {
								case C:
										++totalPending;
										break;
								case H:
										++totalHit;
										break;
								case NH:
										++totalNoHit;
										break;
						}
				}

				Log.i(LOG_TAG, "Total de ordenes P|H|NH " + totalPending + "|" + totalHit + "|" + totalNoHit);

				session.setTotalPending(totalPending);
				session.setTotalHit(totalHit);
				session.setTotalNoHit(totalNoHit);

				isDataLoaded = Boolean.TRUE;

				closeDialog(Boolean.TRUE);
		}

		private void updateVisibleContent(OrderStateEnum stateEnum, String orderNumber, Boolean refresh) {
				List<OrderDTO> allOrders = SessionConfig.getInstance().getOrderResponse().getObjeto();
				List<OrderDTO> resultList = new ArrayList<>();
				AtomicReference<OrderStateEnum> currentState = new AtomicReference<>(SessionConfig.getInstance().getLastSelectedOption());

				if (Objects.nonNull(stateEnum)) {
						resultList = allOrders.stream()
								.filter(it -> stateEnum.getCode().equals(it.getEstadoEntrega().getCode()))
								.collect(Collectors.toList());

						currentState.set(stateEnum);
				} else if (Objects.nonNull(orderNumber)) {
						resultList = (orderNumber.length() > 0)
								? allOrders.stream()
								.filter(it -> it.getCodigoOrden().contains(orderNumber))
								.collect(Collectors.toList()) : allOrders;

						currentState.set((!resultList.isEmpty()) ? resultList.get(0).getEstadoEntrega() : OrderStateEnum.C);
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
								Log.i(LOG_TAG, "Filtrando solo ordenes con estado " + OrderStateEnum.C);
								SessionConfig.getInstance().setLastSelectedOption(OrderStateEnum.C);
								updateVisibleContent(OrderStateEnum.C,null, Boolean.TRUE);
								break;
						case R.id.opt_hit:
								Log.i(LOG_TAG, "Filtrando solo ordenes con estado " + OrderStateEnum.H);
								SessionConfig.getInstance().setLastSelectedOption(OrderStateEnum.H);
								updateVisibleContent(OrderStateEnum.H, null, Boolean.TRUE);
								break;
						case R.id.opt_no_hit:
								Log.i(LOG_TAG, "Filtrando solo ordenes con estado " + OrderStateEnum.NH);
								SessionConfig.getInstance().setLastSelectedOption(OrderStateEnum.NH);
								updateVisibleContent(OrderStateEnum.NH, null, Boolean.TRUE);
								break;
						case R.id.opt_exit:
								Intent intentLogin = new Intent(this, LoginActivity.class);
								startActivity(intentLogin);
								SessionConfig.closeSession();
								finishAffinity();
								break;
				}

				return super.onOptionsItemSelected(item);
		}

}