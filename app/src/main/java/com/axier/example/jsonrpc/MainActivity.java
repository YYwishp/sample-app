package com.axier.example.jsonrpc;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.axier.example.jsonrpc.azazar.bitcoin.jsonrpcclient.Bitcoin;
import com.axier.example.jsonrpc.azazar.bitcoin.jsonrpcclient.BitcoinAcceptor;
import com.axier.example.jsonrpc.azazar.bitcoin.jsonrpcclient.BitcoinException;
import com.axier.example.jsonrpc.azazar.bitcoin.jsonrpcclient.BitcoinJSONRPCClient;
import com.axier.example.jsonrpc.azazar.bitcoin.jsonrpcclient.ConfirmedPaymentListener;
import com.axier.jsonrpclibrary.JSONRPCClient;
import com.axier.jsonrpclibrary.JSONRPCException;
import com.axier.jsonrpclibrary.JSONRPCParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public String URL = "";
    public String EXAMPLE_SUCCESS_CALL = "https://34.232.72.129:8832";
    public String EXAMPLE_ERROR_CALL = "https://raw.githubusercontent.com/axierjhtjz/android-json-rpc/master/error.json";

    /**
     * Just a example of how to use it against a WS. In this case we are just fetching data from the
     * url above.
     */
    public String EXAMPLE_METHOD_NAME = "login";
    public String EXAMPLE_PARAM_1 = "user";
    public String EXAMPLE_PARAM_2 = "password";

    public TextView mResponseArea;
    private static Bitcoin bitcoin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        Authenticator
        mResponseArea = (TextView) findViewById(R.id.response_area);

        Button successBtn = (Button) findViewById(R.id.success_btn);
        Button errorBtn = (Button) findViewById(R.id.error_btn);

        successBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bitcoin = new BitcoinJSONRPCClient();
                new Thread(new Runnable() {
                    @Override
                    public void run() {


                        try {


                            Bitcoin.Info info = bitcoin.getInfo();
                            final String s = info.toString();

//                            List<Bitcoin.Unspent> unspents = bitcoin.listUnspent(1, 99999);
                            String hex = "010000000001014be5393e4ee79bde40558f637679ab04a9a90a02409a927d3a542e46537e5e05000000006a473044022034b35843bac9bac485724b042ca31f6879e3766a3540402a437bf5bb5fd7e4ea02205149a8e1c3704f80814b8c6d69d646221cab6c8eab42fb14e3279ab1a7424427012102a2af8bbb39a0b66eb3b26436133a8becbde5dad297f02c238280ae78721f0210ffffffff02002d3101000000001976a914f65af6aaeb109376da8cbbcd4ed6c4bc9a0ff1bf88ac7840ae02000000001976a91467cfec849adc3fa8f3a4c692f39b79e9a4b6229f88ac0000000000";
                            final String s1 = bitcoin.sendRawTransaction(hex);

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mResponseArea.setText(s1);
                                }
                            });
                        } catch (BitcoinException e) {
                            e.printStackTrace();
                        }

                    }
                }).start();
            }
        });

        errorBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                URL = EXAMPLE_ERROR_CALL;
                new MakeJSONRpcCallTask().execute();
            }
        });





    }




    public static void sendCoins() throws BitcoinException {
        bitcoin.sendToAddress("1EzGDMdqKW5ubTDNHSqCKciPkybGSvWgrj", 10);
    }

    public static void receiveCoins() throws BitcoinException {
        final BitcoinAcceptor acceptor = new BitcoinAcceptor(bitcoin);

        System.out.println("Send bitcoins to " + bitcoin.getNewAddress("NewAccount"));

        acceptor.addListener(new ConfirmedPaymentListener() {
            HashSet processed = new HashSet();

            @Override
            public void confirmed(Bitcoin.Transaction transaction) {
                if (!processed.add(transaction.txId()))
                    return; // already processed

                System.out.println("Payment received, amount: " + transaction.amount() + "; account: " + transaction.account());
                try {
                    if (bitcoin.getBalance("NewAccount") >= 10)
                        acceptor.stopAccepting();
                } catch (BitcoinException ex) {
                    ex.printStackTrace();
                }
            }

        });
        acceptor.run();
    }

    public class MakeJSONRpcCallTask extends AsyncTask<Void, Void, JSONObject> {
        @Override
        protected JSONObject doInBackground(Void... params) {
            JSONRPCClient client = JSONRPCClient.create(URL, JSONRPCParams.Versions.VERSION_2);
            client.setConnectionTimeout(2000);
            client.setSoTimeout(2000);
            try {
                JSONObject jsonObj = new JSONObject();
                jsonObj.put(EXAMPLE_PARAM_1, "myuser");
                jsonObj.put(EXAMPLE_PARAM_2, "mypassword");
                return client.callJSONObject(EXAMPLE_METHOD_NAME, jsonObj);
            } catch (JSONRPCException rpcEx) {
                rpcEx.printStackTrace();
            } catch (JSONException jsEx) {
                jsEx.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            super.onPostExecute(result);
            if (result != null && mResponseArea != null) {
                mResponseArea.setText(result.toString());
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }
}