package hk.edu.hkmu.hksi;

import android.os.Bundle;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;

public class SchoolWebViewActivity extends AppCompatActivity {
    // 对应Lab03的EXTRA_MESSAGE
    public static final String EXTRA_WEBSITE = "EXTRA_WEBSITE";
    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_school_web_view);

        webView = findViewById(R.id.webview);

        // 1. 获取传过来的官网地址（Lab03写法）
        String website = getIntent().getStringExtra(EXTRA_WEBSITE);

        // 2. 开启JS（Lab03必写）
        webView.getSettings().setJavaScriptEnabled(true);

        // 3. 不跳外部浏览器，完全照搬Lab03的WebViewClient
        webView.setWebViewClient(new WebViewClient() {
            // 兼容新旧版本
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                view.loadUrl(request.getUrl().toString());
                return true;
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });

        // 4. 加载网址
        if (website != null && !website.isEmpty()) {
            webView.loadUrl(website);
        }
    }
}