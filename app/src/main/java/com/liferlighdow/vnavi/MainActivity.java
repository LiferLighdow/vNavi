package com.liferlighdow.vnavi;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.inputmethod.InputMethodManager;
import android.webkit.JavascriptInterface;
import android.webkit.URLUtil;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MainActivity extends Activity {

    private FrameLayout webViewContainer;
    private FrameLayout fullscreenContainer;
    private View customView;
    private WebChromeClient.CustomViewCallback customViewCallback;
    private static final String HOME_URL = "file:///android_asset/index.html";
    private static final String SETTINGS_URL = "file:///android_asset/settings.html";
    
    private View bottomControlArea;
    private LinearLayout bottomBar;
    private ProgressBar progressBar;
    private TextView urlText;
    private ImageView menuBtn;

    private View searchOverlay;
    private EditText searchInput;

    // Find in Page UI
    private View findBar;
    private EditText findInput;
    private TextView findCount;

    private boolean isAdBlockEnabled = false;
    private boolean isBarHidden = false;
    private String currentSearchEngine = "google";
    private String currentUserAgent = "default";
    private String barPosition = "bottom";
    private boolean isPwaMode = false;
    private long lastBackPressTime = 0;

    private List<WebView> tabList = new ArrayList<>();
    private int currentTabIndex = 0;
    private float pillTouchX = 0;

    private static final Map<String, String> SEARCH_URLS = new HashMap<>();
    static {
        SEARCH_URLS.put("google", "https://www.google.com/search?q=");
        SEARCH_URLS.put("searxng", "https://searx.be/search?q=");
        SEARCH_URLS.put("brave", "https://search.brave.com/search?q=");
        SEARCH_URLS.put("startpage", "https://www.startpage.com/do/search?q=");
        SEARCH_URLS.put("mojeek", "https://www.mojeek.com/search?q=");
        SEARCH_URLS.put("metager", "https://metager.org/meta/meta.ger3?eingabe=");
        SEARCH_URLS.put("duckduckgo", "https://duckduckgo.com/?q=");
        SEARCH_URLS.put("ecosia", "https://www.ecosia.org/search?q=");
        SEARCH_URLS.put("qwant", "https://www.qwant.com/?q=");
        SEARCH_URLS.put("bing", "https://www.bing.com/search?q=");
        SEARCH_URLS.put("yahoo", "https://search.yahoo.com/search?p=");
        SEARCH_URLS.put("yandex", "https://yandex.com/search/?text=");
        SEARCH_URLS.put("baidu", "https://www.baidu.com/s?wd=");
        SEARCH_URLS.put("naver", "https://search.naver.com/search.naver?query=");
        SEARCH_URLS.put("seznam", "https://search.seznam.cz/?q=");
    }

    private static final Set<String> AD_HOSTS = new HashSet<>();
    static {
        AD_HOSTS.add("doubleclick.net");
        AD_HOSTS.add("googleadservices.com");
        AD_HOSTS.add("googlesyndication.com");
        AD_HOSTS.add("taboola.com");
        AD_HOSTS.add("outbrain.com");
        AD_HOSTS.add("adservice.google.com");
        AD_HOSTS.add("amazon-adsystem.com");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = getSharedPreferences("vNavi", MODE_PRIVATE);
        isAdBlockEnabled = prefs.getBoolean("ad_block", false);
        currentSearchEngine = prefs.getString("engine", "google");
        currentUserAgent = prefs.getString("user_agent", "default");
        barPosition = prefs.getString("bar_position", "bottom");

        Window window = getWindow();
        boolean isNightMode = (getResources().getConfiguration().uiMode & android.content.res.Configuration.UI_MODE_NIGHT_MASK) == android.content.res.Configuration.UI_MODE_NIGHT_YES;

        if (isNightMode) {
            window.setStatusBarColor(Color.BLACK);
            window.setNavigationBarColor(Color.BLACK);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false);
        }

        setContentView(R.layout.activity_main);

        isPwaMode = getIntent().getBooleanExtra("is_pwa", false);
        String startUrl = isPwaMode ? getIntent().getStringExtra("pwa_url") : HOME_URL;
        if (startUrl == null) startUrl = HOME_URL;

        webViewContainer = findViewById(R.id.webview_container);
        fullscreenContainer = findViewById(R.id.fullscreen_container);
        bottomControlArea = findViewById(R.id.bottom_control_area);
        bottomBar = findViewById(R.id.bottom_bar);
        progressBar = findViewById(R.id.progress_bar);
        urlText = findViewById(R.id.url_text);
        menuBtn = findViewById(R.id.menu_btn);
        searchOverlay = findViewById(R.id.search_overlay);
        searchInput = findViewById(R.id.search_input);
        
        findBar = findViewById(R.id.find_bar);
        findInput = findViewById(R.id.find_input);
        findCount = findViewById(R.id.find_count);

        findViewById(R.id.root_view).setOnApplyWindowInsetsListener((v, insets) -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                android.graphics.Insets systemBars = insets.getInsets(WindowInsets.Type.systemBars());
                v.setPadding(0, systemBars.top, 0, 0);
                if ("top".equals(barPosition)) {
                    bottomControlArea.setPadding(0, 10, 0, 10);
                } else {
                    bottomControlArea.setPadding(0, 10, 0, systemBars.bottom + 20);
                }
                updateSystemBarIcons(window, isNightMode);
            }
            return insets;
        });

        setupBottomBar();
        setupSearchOverlay();
        setupPillSwipe();
        setupFindInPage();
        updateBarLayout();

        addNewTab(startUrl);
        if (isPwaMode) {
            isBarHidden = true;
            bottomBar.setTranslationY(500); // 初始隱藏
        }
    }

    private void installPwa() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            Toast.makeText(this, "Android 8.0+ required for this feature", Toast.LENGTH_SHORT).show();
            return;
        }

        WebView wv = getCurrentWebView();
        String url = wv.getUrl();
        String title = wv.getTitle();
        if (url == null || url.startsWith("file:///")) {
            Toast.makeText(this, "Cannot install this page", Toast.LENGTH_SHORT).show();
            return;
        }

        ShortcutManager shortcutManager = getSystemService(ShortcutManager.class);
        if (shortcutManager != null && shortcutManager.isRequestPinShortcutSupported()) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setAction(Intent.ACTION_VIEW);
            intent.putExtra("is_pwa", true);
            intent.putExtra("pwa_url", url);
            // 確保每次點擊都是開啟獨立的 PWA 實例
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);

            Bitmap iconBitmap = wv.getFavicon();
            Icon icon;
            if (iconBitmap != null) {
                icon = Icon.createWithBitmap(iconBitmap);
            } else {
                icon = Icon.createWithResource(this, R.mipmap.ic_launcher);
            }

            ShortcutInfo shortcutInfo = new ShortcutInfo.Builder(this, url)
                    .setShortLabel(title != null ? title : "vNavi Web App")
                    .setIcon(icon)
                    .setIntent(intent)
                    .build();

            shortcutManager.requestPinShortcut(shortcutInfo, null);
        }
    }

    private void updateBarLayout() {
        LinearLayout mainContent = findViewById(R.id.main_content);
        mainContent.removeView(bottomControlArea);
        if ("top".equals(barPosition)) {
            // 在 WebViewContainer 之前插入 (WebViewContainer 是 index 1, findBar 是 index 0)
            mainContent.addView(bottomControlArea, 1);
        } else {
            // 加到最後面
            mainContent.addView(bottomControlArea);
        }
        // 強制重新整理 Insets
        findViewById(R.id.root_view).requestApplyInsets();
    }

    private void setupFindInPage() {
        findInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                getCurrentWebView().findAllAsync(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        findViewById(R.id.find_prev).setOnClickListener(v -> getCurrentWebView().findNext(false));
        findViewById(R.id.find_next).setOnClickListener(v -> getCurrentWebView().findNext(true));
        findViewById(R.id.find_close).setOnClickListener(v -> {
            findBar.setVisibility(View.GONE);
            getCurrentWebView().clearMatches();
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(findInput.getWindowToken(), 0);
        });
    }

    private WebView createWebView() {
        WebView wv = new WebView(this);
        WebSettings ws = wv.getSettings();
        ws.setJavaScriptEnabled(true);
        ws.setDomStorageEnabled(true);
        ws.setAllowFileAccess(true);
        ws.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        
        wv.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, android.graphics.Bitmap favicon) {
                if (view == getCurrentWebView()) {
                    progressBar.setVisibility(View.VISIBLE);
                    updateUrlDisplay(url);
                    toggleBottomBar(true);
                }
            }
            @Override
            public void onPageFinished(WebView view, String url) {
                if (view == getCurrentWebView()) {
                    progressBar.setVisibility(View.GONE);
                    updateUrlDisplay(url);
                }
            }
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                if (!isAdBlockEnabled) return super.shouldInterceptRequest(view, request);
                String host = request.getUrl().getHost();
                if (host == null) return super.shouldInterceptRequest(view, request);
                for (String adHost : AD_HOSTS) {
                    if (host.contains(adHost)) return new WebResourceResponse("text/plain", "UTF-8", new ByteArrayInputStream("".getBytes()));
                }
                return super.shouldInterceptRequest(view, request);
            }
        });

        wv.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (view == getCurrentWebView()) progressBar.setProgress(newProgress);
            }

            @Override
            public void onShowCustomView(View view, CustomViewCallback callback) {
                if (customView != null) {
                    onHideCustomView();
                    return;
                }
                customView = view;
                customViewCallback = callback;
                
                fullscreenContainer.addView(customView);
                fullscreenContainer.setVisibility(View.VISIBLE);
                webViewContainer.setVisibility(View.GONE);
                bottomControlArea.setVisibility(View.GONE);
                findBar.setVisibility(View.GONE);
                searchOverlay.setVisibility(View.GONE);

                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                if (imm != null) imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    getWindow().getInsetsController().hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
                } else {
                    getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
                }
            }

            @Override
            public void onHideCustomView() {
                hideFullscreen();
            }
        });

        wv.setFindListener((activeMatchOrdinal, numberOfMatches, isDoneCounting) -> {
            findCount.setText((numberOfMatches > 0 ? (activeMatchOrdinal + 1) : 0) + "/" + numberOfMatches);
        });

        wv.addJavascriptInterface(new Object() {
            @JavascriptInterface public String getFavorites() { return getSharedPreferences("vNavi", MODE_PRIVATE).getString("favorites", "[]"); }
            @JavascriptInterface public void saveFavorites(String json) { getSharedPreferences("vNavi", MODE_PRIVATE).edit().putString("favorites", json).apply(); }
            @JavascriptInterface public void clearCache() { runOnUiThread(() -> { getCurrentWebView().clearCache(true); Toast.makeText(MainActivity.this, "Cache Cleared", Toast.LENGTH_SHORT).show(); }); }
            @JavascriptInterface public void toggleAdBlock(boolean enabled) { runOnUiThread(() -> { isAdBlockEnabled = enabled; getSharedPreferences("vNavi", MODE_PRIVATE).edit().putBoolean("ad_block", enabled).apply(); getCurrentWebView().reload(); }); }
            @JavascriptInterface public void toggleJS(boolean enabled) { runOnUiThread(() -> { getCurrentWebView().getSettings().setJavaScriptEnabled(enabled); getSharedPreferences("vNavi", MODE_PRIVATE).edit().putBoolean("js_enabled", enabled).apply(); getCurrentWebView().reload(); }); }
            @JavascriptInterface public void setSearchEngine(String engine) { runOnUiThread(() -> { currentSearchEngine = engine; getSharedPreferences("vNavi", MODE_PRIVATE).edit().putString("engine", engine).apply(); }); }
            @JavascriptInterface public void setUserAgent(String uaKey) { runOnUiThread(() -> { currentUserAgent = uaKey; getSharedPreferences("vNavi", MODE_PRIVATE).edit().putString("user_agent", uaKey).apply(); updateAllWebViewsUA(); }); }
            @JavascriptInterface public void setBarPosition(String pos) { runOnUiThread(() -> { barPosition = pos; getSharedPreferences("vNavi", MODE_PRIVATE).edit().putString("bar_position", pos).apply(); updateBarLayout(); }); }
        }, "Android");

        wv.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            wv.setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
                if (scrollY > oldScrollY + 10) toggleBottomBar(false);
                else if (scrollY < oldScrollY - 10) toggleBottomBar(true);
            });
        }

        // 核心：長按圖片下載功能
        wv.setOnLongClickListener(v -> {
            WebView.HitTestResult result = wv.getHitTestResult();
            if (result.getType() == WebView.HitTestResult.IMAGE_TYPE || 
                result.getType() == WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE) {
                
                String imageUrl = result.getExtra();
                if (imageUrl != null) {
                    new android.app.AlertDialog.Builder(this)
                        .setTitle("Save Image")
                        .setMessage("Do you want to download this image?")
                        .setPositiveButton("Download", (dialog, which) -> {
                            handleDownload(imageUrl, wv.getSettings().getUserAgentString(), null, null, 0);
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
                    return true;
                }
            }
            return false;
        });

        wv.setDownloadListener((url, userAgent, contentDisposition, mimetype, contentLength) -> {
            handleDownload(url, userAgent, contentDisposition, mimetype, contentLength);
        });

        return wv;
    }

    private void handleDownload(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
        try {
            // 處理 Data URL (Base64)
            if (url.startsWith("data:")) {
                Toast.makeText(this, "Base64 download not supported yet", Toast.LENGTH_SHORT).show();
                return;
            }

            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
            
            // 如果 mimetype 為空，嘗試從 URL 猜測
            if (mimetype == null || mimetype.isEmpty()) {
                mimetype = android.webkit.MimeTypeMap.getFileExtensionFromUrl(url);
                if (mimetype != null) {
                    mimetype = android.webkit.MimeTypeMap.getSingleton().getMimeTypeFromExtension(mimetype);
                }
            }
            
            if (mimetype != null) request.setMimeType(mimetype);
            
            // 獲取檔名
            String fileName = URLUtil.guessFileName(url, contentDisposition, mimetype);
            
            // 修正：如果 guessFileName 還是給了 .bin，但 URL 明明有副檔名
            if (fileName.endsWith(".bin") && url.contains(".")) {
                String urlPart = url.split("\\?")[0];
                String ext = urlPart.substring(urlPart.lastIndexOf("."));
                if (ext.length() <= 5) { // 避免抓到太長的非法字串
                    fileName = fileName.replace(".bin", ext);
                }
            }

            request.setTitle(fileName);
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);

            DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
            if (dm != null) dm.enqueue(request);
            Toast.makeText(this, "Download started", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {}
    }

    private void addNewTab(String url) {
        WebView wv = createWebView();
        tabList.add(wv);
        switchTab(tabList.size() - 1);
        wv.loadUrl(url);
    }

    private void switchTab(int index) {
        if (index < 0 || index >= tabList.size()) return;
        currentTabIndex = index;
        webViewContainer.removeAllViews();
        WebView current = tabList.get(index);
        webViewContainer.addView(current, new FrameLayout.LayoutParams(-1, -1));
        updateUrlDisplay(current.getUrl());
        updateWebViewUA(current);
    }

    private WebView getCurrentWebView() { return tabList.get(currentTabIndex); }

    private void updateAllWebViewsUA() { for (WebView wv : tabList) updateWebViewUA(wv); }

    private void updateWebViewUA(WebView wv) {
        WebSettings ws = wv.getSettings();
        switch (currentUserAgent) {
            case "iphone": ws.setUserAgentString("Mozilla/5.0 (iPhone; CPU iPhone OS 17_4 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.4 Mobile/15E148 Safari/604.1"); break;
            case "android_tablet": ws.setUserAgentString("Mozilla/5.0 (Linux; Android 10; SM-T870) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.6261.119 Safari/537.36"); break;
            case "desktop_chrome": ws.setUserAgentString("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36"); break;
            case "desktop_safari": ws.setUserAgentString("Mozilla/5.0 (Macintosh; Intel Mac OS X 14_4_1) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.4.1 Safari/605.1.15"); break;
            default: ws.setUserAgentString(null); break;
        }
    }

    private void setupPillSwipe() {
        bottomBar.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN: 
                    pillTouchX = event.getX(); 
                    break;
                case MotionEvent.ACTION_UP:
                    float diffX = event.getX() - pillTouchX;
                    if (Math.abs(diffX) > 100) {
                        // 這是滑動：切換分頁
                        if (diffX > 0) switchTab(currentTabIndex - 1);
                        else switchTab(currentTabIndex + 1);
                    } else {
                        // 這是點擊：觸發搜尋 UI
                        showSearchUI();
                    }
                    break;
            }
            return true;
        });
    }

    private void updateSystemBarIcons(Window window, boolean isNightMode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowInsetsController controller = window.getInsetsController();
            if (controller != null) {
                if (isNightMode) controller.setSystemBarsAppearance(0, WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS | WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS);
                else controller.setSystemBarsAppearance(WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS | WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS, WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS | WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS);
            }
        }
    }

    private void setupBottomBar() { menuBtn.setOnClickListener(v -> showCustomMenu(v)); }

    private void showCustomMenu(View anchor) {
        View menuView = LayoutInflater.from(this).inflate(R.layout.menu_custom, null);
        PopupWindow popupWindow = new PopupWindow(menuView, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, true);
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popupWindow.setElevation(20);

        WebView wv = getCurrentWebView();
        menuView.findViewById(R.id.menu_back).setOnClickListener(v -> { if (wv.canGoBack()) wv.goBack(); popupWindow.dismiss(); });
        menuView.findViewById(R.id.menu_forward).setOnClickListener(v -> { if (wv.canGoForward()) wv.goForward(); popupWindow.dismiss(); });
        menuView.findViewById(R.id.menu_refresh).setOnClickListener(v -> { wv.reload(); popupWindow.dismiss(); });
        menuView.findViewById(R.id.menu_home).setOnClickListener(v -> { wv.loadUrl(HOME_URL); popupWindow.dismiss(); });
        
        menuView.findViewById(R.id.menu_tabs).setOnClickListener(v -> { addNewTab(HOME_URL); popupWindow.dismiss(); });
        menuView.findViewById(R.id.menu_tabs).setOnLongClickListener(v -> { popupWindow.dismiss(); showTabListMenu(anchor); return true; });

        // Find in page 觸發
        menuView.findViewById(R.id.menu_find).setOnClickListener(v -> {
            popupWindow.dismiss();
            findBar.setVisibility(View.VISIBLE);
            findInput.requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.showSoftInput(findInput, InputMethodManager.SHOW_IMPLICIT);
        });

        menuView.findViewById(R.id.menu_add_bookmark).setOnClickListener(v -> {
            String title = wv.getTitle(); String url = wv.getUrl();
            // 修正：同時排除首頁與設定頁
            if (url != null && !url.equals(HOME_URL) && !url.equals(SETTINGS_URL)) {
                try {
                    SharedPreferences prefs = getSharedPreferences("vNavi", MODE_PRIVATE);
                    JSONArray arr = new JSONArray(prefs.getString("favorites", "[]"));
                    JSONObject obj = new JSONObject(); obj.put("name", title != null ? title : "New Site"); obj.put("url", url);
                    arr.put(obj); prefs.edit().putString("favorites", arr.toString()).apply();
                    Toast.makeText(this, "Added to Favorites", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {}
            }
            popupWindow.dismiss();
        });

        menuView.findViewById(R.id.menu_downloads).setOnClickListener(v -> { startActivity(new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS)); popupWindow.dismiss(); });
        
        menuView.findViewById(R.id.menu_install).setOnClickListener(v -> {
            installPwa();
            popupWindow.dismiss();
        });

        menuView.findViewById(R.id.menu_settings).setOnClickListener(v -> { wv.loadUrl(SETTINGS_URL); popupWindow.dismiss(); });

        menuView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        int yOffset = -(menuView.getMeasuredHeight() + anchor.getHeight() + 20);
        popupWindow.showAsDropDown(anchor, -(menuView.getMeasuredWidth() - anchor.getWidth()), yOffset);
    }

    private void showTabListMenu(View anchor) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setBackgroundResource(R.drawable.pill_background);
        layout.setPadding(10, 10, 10, 10);
        for (int i = 0; i < tabList.size(); i++) {
            final int index = i; WebView wv = tabList.get(i);
            LinearLayout row = new LinearLayout(this); row.setPadding(20, 20, 20, 20); row.setGravity(android.view.Gravity.CENTER_VERTICAL);
            TextView title = new TextView(this); String t = wv.getTitle(); if (t == null || t.isEmpty()) t = wv.getUrl(); if (t == null || t.equals(HOME_URL)) t = "Home";
            title.setText(t); title.setTextColor(Color.WHITE); title.setMaxLines(1); title.setEllipsize(android.text.TextUtils.TruncateAt.END); title.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 1));
            title.setOnClickListener(v -> switchTab(index));
            TextView close = new TextView(this); close.setText(" × "); close.setTextColor(Color.RED); close.setTextSize(20); close.setPadding(20, 0, 10, 0);
            close.setOnClickListener(v -> { removeTab(index); showTabListMenu(anchor); });
            row.addView(title); if (tabList.size() > 1) row.addView(close);
            layout.addView(row);
        }
        PopupWindow pw = new PopupWindow(layout, 600, LinearLayout.LayoutParams.WRAP_CONTENT, true);
        pw.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT)); pw.setElevation(25);
        layout.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        int yOffset = -(layout.getMeasuredHeight() + anchor.getHeight() + 30);
        pw.showAsDropDown(anchor, -(600 - anchor.getWidth()), yOffset);
    }

    private void removeTab(int index) {
        if (tabList.size() <= 1) return;
        WebView wv = tabList.get(index); tabList.remove(index); wv.destroy();
        if (currentTabIndex >= tabList.size()) switchTab(tabList.size() - 1);
        else if (currentTabIndex == index) switchTab(currentTabIndex);
        else if (currentTabIndex > index) currentTabIndex--;
    }

    private void setupSearchOverlay() {
        searchOverlay.setOnClickListener(v -> hideSearchUI());
        searchInput.setOnEditorActionListener((v, actionId, event) -> {
            String text = searchInput.getText().toString().trim();
            if (!text.isEmpty()) {
                if (text.contains(".") && !text.contains(" ")) getCurrentWebView().loadUrl(text.startsWith("http") ? text : "https://" + text);
                else getCurrentWebView().loadUrl(SEARCH_URLS.get(currentSearchEngine) + text);
                hideSearchUI();
            }
            return true;
        });
    }

    private void showSearchUI() {
        searchOverlay.setVisibility(View.VISIBLE);
        String currentUrl = getCurrentWebView().getUrl();
        searchInput.setText(currentUrl.equals(HOME_URL) ? "" : currentUrl);
        searchInput.selectAll();
        searchInput.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) imm.showSoftInput(searchInput, InputMethodManager.SHOW_IMPLICIT);
    }

    private void hideSearchUI() {
        searchOverlay.setVisibility(View.GONE);
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) imm.hideSoftInputFromWindow(searchInput.getWindowToken(), 0);
    }

    private void toggleBottomBar(boolean show) {
        if (show && isBarHidden) { 
            bottomBar.animate().translationY(0).setDuration(200).start(); 
            isBarHidden = false; 
        } else if (!show && !isBarHidden) { 
            float translation = "top".equals(barPosition) ? -(bottomBar.getHeight() + 200) : (bottomBar.getHeight() + 200);
            bottomBar.animate().translationY(translation).setDuration(200).start(); 
            isBarHidden = true; 
        }
    }

    private void updateUrlDisplay(String url) {
        if (url == null || url.equals(HOME_URL)) urlText.setText("Search or enter URL");
        else if (url.equals(SETTINGS_URL)) urlText.setText("Settings");
        else urlText.setText(url.replace("https://", "").replace("http://", ""));
    }

    private void hideFullscreen() {
        if (customView == null) return;

        fullscreenContainer.removeView(customView);
        fullscreenContainer.setVisibility(View.GONE);
        customView = null;
        if (customViewCallback != null) customViewCallback.onCustomViewHidden();

        webViewContainer.setVisibility(View.VISIBLE);
        bottomControlArea.setVisibility(View.VISIBLE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            getWindow().getInsetsController().show(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
        } else {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
        }
    }

    @Override
    public void onBackPressed() {
        if (customView != null) {
            hideFullscreen();
        } else if (searchOverlay.getVisibility() == View.VISIBLE) {
            hideSearchUI();
        }
        else if (findBar.getVisibility() == View.VISIBLE) { findBar.setVisibility(View.GONE); getCurrentWebView().clearMatches(); }
        else if (getCurrentWebView().canGoBack()) getCurrentWebView().goBack();
        else if (currentTabIndex > 0) switchTab(0);
        else if (System.currentTimeMillis() - lastBackPressTime < 2000) super.onBackPressed();
        else { lastBackPressTime = System.currentTimeMillis(); Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT).show(); }
    }
}
