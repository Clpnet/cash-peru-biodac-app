package com.acj.client.prosegur.views;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import com.acj.client.prosegur.R;
import com.acj.client.prosegur.api.ApiService.OrderService;
import com.acj.client.prosegur.api.ApiService.UsuarioService;
import com.acj.client.prosegur.api.ApiUtils;
import com.acj.client.prosegur.databinding.ActivityMainBinding;
import com.acj.client.prosegur.model.common.CommonResponse;
import com.acj.client.prosegur.model.constant.OrderStateEnum;
import com.acj.client.prosegur.model.constant.StatusResponseEnum;
import com.acj.client.prosegur.model.dto.orders.OrderDTO;
import com.acj.client.prosegur.config.SessionConfig;
import com.acj.client.prosegur.model.dto.user.UserDetailsDTO;
import com.acj.client.prosegur.util.Constants;
import com.acj.client.prosegur.util.Util;
import com.acj.client.prosegur.views.dialog.LoadingDialogFragment;
import com.acj.client.prosegur.views.login.LoginActivity;
import com.google.android.material.appbar.AppBarLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;

import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import static com.acj.client.prosegur.util.Constants.LOADING_DIALOG_TAG;
import static com.acj.client.prosegur.util.Util.killSessionOnMicrosoft;

import org.apache.commons.lang3.StringUtils;

public class MainActivity extends AppCompatActivity {

		private final String LOG_TAG = MainActivity.class.getSimpleName();

		private final String FRAGMENT_TAG = "ORDER-FRAGMENT";

		private ActivityMainBinding binding;

		private ImageView menuIcon;
		private TextView txtWelcome;
		private PopupMenu orderMenu;
		private MenuItem pendingOption;
		private MenuItem hitOption;
		private MenuItem noHitOption;

		private TextView txtCurrentState;
		private EditText etxSearchBox;

		private Context mContext;

		private Boolean isDataLoaded;

		private LoadingDialogFragment dialogHandler;

		// Dialog
		private AlertDialog.Builder dialogBuilder;

		// Services
		private UsuarioService usuarioService;
		private OrderService orderService;

		// Variables globales
		private String deviceName;

		@Override
		protected void onCreate(Bundle savedInstanceState) {
				super.onCreate(savedInstanceState);

				binding = ActivityMainBinding.inflate(getLayoutInflater());

				setContentView(R.layout.activity_main);

				Retrofit retrofit = ApiUtils.getApi(mContext);
				usuarioService = retrofit.create(UsuarioService.class);
				orderService = retrofit.create(OrderService.class);

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

				if (Objects.isNull(SessionConfig.getInstance().getUserDetails())) {
					Call<CommonResponse> usuarioResponse = usuarioService.getUserDetails(SessionConfig.getInstance().getAccessToken(),
							Constants.APP_CHANNEL,
							SessionConfig.getInstance().getMAccount().getUsername());
					usuarioResponse.enqueue(new Callback<CommonResponse>() {
							@Override
							public void onResponse(Call<CommonResponse> call, Response<CommonResponse> response) {
									if (response.isSuccessful()) {
											if (StatusResponseEnum.SUCCESS.getCode().equals(response.body().getCabecera().getCodigo())) {
													Log.i(LOG_TAG, "Respuesta exitosa de los detalles del usuario. Response [" + response.body().getObjeto() + "]");

													UserDetailsDTO userDetails = Util.jsonToClass(response.body().getObjeto(), UserDetailsDTO.class);

													// Set welcome text
													txtWelcome.setText(String.format(mContext.getString(R.string.txt_appbar_welcome_desc),
															StringUtils.joinWith(StringUtils.SPACE,
																	StringUtils.capitalize(userDetails.getPrimerNombre().toLowerCase()),
																	StringUtils.capitalize(userDetails.getApellidoPaterno().toLowerCase()))));

													txtWelcome.setVisibility(View.VISIBLE);

													SessionConfig.getInstance().setUserDetails(userDetails);
													SessionConfig.getInstance().setNumberIntents(userDetails.getNumeroIntentos());

													findOrders();
											} else {
													Log.e(LOG_TAG, "onResponse() -> No Success Code -> Ocurrió un error en el GET USER DETAILS");
													closeDialog(Boolean.FALSE);
													runOnUiThread(() -> showErrorDialog(mContext.getString(R.string.app_name),
															mContext.getString(R.string.err_get_session),
															(dialog, which) -> exit()));
											}

									} else {
											Log.e(LOG_TAG, "onResponse() -> succesful = false -> Ocurrió un error en el GET USER DETAILS");
											closeDialog(Boolean.FALSE);
											runOnUiThread(() -> showErrorDialog(mContext.getString(R.string.app_name),
													mContext.getString(R.string.err_get_session),
													(dialog, which) -> exit()));
									}
							}

							@Override
							public void onFailure(Call<CommonResponse> call, Throwable t) {
									Log.e(LOG_TAG, "onFailure() -> Ocurrió un error en el GET USER DETAILS. [" + t + "]");
									closeDialog(Boolean.FALSE);
									runOnUiThread(() -> showErrorDialog(mContext.getString(R.string.app_name),
											mContext.getString(R.string.err_get_session),
											(dialog, which) -> exit()));
									t.printStackTrace();
							}
					});
				} else {
						findOrders();
				}
		}

		@Override
		protected void onStop() {
				super.onStop();
				isDataLoaded = Boolean.FALSE;
				closeDialog(Boolean.FALSE);
		}

		private void findOrders() {
				Log.i(LOG_TAG, "Codigo Interno [" + SessionConfig.getInstance().getUserDetails().getCodigoInterno() + "]");
				Call<CommonResponse> readOrders = orderService.findAllOrders(SessionConfig.getInstance().getUserDetails().getCodigoInterno());
				readOrders.enqueue(new Callback<CommonResponse>() {
						@Override
						public void onResponse(@NonNull Call<CommonResponse> call, @NonNull Response<CommonResponse> response) {
								if (response.isSuccessful()) {
										if (StatusResponseEnum.SUCCESS.getCode().equals(response.body().getCabecera().getCodigo())) {
												Log.i(LOG_TAG, "Respuesta exitosa del listado de ordeners. Response [" + response.body().getObjeto() + "]");

												updateOrderData(response.body());
												refreshContent(Boolean.TRUE);
										} else {
												Log.e(LOG_TAG, "onResponse() -> No Success Code -> Error controlado en GET ORDERS. Codigo " + response.body().getCabecera().getCodigo());
												runOnUiThread(() -> showErrorDialog(mContext.getString(R.string.app_name),
														mContext.getString(R.string.err_get_orders),
														(dialog, which) -> exit()));
										}
								} else {
										Log.e(LOG_TAG, "onResponse() -> succesful = false -> Ocurrió un error en el GET ORDERS");
										closeDialog(Boolean.FALSE);
										runOnUiThread(() -> showErrorDialog(mContext.getString(R.string.app_name),
												mContext.getString(R.string.err_get_orders),
												(dialog, which) -> exit()));
								}
						}

						@Override
						public void onFailure(@NonNull Call<CommonResponse> call, @NonNull Throwable t) {
								Log.e(LOG_TAG, "onFailure() -> Ocurrió un error en el GET ORDERS. [" + t + "]");
								closeDialog(Boolean.FALSE);
								runOnUiThread(() -> showErrorDialog(mContext.getString(R.string.app_name),
										mContext.getString(R.string.err_get_orders),
										(dialog, which) -> exit()));
								t.printStackTrace();
						}
				});
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
				txtWelcome = toolbar.findViewById(R.id.txtWelcome);
				txtCurrentState = findViewById(R.id.txtOrderStates);
				etxSearchBox = findViewById(R.id.searchEditText);

				mContext = this;

				deviceName = Settings.Global.getString(mContext.getContentResolver(), Settings.Global.DEVICE_NAME);

				Log.i(LOG_TAG, "Device Name: " + deviceName);

				dialogBuilder = new AlertDialog.Builder(MainActivity.this);

				// Render content on fragment
				OrderFragment orderFragment = new OrderFragment(mContext, new ArrayList<>());
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
						public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

						@Override
						public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

						@Override
						public void afterTextChanged(Editable editable) {
								// Refrescando la lista
								updateVisibleContent(null, editable.toString(), Boolean.TRUE);
						}
				});

				dialogHandler = new LoadingDialogFragment(MainActivity.this);

				getImei();
		}

		private void refreshContent(Boolean isDataFromBack) {
				OrderFragment refreshFragment = (OrderFragment) getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG);
				if (Objects.nonNull(refreshFragment)) {
						refreshFragment.refreshContent(SessionConfig.getInstance().getVisibleList(), isDataFromBack);
						getSupportFragmentManager().beginTransaction()
								.replace(R.id.container, refreshFragment, FRAGMENT_TAG).commit();
				}
		}

		private void updateOrderData(CommonResponse response) {
				Log.i(LOG_TAG, "Actualizado listado de ordenes");

				SessionConfig session = SessionConfig.getInstance();
				session.setCommonResponse(response);

				List<OrderDTO> orders = (Objects.nonNull(response.getObjeto()))
						? Arrays.asList(Util.jsonToClass(response.getObjeto(), OrderDTO[].class)) : Collections.EMPTY_LIST;
				session.setAllOrders(orders);

				if (Objects.nonNull(session.getLastSelectedOption())) {
						updateVisibleContent(session.getLastSelectedOption(), null, Boolean.FALSE);
				} else {
						session.setVisibleList(orders);
				}

				int totalPending = 0;
				int totalHit = 0;
				int totalNoHit = 0;

				for (OrderDTO orderDTO : orders) {
						switch (orderDTO.getEstadoEntrega()) {
								case C:
										++totalPending;
										break;
								case H:
										++totalHit;
										break;
								case N:
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
				List<OrderDTO> allOrders = SessionConfig.getInstance().getAllOrders();
				List<OrderDTO> resultList = new ArrayList<>();

				if (Objects.nonNull(stateEnum)) {
						resultList = allOrders.stream()
								.filter(it -> stateEnum.getCode().equals(it.getEstadoEntrega().getCode()))
								.collect(Collectors.toList());

						txtCurrentState.setText((OrderStateEnum.Constants.PENDING_CODE.equals(stateEnum.getCode()))
								? getString(R.string.state_pending_desc)
								: stateEnum.getDescription());
				} else if (Objects.nonNull(orderNumber)) {
						boolean wordIsNotEmpty = StringUtils.isNotEmpty(orderNumber);

						resultList = (wordIsNotEmpty)
								? allOrders.stream()
								.filter(it -> it.getCodigoOperacion().contains(orderNumber))
								.collect(Collectors.toList()) : allOrders;

						txtCurrentState.setText(R.string.state_default_desc);
				}

				SessionConfig.getInstance().setVisibleList(resultList);

				if (refresh) refreshContent(Boolean.FALSE);
		}

		public void showErrorDialog(String title, String message, DialogInterface.OnClickListener listener) {
				dialogBuilder
						.setTitle(title)
						.setMessage(message)
						.setCancelable(false)
						.setPositiveButton(mContext.getString(R.string.btn_confirmation), listener)
						.create().show();
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
								Log.i(LOG_TAG, "Filtrando solo ordenes con estado " + OrderStateEnum.N);
								SessionConfig.getInstance().setLastSelectedOption(OrderStateEnum.N);
								updateVisibleContent(OrderStateEnum.N, null, Boolean.TRUE);
								break;
						case R.id.opt_exit:
								exit();
								break;
				}

				return super.onOptionsItemSelected(item);
		}

		public void getImei() {
				try {
						String androidId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
						Log.i(LOG_TAG, "androidId: " + androidId);
				} catch (Exception e) {
						Log.e(LOG_TAG, "Ocurrio un error al obtener el IMEI: " + e);
				}
		}

		public void exit() {
				killSessionOnMicrosoft();
				SessionConfig.closeSession();
				sendToLogin();
		}

		public void sendToLogin() {
				Intent intentLogin = new Intent(this, LoginActivity.class);
				startActivity(intentLogin);
				finishAffinity();
		}

}