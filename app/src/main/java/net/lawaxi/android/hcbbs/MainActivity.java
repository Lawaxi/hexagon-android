package net.lawaxi.android.hcbbs;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Looper;
import android.util.JsonReader;
import android.util.LogPrinter;
import android.view.MotionEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;

import net.lawaxi.android.hcbbs.api.httprequest;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    //public static boolean isLogin = false;
    //public static String mypage = "http://www.lawaxi.net/";
    public static WebView web;
    public static FloatingActionButton fab;
    public static MainActivity instance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        instance = this;

        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        web = findViewById(R.id.webView);
        web.getSettings().setUseWideViewPort(false);
        web.getSettings().setJavaScriptEnabled(true);
        web.getSettings().setDomStorageEnabled(true);
        web.getSettings().setDatabaseEnabled(true);
        web.loadUrl("http://www.lawaxi.net/");

        fab = findViewById(R.id.share);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent textIntent = new Intent(Intent.ACTION_SEND);
                textIntent.setType("text/plain");
                textIntent.putExtra(Intent.EXTRA_TEXT, web.getUrl());
                startActivity(Intent.createChooser(textIntent, "分享本页面"));
            }
        });
        fab.setOnTouchListener(new View.OnTouchListener() {

            private float recordx;
            private float recordy;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction()==MotionEvent.ACTION_DOWN){
                    recordx = event.getX();
                    recordy = event.getY();
                }else{
                    fab.offsetLeftAndRight((int)(event.getX()-recordx));
                    fab.offsetTopAndBottom((int)(event.getY()-recordy));
                }
                return true;
            }
        });

       /*
        web.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                web.loadUrl("javascript:window.java_obj.onHtml('<head>'+document.getElementsByTagName('html')[0].innerHTML+'</head>');");
            }
        });

        web.addJavascriptInterface(new Object(){
            @JavascriptInterface // 要加这个注解，不然调用不到
            public void onHtml(String html) {
                if(Jsoup.parseBodyFragment(html).body().hasClass("App affix App--index scrolled")){
                    freshLogin(Jsoup.parseBodyFragment(html).body().getElementsByClass("App affix App--index scrolled")
                            .get(0).getElementsByClass("App-drawer").get(0)
                            .getElementsByClass("App-header").get(0).getElementsByClass("container")
                            .get(0).getElementsByClass("Header-secondary").get(0)
                            .getElementsByClass("Header-controls").get(0));
                }
            }
        }, "java_obj");

        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "社区未加载，没联网? :(", Snackbar.LENGTH_LONG)
                        .setAction("提示", null).show();
            }
        });*/

        final ArrayList<String> ret = new ArrayList<>();
        Thread a = new Thread(new Runnable(){
            @Override
            public void run() {
                try {
                    String latest_version = httprequest.testGetHtml("http://121.37.27.192/data/hcapp.html");
                    System.out.println(latest_version);

                    if(latest_version.equals(""))
                        return;

                    if(latest_version.startsWith("beta-")){
                        if (!BuildConfig.VERSION_NAME.startsWith("beta-"))
                            return ;

                        if (Integer.valueOf(latest_version.substring(5)) <= Integer.valueOf(BuildConfig.VERSION_NAME.substring(5))) {
                            return;
                        }
                    }else{
                        if (!BuildConfig.VERSION_NAME.startsWith("beta-")) {
                            if (Integer.valueOf(latest_version) <= Integer.valueOf(BuildConfig.VERSION_NAME)) {
                                return;
                            }
                        }
                    }

                    ret.add(latest_version) ;
                    ret.add(httprequest.testGetHtml("http://121.37.27.192/data/hcappc.html"));

                }catch (Exception e){

                    e.printStackTrace();
                }
            }
        });
        try {
            a.start();
            a.join();

            if(ret.size()==2){
                new AlertDialog.Builder(instance)
                        .setTitle("由 "+BuildConfig.VERSION_NAME+" 更新至 "+ret.get(0))
                        .setMessage("更新介绍: " +ret.get(1))
                        .setPositiveButton("是", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                web.loadUrl("https://github.com/Lawaxi/hexagon-android/releases");
                            }
                        })
                        .setNegativeButton("否", null)
                        .show();
            }


        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }

    /*
    public static void freshLogin(Element a){

        if(isLogin){
            if(!a.hasClass("item-session")){
                isLogin = false;
                fab.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Snackbar.make(view, "请先登录 :)", Snackbar.LENGTH_LONG)
                                .setAction("提示", null).show();
                    }
                });
            }
        }else{
            if(a.hasClass("item-session")){
                isLogin = true;
                Element b = a.getElementsByClass("item-session")
                        .get(0).getElementsByClass("ButtonGroup Dropdown dropdown SessionDropdown itemCount4").get(0)
                        .getElementsByClass("Dropdown-toggle Button Button--user Button--flat").get(0);
                mypage = "http://www.lawaxi.net/u/"+b.getElementsByClass("Button-label").get(0).getElementsByClass("username").get(0).toString()+"/";
                fab.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        web.loadUrl(mypage);
                    }
                });
            }
        }
    }*/


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement

        switch (id){
            case R.id.main:
                web.loadUrl("http://www.lawaxi.net/");
                return true;

            case R.id.announce:
                web.loadUrl("http://www.lawaxi.net/t/announcements");
                return true;

            case R.id.page2:
                web.loadUrl("http://www.lawaxi.net/t/vocabulary");
                return true;

            case R.id.page3:
                web.loadUrl("http://www.lawaxi.net/t/science");
                return true;

            case R.id.page4:
                web.loadUrl("http://www.lawaxi.net/t/xijinpings");
                return true;

            case R.id.page5:
                web.loadUrl("http://www.lawaxi.net/t/articles");
                return true;

            case R.id.out:
            {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(web.getUrl()));
                startActivity(intent);
                return true;
            }
            case R.id.report:
                web.loadUrl("http://www.lawaxi.net/d/54");
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }

    }
}
