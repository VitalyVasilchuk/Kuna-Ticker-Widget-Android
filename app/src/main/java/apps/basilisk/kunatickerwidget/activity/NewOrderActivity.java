package apps.basilisk.kunatickerwidget.activity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.math.BigDecimal;
import java.util.HashMap;

import apps.basilisk.kunatickerwidget.R;
import apps.basilisk.kunatickerwidget.Session;
import apps.basilisk.kunatickerwidget.tools.Utils;

public class NewOrderActivity extends AppCompatActivity implements View.OnClickListener {
    TextView textBid;
    TextView textAsk;

    TextInputEditText textAvailable;
    TextInputEditText textPrice;
    TextInputEditText textQuantity;
    TextInputEditText textAmount;

    TextInputLayout textLayoutAvailable;
    TextInputLayout textLayoutPrice;
    TextInputLayout textLayoutQuantity;
    TextInputLayout textLayoutAmount;

    Button buttonSubmit;
    Button buttonCancel;

    RadioGroup radioGroup;
    RadioButton radioBuy;
    RadioButton radioSell;

    String currencyBase;
    String currencyTrade;
    String balanceBase;
    String balanceTrade;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_order);

        Intent intent = getIntent();
        HashMap<String, String> itemMap = (HashMap<String, String>) intent.getSerializableExtra("EXTRA_DATA");

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.new_order_for)/* + " " + itemMap.get("title")*/);
        //getSupportActionBar().setSubtitle(getString(R.string.market_last_price) + ": " + itemMap.get("last"));

        ((ImageView) findViewById(R.id.image_icon)).setImageResource(Integer.parseInt(String.valueOf(itemMap.get("icon_res"))));
        textBid = findViewById(R.id.text_bid);
        textBid.setText(itemMap.get("bid"));
        textAsk = findViewById(R.id.text_ask);
        textAsk.setText(itemMap.get("ask"));
        ((TextView) findViewById(R.id.text_low)).setText(itemMap.get("low"));
        ((TextView) findViewById(R.id.text_high)).setText(itemMap.get("high"));
        ((TextView) findViewById(R.id.text_volume)).setText(itemMap.get("vol"));
        ((TextView) findViewById(R.id.text_market)).setText(itemMap.get("market"));

        currencyTrade = itemMap.get("currencyTrade").toUpperCase();
        currencyBase = itemMap.get("currencyBase").toUpperCase();
        balanceBase = itemMap.get("balanceBase");
        balanceTrade = itemMap.get("balanceTrade");

        initOrderUI();
    }

    private void initOrderUI() {
        // поля ввода значений
        textPrice = findViewById(R.id.text_price);
        textQuantity = findViewById(R.id.text_quantity);
        textAmount = findViewById(R.id.text_amount);

        textAvailable = findViewById(R.id.text_available);
        textAvailable.setOnClickListener(this);
        textBid.setOnClickListener(this);
        textAsk.setOnClickListener(this);
        textAvailable.setOnClickListener(this);

        // метки
        textLayoutAvailable = findViewById(R.id.text_layout_available);
        textLayoutPrice = findViewById(R.id.text_layout_price);
        textLayoutQuantity = findViewById(R.id.text_layout_quantity);
        textLayoutAmount = findViewById(R.id.text_layout_amount);

        // кнопки
        buttonSubmit = findViewById(R.id.button_submit);
        buttonCancel = findViewById(R.id.button_cancel);
        buttonSubmit.setOnClickListener(this);
        buttonCancel.setOnClickListener(this);

        // радиокнопки
        radioBuy = findViewById(R.id.radio_buy);
        radioSell = findViewById(R.id.radio_sell);

        // реализация слушателя ввода значений в полях цены и количества для пересчета суммы
        TextWatcher inputTW = new TextWatcher() {
            public void afterTextChanged(Editable s) {
                BigDecimal amount = new BigDecimal(0);
                BigDecimal price, volume;
                try {
                    price = new BigDecimal(textPrice.getText().toString());
                    volume = new BigDecimal(textQuantity.getText().toString());
                    amount = price.multiply(volume);
                    if (radioBuy.isChecked() || radioSell.isChecked())
                        buttonSubmit.setEnabled(true);
                } catch (NumberFormatException ex) {
                    buttonSubmit.setEnabled(false);
                }
                textAmount.setText(Utils.getFormattedValue(amount.toPlainString()));
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        };

        textPrice.addTextChangedListener(inputTW);
        textQuantity.addTextChangedListener(inputTW);

        textAvailable.setInputType(InputType.TYPE_NULL);
        textAmount.setInputType(InputType.TYPE_NULL);

        textPrice.setHint(currencyBase);
        textQuantity.setHint(currencyTrade);
        textAmount.setHint(currencyBase);

        textLayoutAvailable.setHint(getString(R.string.label_available));
        textLayoutPrice.setHint(getString(R.string.label_price) + " (" + currencyBase + ")");
        textLayoutQuantity.setHint(getString(R.string.label_quantity) + " (" + currencyTrade + ")");
        textLayoutAmount.setHint(getString(R.string.label_amount) + " (" + currencyBase + ")");

        buttonSubmit = findViewById(R.id.button_submit);
        buttonCancel = findViewById(R.id.button_cancel);
        buttonSubmit.setOnClickListener(this);
        buttonCancel.setOnClickListener(this);

        // группа Тип операции, определяем поведение при переключении типов
        radioGroup = findViewById(R.id.radioGroup);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                radioBuy.setError(null);
                radioSell.setError(null);
                switch (i) {
                    case R.id.radio_buy:
                        buttonSubmit.setText(R.string.label_buy);
                        textPrice.setText("" + textAsk.getText());
                        textLayoutAvailable.setHint(getString(R.string.label_available) + " (" + currencyBase + ")");
                        if (textAmount.getText().length() > 0) buttonSubmit.setEnabled(true);
                        if (!balanceBase.equals(""))
                            textAvailable.setText(balanceBase);
                        break;
                    case R.id.radio_sell:
                        buttonSubmit.setText(R.string.label_sell);
                        textPrice.setText("" + textBid.getText());
                        textLayoutAvailable.setHint(getString(R.string.label_available) + " (" + currencyTrade + ")");
                        if (textAmount.getText().length() > 0) buttonSubmit.setEnabled(true);
                        if (!balanceTrade.equals("")) textAvailable.setText(balanceTrade);
                        break;
                    default:
                        break;
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                setResult(Activity.RESULT_CANCELED);
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_cancel:
                setResult(Activity.RESULT_CANCELED);
                finish();
                break;

            case R.id.button_submit:
                // нужно выбрать тип операции
                if (radioGroup.getCheckedRadioButtonId() == -1) {
                    radioBuy.setError("");
                    radioSell.setError("");
                    break;
                }

                float amount = Float.parseFloat(textAmount.getText().toString());
                if (amount > 0) {
                    if (!Session.getInstance().isCorrectKeys()) {
                        AlertDialog.Builder builder;
                        builder = new AlertDialog.Builder(this);
                        builder.setMessage(getString(R.string.error_order_auth))
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                })
                                .show();
                        break;
                    }

                    // формирование описания данных ордера
                    String operation = radioBuy.isChecked() ?
                            getString(R.string.confirmation_order_operation_buy) :
                            getString(R.string.confirmation_order_operation_sell);
                    final String price = textPrice.getText().toString();
                    final String volume = textQuantity.getText().toString();
                    String sum = textAmount.getText().toString();

                    String confirmationText = "" +
                            operation + " " +
                            volume + " " + currencyTrade + "\n" +
                            getString(R.string.confirmation_order_price) + " " + price + " " + currencyBase + "\n" +
                            getString(R.string.confirmation_order_amount) + " " + sum + " " + currencyTrade;

                    // вывод подтверждающего диалога
                    AlertDialog.Builder builder;
                    builder = new AlertDialog.Builder(this);
                    builder.setTitle(R.string.confirmation_order_title)
                            .setMessage(confirmationText)
                            .setPositiveButton(operation, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent intentResult = new Intent();
                                    intentResult.putExtra("market",currencyTrade + currencyBase);
                                    intentResult.putExtra("side", radioBuy.isChecked() ? "buy" : "sell");
                                    intentResult.putExtra("price", price);
                                    intentResult.putExtra("volume", volume);

                                    setResult(RESULT_OK, intentResult);
                                    finish();
                                }
                            })
                            .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // do nothing
                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                } else {
                    textLayoutAmount.setError(getString(R.string.error_order_amount_zero));
                }
                break;

            case R.id.text_bid:
                textPrice.setText(textBid.getText());
                break;

            case R.id.text_ask:
                textPrice.setText(textAsk.getText());
                break;

            case R.id.text_available:
                String volume = "";
                try {
                    BigDecimal balance = new BigDecimal(textAvailable.getText().toString());
                    BigDecimal price = new BigDecimal(textPrice.getText().toString());
                    if (radioBuy.isChecked())
                        volume = (price.compareTo(BigDecimal.ZERO) > 0) ?
                                Utils.getFormattedValue(balance.divide(price, BigDecimal.ROUND_HALF_UP).toPlainString()) : "";
                    if (radioSell.isChecked())
                        volume = textAvailable.getText().toString();
                } catch (NumberFormatException ex) {
                    // значит там поломаный текст был, ничего не делаем
                }
                textQuantity.setText(volume);
                break;
        }

    }

}
