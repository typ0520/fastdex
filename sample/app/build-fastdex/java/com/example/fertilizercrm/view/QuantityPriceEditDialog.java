package com.example.fertilizercrm.view;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.fertilizercrm.R;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.example.fertilizercrm.common.utils.Logger;

/**
 * Created by tong on 15/12/14.
 * 发货，信息编辑框
 */
public class QuantityPriceEditDialog extends Dialog {
    /**
     * 编辑数量和价格的模式
     */
    public static final int MODE_QUANTITY_AND_PRICE = 0;

    /**
     * 支持编辑数量的模式
     */
    public static final int MODE_QUANTITY = 1;
    /**
     * 支持编辑价格的模式
     */
    public static final int MODE_PRICE = 2;

    public static final String DEFAULT_EMPTY_PRICE = "0.00";
    public static final String DEFAULT_EMPTY_QUANTIT = "0";

    @Bind(R.id.rl_quantity) View rl_quantity;//价格背景框
    @Bind(R.id.iv_quantity) ImageView iv_quantity;
    @Bind(R.id.tv_quantity_label) TextView tv_quantity_label;
    @Bind(R.id.tv_quantity) TextView tv_quantity;
    @Bind(R.id.tv_name) TextView tv_name;
    @Bind(R.id.rl_price)    View rl_price;   //价格背景框
    @Bind(R.id.iv_price) ImageView iv_price;
    @Bind(R.id.tv_price_label) TextView tv_price_label;
    @Bind(R.id.tv_price) TextView tv_price;

    private Context context;
    private String quantitUnit = "吨";
    private String quantit = DEFAULT_EMPTY_QUANTIT;//数量
    private String price = DEFAULT_EMPTY_PRICE;//价格

    private boolean isPriceMode;//是不是正在编辑价格
    private SendOutEventListener mSendOutEventListener;
    private int mode = MODE_QUANTITY_AND_PRICE;
    private boolean integerPrice;

    public QuantityPriceEditDialog(Context context) {
        super(context, R.style.Dialog_bocop);
        this.context = context;

        View rootView = View.inflate(context, R.layout.dialog_send_out_edit_info, null);
        setContentView(rootView);

        ButterKnife.bind(this, rootView);

        View container = rootView.findViewById(R.id.rl_container);
        scanButton((ViewGroup) container);
    }

    public void setMode(int mode) {
        if (mode == MODE_QUANTITY_AND_PRICE) {
            rl_quantity.setVisibility(View.VISIBLE);
            rl_price.setVisibility(View.VISIBLE);
        }
        else if (mode == MODE_QUANTITY) {
            rl_quantity.setVisibility(View.VISIBLE);
            rl_price.setVisibility(View.GONE);
            isPriceMode = false;
        }
        else if (mode == MODE_PRICE) {
            rl_quantity.setVisibility(View.GONE);
            rl_price.setVisibility(View.VISIBLE);
            isPriceMode = true;
        } else {
            throw new RuntimeException("not support mode");
        }
        this.mode = mode;
    }

    public void setIntegerPrice(boolean integerPrice) {
        if (integerPrice) {
            this.integerPrice = integerPrice;
        }
    }

    public void setPriceTitle(String title) {
        tv_price_label.setText(title);
    }

    @OnClick(R.id.ib_close) void clickClose() {
        //关闭
        dismiss();
    }

    @OnClick(R.id.rl_quantity) void clickQuantity() {
        //数量
        isPriceMode = false;
        iv_quantity.setVisibility(View.VISIBLE);
        rl_quantity.setBackgroundColor(context.getResources().getColor(R.color.send_out_red_bg));
        tv_quantity_label.setTextColor(context.getResources().getColor(R.color.send_out_label));
        tv_quantity.setTextColor(context.getResources().getColor(R.color.white));

        iv_price.setVisibility(View.INVISIBLE);
        rl_price.setBackgroundColor(context.getResources().getColor(R.color.send_out_red_bg_default));
        tv_price_label.setTextColor(context.getResources().getColor(R.color.send_out_label_default));
        tv_price.setTextColor(context.getResources().getColor(R.color.black));
    }

    @OnClick(R.id.rl_price) void clickPrice() {
        //价格
        isPriceMode = true;
        iv_price.setVisibility(View.VISIBLE);
        rl_price.setBackgroundColor(context.getResources().getColor(R.color.send_out_red_bg));
        tv_price_label.setTextColor(context.getResources().getColor(R.color.send_out_label));
        tv_price.setTextColor(context.getResources().getColor(R.color.white));

        iv_quantity.setVisibility(View.INVISIBLE);
        rl_quantity.setBackgroundColor(context.getResources().getColor(R.color.send_out_red_bg_default));
        tv_quantity_label.setTextColor(context.getResources().getColor(R.color.send_out_label_default));
        tv_quantity.setTextColor(context.getResources().getColor(R.color.black));
    }

    protected void scanButton(ViewGroup container) {
        for (int i = 0;i < container.getChildCount();i++) {
            View item = container.getChildAt(i);
            if (item instanceof Button) {
                setItemOnClickListener(item, ActionEvent.valueOf(item));
            }
            else if (item instanceof ImageButton) {
                setItemOnClickListener(item, ActionEvent.valueOf(item));
            }
            else if (item instanceof ViewGroup) {
                scanButton((ViewGroup)item);
            }
        }
    }

    private void setItemOnClickListener(View v, final ActionEvent actionEvent) {
        //Logger.d(">> actionEvent: " + actionEvent.toString());
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClick(actionEvent);
            }
        });
    }

    protected void onItemClick(ActionEvent actionEvent) {
        if (actionEvent.getType() == ActionEvent.TYPE_ACTION) {
            handleAction(actionEvent);
        }
        else if (actionEvent.getType() == ActionEvent.TYPE_CHAR) {
            handleChar(actionEvent);
        }
        else {
            handleNumberEvent(actionEvent);
        }

        syncValue();
    }

    private void handleNumberEvent(ActionEvent actionEvent) {
        if (isPriceMode) {
            if (price.equals("0.00")) {
                price = actionEvent.getTag() + "";
                return;
            }

            int pointIndex = price.indexOf(".");
            if (pointIndex != -1
                    && price.substring(pointIndex + 1).length() == 2) {
                //小数点后已有两位
                return;
            }

            if (price.equals("0")) {
                price = actionEvent.getTag() + "";
                return;
            }
            price = price + actionEvent.getTag();
        } else {
            //第一位如果是0，再输入0忽略掉
            if (actionEvent.getTag() == 0 && quantit.startsWith("0")) {
                return;
            }
            if (quantit.equals("0")
                    || quantit.equals("0.")
                    || quantit.equals("0.0")
                    || quantit.equals("0.00")) {
                quantit = actionEvent.getTag() + "";
                return;
            }
            quantit = quantit + actionEvent.getTag();
        }
    }

    private void handleChar(ActionEvent actionEvent) {
        if (actionEvent.getTag() == ActionEvent.CHAR_POINT) {
            if (!isPriceMode) {
                return;
            }
            if (integerPrice) {
                return;
            }

            if (price.contains(".")) {
                return;
            }

            price = price + ".";
        }
    }

    private void handleAction(ActionEvent actionEvent) {
        if (actionEvent.getTag() == ActionEvent.ACTION_CLEAR) {
            if (isPriceMode) {
                price = DEFAULT_EMPTY_PRICE;
            } else {
                quantit = DEFAULT_EMPTY_QUANTIT;
            }
        }
        else if (actionEvent.getTag() == ActionEvent.ACTION_DEL) {
            if (isPriceMode) {
                if (price.length() == 1) {
                    price = "0";
                } else {
                    price = price.substring(0,price.length() - 1);
                }
            } else {
                if (quantit.length() == 1) {
                    quantit = DEFAULT_EMPTY_QUANTIT;
                } else {
                    quantit = quantit.substring(0,quantit.length() - 1);
                }
            }
        }
        else if (actionEvent.getTag() == ActionEvent.ACTION_OK) {
            boolean isClose = false;
            if (mSendOutEventListener != null) {
                isClose = mSendOutEventListener.onDetermine(this.quantit,this.price);
            }
            if (!isClose) {
                dismiss();
            }
        }
    }

    private void syncValue() {
        if (isPriceMode) {
            String text = price;
            if (text.equals("0") || text.equals("0.") || text.equals("0.0")) {
                text = DEFAULT_EMPTY_PRICE;
            }
            tv_price.setText(text);

            Logger.d(">> syncValue price: " + tv_price.getText().toString());
        } else {
            tv_quantity.setText(quantit + getQuantitUnit());
            Logger.d(">> syncValue quantit: " + tv_quantity.getText().toString());
        }
    }

    public String getQuantitUnit() {
        return quantitUnit;
    }

    public String getQuantit() {
        return quantit;
    }

    public void setQuantit(String quantit) {
        if (quantit == null) {
            quantit = DEFAULT_EMPTY_QUANTIT;
        }
        this.quantit = quantit;
        tv_quantity.setText(quantit + getQuantitUnit());
    }

    public void setQuantit(int quantit) {
       this.setQuantit(quantit + "");
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        if (price == null) {
            price = DEFAULT_EMPTY_PRICE;
        }
        if (price.equals("0") || price.equals("0.") || price.equals("0.0")) {
            price = DEFAULT_EMPTY_PRICE;
        }
        tv_price.setText(price);
        this.price = price;
    }

    public void setProductName(String name) {
        tv_name.setText(name);
    }

    static class ActionEvent {
        /**
         * 数字类型
         */
        public static final int TYPE_NUMBER = 1;
        /**
         * 字符类型
         */
        public static final int TYPE_CHAR = 2;
        /**
         * 动作类型
         */
        public static final int TYPE_ACTION = 3;

        /**
         * 清空
         */
        public static final int ACTION_CLEAR = 1;
        /**
         * 删除一个字符
         */
        public static final int ACTION_DEL = 2;
        /**
         * 确定
         */
        public static final int ACTION_OK = 3;

        /**
         * 小数点
         */
        public static final int CHAR_POINT = 0;

        private int type;

        private int tag;

        private View view;

        public ActionEvent(View view, int type, int tag) {
            this.view = view;
            this.type = type;
            this.tag = tag;
        }

        public int getType() {
            return type;
        }

        public int getTag() {
            return tag;
        }

        public View getView() {
            return view;
        }

        @Override
        public String toString() {
            return "ActionEvent{" +
                    "type=" + type +
                    ", tag=" + tag +
                    ", view=" + view +
                    '}';
        }

        public static ActionEvent valueOf(View v) {
            if (v.getId() != View.NO_ID) {
                return new ActionEvent(v,TYPE_ACTION,convert2Action(v.getId()));
            }
            String text = ((Button)v).getText().toString();

            try {
                return new ActionEvent(v,TYPE_NUMBER,Integer.valueOf(text));
            } catch (NumberFormatException e) {
                return new ActionEvent(v,TYPE_CHAR,convert2char(text));
            }
        }

        private static int convert2Action(int id) {
            if (id == R.id.btn_clear) {
                return ACTION_CLEAR;
            }
            else if (id == R.id.btn_del) {
                return ACTION_DEL;
            }
            if (id == R.id.btn_ok) {
                return ACTION_OK;
            }
            return 0;
        }

        private static int convert2char(String text) {
            if (".".equals(text)) {
                return CHAR_POINT;
            }
            return 0;
        }
    }

    public void setSendOutEventListener(SendOutEventListener listener) {
        this.mSendOutEventListener = listener;
    }

    public interface SendOutEventListener {
        boolean onDetermine(String quantit,String price);
    }
}
