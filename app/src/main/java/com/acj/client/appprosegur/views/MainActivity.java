package com.acj.client.appprosegur.views;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;

import com.acj.client.appprosegur.R;
import com.acj.client.appprosegur.api.ApiService.OrderService;
import com.acj.client.appprosegur.api.ApiUtils;
import com.acj.client.appprosegur.api.model.OrderResponse;
import com.acj.client.appprosegur.api.model.constant.OrderStateEnum;
import com.acj.client.appprosegur.api.model.constant.StatusResponseEnum;
import com.acj.client.appprosegur.api.model.dto.OrderDTO;
import com.acj.client.appprosegur.databinding.ActivityMainBinding;
import com.acj.client.appprosegur.functions.EikonManager;
import com.acj.client.appprosegur.functions.SessionConfig;
import com.acj.client.appprosegur.views.login.LoginActivity;
import com.digitalpersona.uareu.ReaderCollection;
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

import cn.pedant.SweetAlert.SweetAlertDialog;
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

		private EikonManager eikonManager;

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
				eikonManager = new EikonManager(context);

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

						pendingOption.setTitle(String.format(getString(R.string.pending_option_desc),
								SessionConfig.getInstance().getTotalPending().toString()));
						hitOption.setTitle(String.format(getString(R.string.hit_option_desc),
								SessionConfig.getInstance().getTotalHit().toString()));
						noHitOption.setTitle(String.format(getString(R.string.nohit_option_desc),
								SessionConfig.getInstance().getTotalNoHit().toString()));

						orderMenu.show();
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

				//eikonManager.requestPermission(mainUsbReceiver, this);
				//SERGIO SICCHA -> SOLICITAR PERMISOS HUELLERO

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

				session.setTotalPending(totalPending);
				session.setTotalHit(totalHit);
				session.setTotalNoHit(totalNoHit);
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
								SessionConfig.getInstance().setLastSelectedOption(OrderStateEnum.PENDING);
								updateVisibleContent(OrderStateEnum.PENDING,null, Boolean.TRUE);
								break;
						case R.id.opt_hit:
								SessionConfig.getInstance().setLastSelectedOption(OrderStateEnum.HIT);
								updateVisibleContent(OrderStateEnum.HIT, null, Boolean.TRUE);
								break;
						case R.id.opt_no_hit:
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

		private Boolean deteccionHuellero() {
				try {
						ReaderCollection readers = eikonManager.getReaders();

						if (readers.size() >= 1) {
								eikonManager.activateReader();
								eikonManager.getSerialNumber();
								return Boolean.TRUE;
						} else {
								Log.i("VerificarFDP Huellero", "No Hay Huellero");
								SweetAlertDialog a = new SweetAlertDialog(context, SweetAlertDialog.ERROR_TYPE);
								a.setCancelable(false);
								a.setCanceledOnTouchOutside(false);
								a.setTitleText("EIKON Fingerprint SDK");
								a.setConfirmText("OK");
								a.setConfirmButtonTextColor(Color.WHITE);
								a.setConfirmButtonBackgroundColor(Color.RED);
								a.setContentText("¡La inicialización del scanner" +
										" de huellas digitales ha fallado!" + "\n" +
										"Conecte el scanner a su dispositivo");
								a.setConfirmClickListener(sDialog -> {
										sDialog.dismiss();

										Intent intent = this.getIntent();
										intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK
												| Intent.FLAG_ACTIVITY_NO_ANIMATION);
										this.overridePendingTransition(0, 0);
										this.finish();

										this.overridePendingTransition(0, 0);
										startActivity(intent);
								});
								a.show();
								return Boolean.FALSE;
						}

				} catch (Exception ex) {
						ex.printStackTrace();
				}
				return Boolean.FALSE;
		}

		// SERGIO SICCHA -> VALIDAR SI SE PUEDE MOVER A LA ACTIVITY DE CAPTURA
		private final BroadcastReceiver mainUsbReceiver = new BroadcastReceiver() {
				public void onReceive(Context context, Intent intent) {
						Log.i("SERGIO  eikon ", "-------broadcaster Fragment --------");
						String action = intent.getAction();
						if (EikonManager.ACTION_USB_PERMISSION.equals(action)) {
								synchronized (this) {
										UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
										if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
												if (device != null) {
														SessionConfig.getInstance().setAllowedPermission(true);
														//huellero = deteccionHuellero(); SERGIO SICCHA -> REVISAR
												}
										}
										context.unregisterReceiver(mainUsbReceiver);
								}
						}
				}
		};

}