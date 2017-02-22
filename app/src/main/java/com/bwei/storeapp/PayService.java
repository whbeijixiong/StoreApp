package com.bwei.storeapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.alipay.sdk.app.PayTask;

import java.util.Map;

/**
 * 1. 类的用途
 * 2. @author：liqingyi
 * 3. @date：2017/2/22 11:43
 */

public class PayService {

    /**
     * 支付宝支付业务：入参app_id,请填写自己的商户APP_ID
     */
    public static final String APPID = "";

    /** 商户私钥，pkcs8格式 */
    /** 如下私钥，RSA2_PRIVATE 或者 RSA_PRIVATE 只需要填入一个 */
    /** 如果商户两个都设置了，优先使用 RSA2_PRIVATE */
    /** RSA2_PRIVATE 可以保证商户交易在更加安全的环境下进行，建议使用 RSA2_PRIVATE */
    /** 获取 RSA2_PRIVATE，建议使用支付宝提供的公私钥生成工具生成， */
    /**
     * 工具地址：https://doc.open.alipay.com/docs/doc.htm?treeId=291&articleId=106097&docType=1
     */
    public static final String RSA2_PRIVATE = "";

    /**
     * 商户私钥，pkcs8格式,请填写自己的商户私钥
     */
    public static final String RSA_PRIVATE = "";

    Activity context;

    public PayService(Activity context) {
        this.context = context;
    }

    /**
     * 调用支付宝支付
     */
    public void payV2() {
        if (TextUtils.isEmpty(APPID) || (TextUtils.isEmpty(RSA2_PRIVATE) && TextUtils.isEmpty(RSA_PRIVATE))) {
            new AlertDialog.Builder(context).setTitle("警告").setMessage("需要配置APPID | RSA_PRIVATE")
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialoginterface, int i) {
                            //TODO
                        }
                    }).show();
            return;
        }
        //信息完整，支付跳转
        else {
            //TODO orderInfo的获取必须来自服务端；
            new PayServiceTask(context).execute(orderInfo());
        }
    }

    /**
     * 在服务端获取支付信息
     *
     * @return
     */
    private String orderInfo() {
        /**
         * 这里只是为了方便直接向商户展示支付宝的整个支付流程；所以Demo中加签过程直接放在客户端完成；
         * 真实App里，privateKey等数据严禁放在客户端，加签过程务必要放在服务端完成；
         * 防止商户私密数据泄露，造成不必要的资金损失，及面临各种安全风险；
         *
         * orderInfo的获取必须来自服务端；
         */
        boolean rsa2 = (RSA2_PRIVATE.length() > 0);

        Map<String, String> params = OrderInfoUtil2_0.buildOrderParamMap(APPID, rsa2);
        String orderParam = OrderInfoUtil2_0.buildOrderParam(params);

        String privateKey = rsa2 ? RSA2_PRIVATE : RSA_PRIVATE;
        String sign = OrderInfoUtil2_0.getSign(params, privateKey, rsa2);

        return orderParam + "&" + sign;
    }

    /**
     * 支付服务核心类，对参数进行加密，签名
     */
    static class PayServiceTask extends AsyncTask<String, Void, PayResult> {

        Activity activity;

        public PayServiceTask(Activity activity) {
            this.activity = activity;
        }

        /**
         * 调用支付宝接口，并跳转到支付宝支付页面
         *
         * @param params
         * @return
         */
        @Override
        protected PayResult doInBackground(String... params) {
            PayTask alipay = new PayTask(activity);
            Map<String, String> result = alipay.payV2(params[0], true);
            Log.i("msp", result.toString());
            return new PayResult(result);
        }

        /**
         * 支付成功，返回回调结果
         *
         * @param payResult
         */
        @Override
        protected void onPostExecute(PayResult payResult) {
            super.onPostExecute(payResult);
            /**
             对于支付结果，请商户依赖服务端的异步通知结果。同步通知结果，仅作为支付结束的通知。
             */
            String resultInfo = payResult.getResult();// 同步返回需要验证的信息
            String resultStatus = payResult.getResultStatus();
            //TODO 如需测试情收到修改返回状态码为 9000
            // 判断resultStatus 为9000则代表支付成功
            if (TextUtils.equals(resultStatus, "9000")) {
                // 该笔订单是否真实支付成功，需要依赖服务端的异步通知。
                Toast.makeText(activity, "支付成功", Toast.LENGTH_SHORT).show();
            } else {
                // 该笔订单真实的支付结果，需要依赖服务端的异步通知。
                Toast.makeText(activity, "支付失败", Toast.LENGTH_SHORT).show();
            }
        }

    }

}
