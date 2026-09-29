package com.hwj.recharge;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    // 三大运营商 / 广电 充值入口配置
    private static final String[] NAMES = {"中国移动", "中国联通", "中国电信", "中国广电"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ScrollView scroll = new ScrollView(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        int pad = dp(20);
        root.setPadding(pad, pad, pad, pad);
        scroll.addView(root);

        TextView title = new TextView(this);
        title.setText("一键充值");
        title.setTextSize(24);
        title.setGravity(Gravity.CENTER);
        root.addView(title);

        TextView tip = new TextView(this);
        tip.setText("点击直接跳转对应运营商充值页");
        tip.setTextSize(13);
        tip.setTextColor(Color.GRAY);
        tip.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams tipLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tipLp.topMargin = dp(8);
        tipLp.bottomMargin = dp(16);
        root.addView(tip, tipLp);

        addButton(root, "中国移动", new Runnable() {
            public void run() { openChinaMobile(); }
        });
        addButton(root, "中国联通", new Runnable() {
            public void run() { openChinaUnicom(); }
        });
        addButton(root, "中国电信", new Runnable() {
            public void run() { openChinaTelecom(); }
        });
        addButton(root, "中国广电", new Runnable() {
            public void run() { openChinaBroadnet(); }
        });

        TextView note = new TextView(this);
        note.setText("说明：优先直达 App 内充值页；若未安装或版本不支持，自动回退到运营商 App 首页 / 官方网页。");
        note.setTextSize(12);
        note.setTextColor(Color.GRAY);
        LinearLayout.LayoutParams noteLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        noteLp.topMargin = dp(20);
        root.addView(note, noteLp);

        setContentView(scroll);
    }

    private void addButton(LinearLayout parent, String text, final Runnable action) {
        Button b = new Button(this);
        b.setText(text);
        b.setTextSize(18);
        b.setAllCaps(false);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(64));
        lp.bottomMargin = dp(14);
        parent.addView(b, lp);
        b.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                action.run();
            }
        });
    }

    // ---------------- 中国移动 ----------------
    private void openChinaMobile() {
        // 1) 移动营业厅 App 充值页
        if (tryStartExplicit("com.greenpoint.android.mc10086.activity",
                "com.greenpoint.android.mc10086.activity.MainActivity")) {
            return;
        }
        // 2) 移动 App 首页
        if (tryLaunchPackage("com.greenpoint.android.mc10086.activity")) {
            toast("已打开中国移动 App，请点「充值交费」");
            return;
        }
        // 3) 网页兜底
        openWeb("https://shop.10086.cn/recharge/");
    }

    // ---------------- 中国联通 ----------------
    private void openChinaUnicom() {
        if (tryStartExplicit("com.sinovatech.unicom.ui",
                "com.sinovatech.unicom.ui.WelcomeClient")) {
            return;
        }
        if (tryLaunchPackage("com.sinovatech.unicom.ui")) {
            toast("已打开中国联通 App，请点「交费充值」");
            return;
        }
        openWeb("https://upay.10010.com/npfweb/npfcellweb/phone_recharge_fill.htm");
    }

    // ---------------- 中国电信 ----------------
    private void openChinaTelecom() {
        // 电信营业厅 App
        if (tryLaunchPackage("com.ct.client")) {
            toast("已打开中国电信 App，请点「充值交费」");
            return;
        }
        openWeb("https://www.189.cn/recharge/");
    }

    // ---------------- 中国广电 ----------------
    private void openChinaBroadnet() {
        // 广电暂无独立线上下载量级的充值 App，走支付宝小程序深链，失败退支付宝
        String[] appIds = {"2021004100000000", "200011235"};
        for (String id : appIds) {
            Intent i = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("alipays://platformapi/startapp?appId=" + id));
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (i.resolveActivity(getPackageManager()) != null) {
                try {
                    startActivity(i);
                    return;
                } catch (Exception ignored) {
                }
            }
        }
        if (tryLaunchPackage("com.eg.android.AlipayGphone")) {
            toast("已打开支付宝，请搜索「中国广电」充值");
            return;
        }
        openWeb("https://www.10099.com.cn/");
    }

    // ---------------- 工具方法 ----------------

    private boolean tryStartExplicit(String pkg, String cls) {
        Intent i = new Intent();
        i.setComponent(new ComponentName(pkg, cls));
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (i.resolveActivity(getPackageManager()) == null) return false;
        try {
            startActivity(i);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean tryLaunchPackage(String pkg) {
        Intent i = getPackageManager().getLaunchIntentForPackage(pkg);
        if (i == null) return false;
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            startActivity(i);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void openWeb(String url) {
        Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (i.resolveActivity(getPackageManager()) != null) {
            startActivity(i);
        } else {
            toast("未找到可用的充值入口，请手动打开对应 App");
        }
    }

    private void toast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }

    private int dp(int v) {
        return (int) (v * getResources().getDisplayMetrics().density + 0.5f);
    }
}