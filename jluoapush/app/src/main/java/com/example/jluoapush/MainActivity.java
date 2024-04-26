package com.example.jluoapush;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Person;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.CalendarView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {
    Handler mTimeHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            if (msg.what == 0) {
                content.setText(outOa); //View.ininvalidate()
                sendEmptyMessageDelayed(0, 1000);
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //文本框加载
        content = (TextView) findViewById(R.id.content);
        mTimeHandler.sendEmptyMessageDelayed(0, 500);
        //日历
        CalendarView calendarview = (CalendarView) findViewById(R.id.calendarView);
        calendarview.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {
                String month_f = String.format("%02d",month+1);
                String day_f = String.format("%02d",dayOfMonth);
                choiceTime = year+"-"+month_f+"-"+day_f;
                choiceTimeOperate = "1";
                Toast.makeText(MainActivity.this,"您选择的时间是："+ year + "年" + month_f + "月" + day_f + "日",Toast.LENGTH_SHORT).show();
            }
        });
        //内置浏览器
        webView = (WebView) findViewById(R.id.oa_webView);
        // 开启 localStorage
        webView.getSettings().setDomStorageEnabled(true);
        // 设置支持javascript
        webView.getSettings().setJavaScriptEnabled(true);
        // 设置缓存模式
        webView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
        //使用自定义的WebViewClient
        webView.setWebViewClient(new WebViewClient());

    }

    private WebView webView;
    private TextView content;  //  显示内容的 TextView
    public static MainActivity mMainActivity;
    String choiceTime="sxy";
    String outOa;
    String line;
    String choiceTimeOperate = "-1";
    // 进行Unicode与中文相互转换
    public static String unicodeDecode(String string) {
        Pattern pattern = Pattern.compile("(\\\\u(\\p{XDigit}{4}))");
        Matcher matcher = pattern.matcher(string);
        char ch;
        while (matcher.find()) {
            ch = (char) Integer.parseInt(matcher.group(2), 16);
            string = string.replace(matcher.group(1), ch + "");
        }
        return string;
    }
    public void btn_1(View v)
    {
        //绑定的btn_1方法
        Toast.makeText(getApplicationContext(), "正在获取中...", Toast.LENGTH_LONG).show();
        //网络请求需要在子线程中完成
        new Thread(() -> {
            String data_post;
            LocalDate date = LocalDate.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            if (Objects.equals(choiceTimeOperate.toString(), "-1")){
                data_post = date.format(formatter);
            }
            else {
                data_post = choiceTime;
            }
            URLRequest request = new URLRequest();
            String data = "";//POST请求的参数
            // 调用我们写的get方法
            String res = request.get("https://post.oapush.com/time/"+data_post);
            //String res = request.get("https://post.oapush.com/time/2024-04-25");
            if (Objects.equals(res, "-1")){
                Log.d("error","请求失败");
                line = "请求失败";
                Looper.prepare();
                Toast.makeText(getBaseContext(), "请求失败，请检查网络环境后重新请求！", Toast.LENGTH_LONG).show();
                Looper.loop();
            }
            else {
                Log.d("success",res);
                String line_1 = res.replace("{","");
                String line_2 = line_1.replace("}","");
                String line_3 = line_2.replace(":","");
                String line_4 = line_3.replace('"',' ');
                String line_5 = line_4.replace("body","");
                line = line_5.replace(" ","");
                line = unicodeDecode(line);
                String[] data_post_list = data_post.split("-");
                if (Objects.equals(line, "当日没有发布校内通知")){
                    outOa="学校在"+data_post_list[0]+"年"+data_post_list[1]+"月"+data_post_list[2]+"日"+"没有发布校内通知";
                }
                else {
                    String[] oaArray;
                    oaArray = line.split("🌙");
                    List<String> oaUrl = new ArrayList<String>();
                    String[] oaList;
                    outOa = "";
                    int year_new = Integer.parseInt(data_post_list[0])*100000;
                    for (int i = 0; i < oaArray.length; i++) {
                        oaList = oaArray[i].split("⭐");
                        oaUrl.add(oaList[2]);
                        int oa_new = year_new+Integer.parseInt(oaUrl.get(i));
                        String oa_hex = Integer.toHexString(oa_new);
                        outOa += oaList[1] + System.getProperty("line.separator") + "发布单位："
                                + oaList[0] + System.getProperty("line.separator")+
                                "https://api.jluer.cn/oa/oa.php?"+oa_hex
                                + System.getProperty("line.separator")+System.getProperty("line.separator");
                        //System.out.println("https://post.oapush.com/oa/2024/" + oaUrl.get(i) + " ");
                    }
                    //line = line.replace("🌙","\n");
                    // strArr= line.split("🌙");
                    Looper.prepare();
                    Toast.makeText(getBaseContext(), "校内通知获取成功！", Toast.LENGTH_LONG).show();
                    Looper.loop();
                }
                Log.d("success", outOa);
            }
        }).start();
        //Toast.makeText(getApplicationContext(), "校内通知获取结束", Toast.LENGTH_LONG).show();
        show();
    }
    void show(){  // 刷新 TextView 内容
        content.setText(outOa);
    }
}