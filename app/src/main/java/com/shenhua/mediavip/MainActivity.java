package com.shenhua.mediavip;

import android.app.AlertDialog;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.umeng.analytics.MobclickAgent;
import com.umeng.update.UmengUpdateAgent;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {
    private List<String> nkResults = new ArrayList<>();
    private static List<Map<String, Object>> resultData = new ArrayList<>();
    private int listCount, line = 1;
    private static Boolean isExit = false;
    private static final int COPY = 0, SEND = 1;
    private AlertDialog alertDialog;
    private ListView main_list;
    private TextView main_tv_empty;
    private TextView main_tv_resent;
    private WebView web;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        intView();
        isFirstRun();
        UmengUpdateAgent.update(this);
    }

    private void isFirstRun() {
        SharedPreferences sp = getSharedPreferences("firstrun", MODE_APPEND);
        if (sp.getBoolean("is", true)) {//默认是第一次启动
            final AlertDialog alertDialog = new AlertDialog.Builder(
                    MainActivity.this).create();
            alertDialog.setCancelable(false);
            alertDialog.show();
            Window window = alertDialog.getWindow();
            window.setContentView(R.layout.dialog_main_info);
            TextView tv_title = (TextView) window
                    .findViewById(R.id.tv_dialog_title);
            Button btn_ok = (Button) window.findViewById(R.id.btn_dia_ok);
            Button btn_cacle = (Button) window.findViewById(R.id.btn_dia_cacle);
            tv_title.setText("使用协议--声明");
            TextView tv_message = (TextView) window
                    .findViewById(R.id.tv_dialog_message);
            Utils agree = new Utils(this);
            tv_message.setText(agree.readAgree());
            btn_ok.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    alertDialog.dismiss();
                    checkNet();
                    SharedPreferences sp1 = getSharedPreferences("firstrun",
                            MODE_APPEND);
                    SharedPreferences.Editor editor = sp1.edit();
                    editor.putBoolean("is", false);
                    editor.apply();
                }
            });
            btn_cacle.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    alertDialog.dismiss();
                    MainActivity.this.finish();
                    System.exit(0);
                }
            });

        } else {
            checkNet();
        }

    }

    private void checkNet() {
        Utils netUtils = new Utils(this);
        if (netUtils.isNetworkAvailable()) {
            main_tv_empty.setVisibility(View.GONE);
            showprogerssdialog();
        } else {
            noNet();
        }
    }

    private void noNet() {
        Toast.makeText(MainActivity.this, "无网络连接，请重试！", Toast.LENGTH_LONG)
                .show();
        main_tv_empty.setVisibility(View.VISIBLE);

    }

    private void showprogerssdialog() {
        alertDialog = new AlertDialog.Builder(MainActivity.this,
                R.style.AlertDialog).create();
        alertDialog.show();
        Window window = alertDialog.getWindow();
        window.setContentView(R.layout.dialog_main_progerss);
        ImageView iv = (ImageView) window.findViewById(R.id.pro_dia_iv);
        Animation am = AnimationUtils
                .loadAnimation(this, R.anim.dialog_loading);
        am.setFillAfter(true);
        iv.setAnimation(am);
        TextView tv = (TextView) window.findViewById(R.id.pro_dia_tv);
        tv.setText("正在初始化...");
        GetSource gSource = new GetSource(this, web, tv);
        gSource.getHtmlSource(line);
    }

    public class JSInterface {
        public void showBody(String data) {
            try {
                Utils http = new Utils(MainActivity.this);
                http.writeFileData("VIP.txt", data);
            } catch (Exception e) {
                Log.e("", "showBody: error");
            }
            String[] nks;
            if (line == 1) {
                Log.e("line", "1");
                Pattern p = Pattern
                        .compile("document.write.*?\\n</script>(.*?)\\n");
                Matcher m = p.matcher(data);
                nkResults.clear();
                resultData.clear();
                while (m.find()) {
                    MatchResult mr = m.toMatchResult();
                    String str = mr.group(1);
                    str = str.replaceAll("<br>", "");
                    nkResults.add(str);
                    System.out.println("line1---->" + str);
                }
                listCount = nkResults.size();
                for (int i = 0; i < listCount; i++) {
//					nks = nkResults.get(i).split("密码:");
//					Map<String, Object> map = new HashMap<String, Object>();
//					for (int j = 0; j < nks.length; j++) {
//						map.put("vipnumbers", nks[0].replace("账号:", ""));
//						map.put("vipkeys", nks[1]);
//					}
                    if (nkResults.get(i).contains("----")) {
                        System.out.println("yes");
                        nks = nkResults.get(i).split("----");
                        System.out.println(i);
                        Map<String, Object> map = new HashMap<>();
                        for (String nk : nks) {
                            map.put("vipnumbers", nks[0]);
                            map.put("vipkeys", nks[1]);
                            System.out.println("__" + nks[0] + "__" + nk);
                        }
                        resultData.add(map);
                    }
                }
            } else {
                Log.e("line", "2");
                Pattern p = Pattern
                        .compile("document.write.*?\\n</script>(.*?)\\n");
                Matcher m = p.matcher(data);
                nkResults.clear();
                resultData.clear();
                while (m.find()) {
                    MatchResult mr = m.toMatchResult();
                    String str = mr.group(1);
                    str = str.replaceAll("<br>", "");
                    nkResults.add(str);
                    System.out.println("line2---->" + str);
                }
                listCount = nkResults.size();
                for (int i = 0; i < listCount; i++) {
                    nks = nkResults.get(i).split("----");
                    Map<String, Object> map = new HashMap<>();
                    for (String nk : nks) {
                        map.put("vipnumbers", nks[0]);
                        map.put("vipkeys", nks[1]);
                    }
                    resultData.add(map);
                }
            }
            startListHandler();
        }
    }

    private void startListHandler() {
        new Thread() {
            @Override
            public void run() {
                listHandler.sendEmptyMessage(0);
            }
        }.start();

    }

    private Handler listHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what >= 0) {
                alertDialog.dismiss();
                setListData();
                setAnim();
            } else {
                Toast.makeText(MainActivity.this, "获取失败！", Toast.LENGTH_LONG)
                        .show();
            }
        }

    };

    private void setListData() {
        main_list.invalidateViews();
        MyAdapter myAdapter = new MyAdapter(MainActivity.this);
        main_list.setAdapter(myAdapter);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date curDate = new Date(System.currentTimeMillis());
        String str = formatter.format(curDate);
        main_tv_resent.setText("更新于：" + str);
        SharedPreferences sp = getSharedPreferences("resenttime", MODE_APPEND);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("time", str);
        editor.apply();
    }

    private class MyAdapter extends BaseAdapter {
        private LayoutInflater mInflater;

        public MyAdapter(Context context) {
            this.mInflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return listCount;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.list_items, parent, false);
                holder = new ViewHolder();
                holder.item_layout = convertView.findViewById(R.id.item_layout);
                holder.number = (TextView) convertView.findViewById(R.id.item_tv_number);
                holder.pwd = (TextView) convertView.findViewById(R.id.item_tv_pwd);
                holder.copy = (Button) convertView.findViewById(R.id.item_btn_copy);
                holder.send = (Button) convertView.findViewById(R.id.item_btn_send);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.number.setText((String) resultData.get(position).get(
                    "vipnumbers"));
            holder.pwd
                    .setText((String) resultData.get(position).get("vipkeys"));
            if (position == 0) {
                holder.item_layout.setBackground(getResources().getDrawable(
                        R.drawable.card_00));
                holder.number.setTextColor(getResources().getColor(
                        R.color.card01));
                holder.pwd
                        .setTextColor(getResources().getColor(R.color.card01));
            }
            if (position == 1) {
                holder.item_layout.setBackground(getResources().getDrawable(
                        R.drawable.card_01));
                holder.number.setTextColor(getResources().getColor(
                        R.color.card02));
                holder.pwd
                        .setTextColor(getResources().getColor(R.color.card02));
            }
            if (position == 2) {
                holder.item_layout.setBackground(getResources().getDrawable(
                        R.drawable.card_02));
                holder.number.setTextColor(getResources().getColor(
                        R.color.card03));
                holder.pwd
                        .setTextColor(getResources().getColor(R.color.card03));
            }
            if (position == 3) {
                holder.item_layout.setBackground(getResources().getDrawable(
                        R.drawable.card_03));
                holder.number.setTextColor(getResources().getColor(
                        R.color.card04));
                holder.pwd
                        .setTextColor(getResources().getColor(R.color.card04));
            }
            if (position == 4) {
                holder.item_layout.setBackground(getResources().getDrawable(
                        R.drawable.card_04));
                holder.number.setTextColor(getResources().getColor(
                        R.color.card05));
                holder.pwd
                        .setTextColor(getResources().getColor(R.color.card05));
            }
            holder.send.setTag(position);
            holder.send.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendInfo(position, SEND);
                }
            });

            holder.copy.setTag(position);
            holder.copy.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendInfo(position, COPY);
                }
            });

            return convertView;
        }

    }

    @SuppressWarnings("deprecation")
    public void sendInfo(int position, int ty) {
        String number = (String) resultData.get(position).get("vipnumbers");
        String pwd = (String) resultData.get(position).get("vipkeys");

        String str = "我通过爱奇艺黄金会员获取器获得了这个会员号：\n" + "号码：" + number + "\n" + "密码："
                + pwd + "\n快来试试吧！";

        if (ty == COPY) {
            ClipboardManager cmb = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            cmb.setText(str);
            Toast.makeText(MainActivity.this, "已复制到剪切板", Toast.LENGTH_SHORT)
                    .show();

        }
        if (ty == SEND) {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TITLE, "爱奇艺黄金会员获取器");
            intent.putExtra(Intent.EXTRA_TEXT, str);
            Intent chooserIntent = Intent.createChooser(intent, "请选择一个要发送的应用：");
            if (chooserIntent == null) {
                return;
            }
            try {
                startActivity(chooserIntent);
            } catch (android.content.ActivityNotFoundException ex) {
                Toast.makeText(this, "不能完成发送！", Toast.LENGTH_SHORT).show();
            }
        }

    }

    public final class ViewHolder {
        public TextView number, pwd;
        public Button copy, send;
        public View item_layout;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            exitBy2Click();
        }
        return false;
    }

    private void exitBy2Click() {
        Timer tExit;
        if (!isExit) {
            isExit = true;
            Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
            tExit = new Timer();
            tExit.schedule(new TimerTask() {
                @Override
                public void run() {
                    isExit = false;
                }

            }, 2000);
        } else {
            this.finish();
            System.exit(0);
        }
    }

    private void intView() {
        main_list = (ListView) findViewById(R.id.main_list);
        web = (WebView) findViewById(R.id.web);
        Button refresh = (Button) findViewById(R.id.main_btn_refresh);
        Button btnline = (Button) findViewById(R.id.main_btn_line);
        main_tv_empty = (TextView) findViewById(R.id.main_tv_empty);
        main_tv_resent = (TextView) findViewById(R.id.main_tv_resent);
        SharedPreferences sp = getSharedPreferences("resenttime", MODE_APPEND);
        String str = sp.getString("time", "null");
        main_tv_resent.setText(str);
        WebSettings webSettings = web.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setBuiltInZoomControls(false);
        web.setDrawingCacheEnabled(true);
        web.setBackgroundColor(Color.TRANSPARENT);
        web.addJavascriptInterface(new JSInterface(), "javabody");
        WebClient wClient = new WebClient();
        web.setWebViewClient(wClient);
        assert refresh != null;
        refresh.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                checkNet();
            }

        });
        assert btnline != null;
        btnline.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (line == 1) {
                    Toast.makeText(MainActivity.this, "正在进行第二条路线！",
                            Toast.LENGTH_LONG).show();
                    line = 2;
                    checkNet();
                } else {
                    Toast.makeText(MainActivity.this, "正在进行第一条路线！",
                            Toast.LENGTH_LONG).show();
                    line = 1;
                    checkNet();
                }

            }
        });
        main_tv_empty.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                checkNet();

            }
        });

    }

    private void setAnim() {
        Animation animation = AnimationUtils.loadAnimation(this,
                R.anim.list_anim);
        LayoutAnimationController lac = new LayoutAnimationController(animation);
        lac.setOrder(LayoutAnimationController.ORDER_NORMAL);
        lac.setDelay(1);
        main_list.setLayoutAnimation(lac);
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

}
