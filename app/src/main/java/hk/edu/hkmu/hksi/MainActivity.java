package hk.edu.hkmu.hksi;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.LocaleList;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.yanzhenjie.recyclerview.SwipeMenuCreator;
import com.yanzhenjie.recyclerview.SwipeMenuItem;
import com.yanzhenjie.recyclerview.SwipeRecyclerView;
import com.yanzhenjie.recyclerview.OnItemMenuClickListener;
import android.content.res.Configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    // ====================== 替换：ListView → SwipeRecyclerView ======================
    private SwipeRecyclerView mRecyclerView;
    private SchoolAdapter mAdapter;

    private EditText etSearch;
    private TextView tvEmpty;

    // 地图相关变量
    private GoogleMap mMap;

    // 筛选变量
    private String m_filterType = "";
    private String m_filterGender = "";
    private String m_filterFinance = "";
    private String m_filterFavorite = "";

    // 分页配置
    private static final int PAGE_SIZE = 20;
    private int currentPage = 1;
    private int totalPage = 1;

    // 分页按钮
    private Button btnPrev, btnNext;
    private Button[] pageBtns = new Button[5];

    // 数据集合
    private List<HashMap<String, String>> allSchoolList = new ArrayList<>();

    // 收藏持久化
    private SharedPreferences mSharedPref;
    private static final String KEY_FAVORITE_SCHOOLS = "favorite_school_set";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

//        SharedPreferences prefs = getSharedPreferences("Settings", MODE_PRIVATE);
//        String language = prefs.getString("My_Lang", "zh-rHK");
//        applyLocale(language);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSharedPref = getApplicationContext().getSharedPreferences(
                "SchoolFavoritePref",
                Context.MODE_PRIVATE
        );

        // ====================== 绑定控件：删除旧ListView ======================
        etSearch = findViewById(R.id.et_search);
        btnPrev = findViewById(R.id.btn_prev);
        btnNext = findViewById(R.id.btn_next);
        tvEmpty = findViewById(R.id.tv_empty);
        pageBtns[0] = findViewById(R.id.page_btn1);
        pageBtns[1] = findViewById(R.id.page_btn2);
        pageBtns[2] = findViewById(R.id.page_btn3);
        pageBtns[3] = findViewById(R.id.page_btn4);
        pageBtns[4] = findViewById(R.id.page_btn5);

        // 绑定新RecyclerView
        mRecyclerView = findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // ====================== 筛选按钮（完全保留原有逻辑） ======================
        Button btnFilterType = findViewById(R.id.filter_type);
        Button btnFilterGender = findViewById(R.id.filter_gender);
        Button btnFilterFinance = findViewById(R.id.filter_finance);
        Button btnFilterFavorite = findViewById(R.id.filter_favorite);

        // 学校类型筛选
//        btnFilterType.setText("學校類型 全部");
        btnFilterType.setText(getString(R.string.filter_type_label) + getString(R.string.all));


        btnFilterType.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(v.getContext(), btnFilterType);
            popupMenu.getMenuInflater().inflate(R.menu.menu_school_type, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(item -> {
                String selected = "";
                int id = item.getItemId();
                if (id == R.id.option_all) {
                    selected = getString(R.string.all);
                    m_filterType = "";
                } else if (id == R.id.option_primary) {
                    selected = getString(R.string.option_primary);
                    m_filterType = "小學"; // 数据库/API通常固定为中文，这里需匹配API数据源
                } else if (id == R.id.option_middle) {
                    selected = getString(R.string.option_middle);
                    m_filterType = "中學";
                }

                btnFilterType.setText(getString(R.string.filter_type_label) + " " + selected);
                applySearchAndFilter();
                Toast.makeText(this, getString(R.string.toast_selected) + selected, Toast.LENGTH_SHORT).show();
                return true;
            });
            popupMenu.show();
        });

        // 学生性别筛选
//        btnFilterGender.setText("學生性別 全部");
        btnFilterGender.setText(getString(R.string.filter_gender_label) + getString(R.string.all));
        btnFilterGender.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(v.getContext(), btnFilterGender);
            popupMenu.getMenuInflater().inflate(R.menu.menu_gender, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(item -> {
                String selected = "";
                int id = item.getItemId();
                if (id == R.id.gender_all) {
                    selected = getString(R.string.all);
                    m_filterGender = "";
                } else if (id == R.id.gender_mix) {
                    selected = getString(R.string.gender_mix);
                    m_filterGender = "男女";
                } else if (id == R.id.gender_male) {
                    selected = getString(R.string.gender_male);
                    m_filterGender = "男";
                } else if (id == R.id.gender_female) {
                    selected = getString(R.string.gender_female);
                    m_filterGender = "女";
                }

                btnFilterGender.setText(getString(R.string.filter_gender_label) + " " + selected);
                applySearchAndFilter();
                return true;
            });
            popupMenu.show();
        });

        // 资助种类筛选
//        btnFilterFinance.setText("資助種類 全部");
        btnFilterFinance.setText(getString(R.string.filter_finance_label) + getString(R.string.all));
        btnFilterFinance.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(v.getContext(), btnFilterFinance);
            popupMenu.getMenuInflater().inflate(R.menu.menu_finance, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(item -> {
                String selected = "";
                int id = item.getItemId();
                if (id == R.id.finance_all) {
                    selected = getString(R.string.all);
                    m_filterFinance = "";
                } else if (id == R.id.finance_subsidy) {
                    selected = getString(R.string.finance_subsidy);
                    m_filterFinance = "資助";
                } else if (id == R.id.finance_official) {
                    selected = getString(R.string.finance_official);
                    m_filterFinance = "官立";
                } else if (id == R.id.finance_private) {
                    selected = getString(R.string.finance_private);
                    m_filterFinance = "私立";
                }

                btnFilterFinance.setText(getString(R.string.filter_finance_label) + " " + selected);
                applySearchAndFilter();
                return true;
            });
            popupMenu.show();
        });

        // 收藏筛选
//        btnFilterFavorite.setText("收藏 全部");
        btnFilterFavorite.setText(getString(R.string.filter_fav_label) + getString(R.string.all));
        btnFilterFavorite.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(v.getContext(), btnFilterFavorite);
            popupMenu.getMenuInflater().inflate(R.menu.menu_favorite, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(item -> {
                String selected = "";
                int id = item.getItemId();
                if (id == R.id.fav_all) {
                    selected = getString(R.string.all);
                    m_filterFavorite = "";
                } else if (id == R.id.fav_favorited) {
                    selected = getString(R.string.fav_done);
                    m_filterFavorite = "已收藏";
                }

                btnFilterFavorite.setText(getString(R.string.filter_fav_label) + " " + selected);
                applySearchAndFilter();
                return true;
            });
            popupMenu.show();
        });

        // 绑定新按钮
        Button btnLanguage = findViewById(R.id.btn_language);
        btnLanguage.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(MainActivity.this, v);
            // 直接动态添加菜单项
            popup.getMenu().add(0, 1, 0, "English");
            popup.getMenu().add(0, 2, 1, "繁體中文");

            popup.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == 1) {
                    updateLocale("en");
                } else if (item.getItemId() == 2) {
                    updateLocale("zh"); // 对应繁体中文
                }
                return true;
            });
            popup.show();
        });

        // 拉取JSON数据（不变）
        JsonHandlerThread thread = new JsonHandlerThread();
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        allSchoolList.addAll(SchoolInfo.schoolList);
        int totalSize = SchoolInfo.schoolList.size();
        totalPage = (totalSize + PAGE_SIZE - 1) / PAGE_SIZE;

        initPageEvent();
        initSearchEvent();

        initSwipeMenu();
        initRecyclerItemClick();

        refreshList();
        refreshPageButtons();

        // 初始化地图（不变）
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    // ====================== 侧滑菜单初始化 ======================
    private void initSwipeMenu() {
        SwipeMenuCreator menuCreator = (leftMenu, rightMenu, position) -> {
            int width = getResources().getDisplayMetrics().widthPixels / 4;
            HashMap<String, String> school = getCurrentPageData().get(position);
            boolean isFavorited = mSharedPref.getStringSet(KEY_FAVORITE_SCHOOLS, new HashSet<>())
                    .contains(school.get(SchoolInfo.NAME));

            // 收藏按钮
            SwipeMenuItem favItem = new SwipeMenuItem(this)
                    .setBackgroundColor(isFavorited ? 0xFFFF4444 : 0xFF4CAF50)
//                    .setText(isFavorited ? "取消收藏" : "加入收藏")
                    .setText(isFavorited ? getString(R.string.swipe_unfav) : getString(R.string.swipe_fav))
                    .setTextColor(0xFFFFFFFF)
                    .setWidth(width)
                    .setHeight(ViewGroup.LayoutParams.MATCH_PARENT);
            rightMenu.addMenuItem(favItem);

            // 详情按钮
            SwipeMenuItem infoItem = new SwipeMenuItem(this)
                    .setBackgroundColor(0xFF2196F3)
//                    .setText("ℹ️ 詳情")
                    .setText("ℹ️ " + getString(R.string.swipe_detail)) // 使用 ID
                    .setTextColor(0xFFFFFFFF)
                    .setWidth(width)
                    .setHeight(ViewGroup.LayoutParams.MATCH_PARENT);
            rightMenu.addMenuItem(infoItem);
        };
        mRecyclerView.setSwipeMenuCreator(menuCreator);

        // 侧滑按钮点击事件
        mRecyclerView.setOnItemMenuClickListener((menuBridge, adapterPosition) -> {
            menuBridge.closeMenu();
            HashMap<String, String> school = getCurrentPageData().get(adapterPosition);
            String schoolName = school.get(SchoolInfo.NAME);

            if (menuBridge.getPosition() == 0) {
                // 收藏/取消收藏
                Set<String> originSet = mSharedPref.getStringSet(KEY_FAVORITE_SCHOOLS, new HashSet<>());
                HashSet<String> editSet = new HashSet<>(originSet);
                if (editSet.contains(schoolName)) {
                    editSet.remove(schoolName);
                    Toast.makeText(this, "已取消收藏", Toast.LENGTH_SHORT).show();
                } else {
                    editSet.add(schoolName);
                    Toast.makeText(this, "收藏成功", Toast.LENGTH_SHORT).show();
                }
                mSharedPref.edit().putStringSet(KEY_FAVORITE_SCHOOLS, editSet).apply();
                applySearchAndFilter();
            } else {
                // 详情弹窗（无收藏按钮）
                new AlertDialog.Builder(this)
                        .setTitle(schoolName)
//                        .setMessage("电话：" + school.get(SchoolInfo.PHONE)
//                                + "\n地址：" + school.get(SchoolInfo.ADDR)
//                                + "\n官网：" + school.get(SchoolInfo.WEBSITE))
//                        .setPositiveButton("確定", null)
//                        .setNegativeButton("打開官網", (dialog, which) -> {
                        .setMessage(getString(R.string.label_phone) + school.get(SchoolInfo.PHONE) // 建议在xml也定义label_phone
                                + "\n" + getString(R.string.label_addr) + school.get(SchoolInfo.ADDR)
                                + "\n" + getString(R.string.label_web) + school.get(SchoolInfo.WEBSITE))
                        .setPositiveButton(getString(R.string.confirm), null)
                        .setNegativeButton(getString(R.string.school_website), (dialog, which) -> {
                            Intent intent = new Intent(this, SchoolWebViewActivity.class);
                            intent.putExtra(SchoolWebViewActivity.EXTRA_WEBSITE, school.get(SchoolInfo.WEBSITE));
                            startActivity(intent);
                        }).show();
            }
        });
    }

    // ====================== 列表点击→地图居中 ======================
    private void initRecyclerItemClick() {
        mRecyclerView.setOnItemClickListener((view, position) -> {
            if (mMap == null) return;
            HashMap<String, String> school = getCurrentPageData().get(position);
            try {
                double lat = Double.parseDouble(school.get(SchoolInfo.LATITUDE));
                double lng = Double.parseDouble(school.get(SchoolInfo.LONGITUDE));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), 16));
            } catch (Exception e) {
                Toast.makeText(this, "無有效經緯度資料", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 地图就绪（不变）
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        refreshMapMarkers();
    }

    // 搜索监听（不变）
    private void initSearchEvent() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                applySearchAndFilter();
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    // 搜索+筛选（仅修改空数据控件）
    private void applySearchAndFilter() {
        String keyword = etSearch.getText().toString().trim();
        SchoolInfo.schoolList.clear();
        Set<String> favoriteSet = mSharedPref.getStringSet(KEY_FAVORITE_SCHOOLS, new HashSet<>());

        for (HashMap<String, String> school : allSchoolList) {
            boolean matchName = keyword.isEmpty()
                    || (school.get(SchoolInfo.NAME) != null && school.get(SchoolInfo.NAME).contains(keyword))
                    || (school.get(SchoolInfo.NAME_EN) != null && school.get(SchoolInfo.NAME_EN).contains(keyword));

            boolean matchType = m_filterType.isEmpty()
                    || school.getOrDefault(SchoolInfo.SCHOOL_LEVEL, "").equals(m_filterType);
            boolean matchGender = m_filterGender.isEmpty()
                    || school.getOrDefault(SchoolInfo.STUDENTS_GENDER, "").equals(m_filterGender);
            boolean matchFinance = m_filterFinance.isEmpty()
                    || school.getOrDefault(SchoolInfo.FINANCE_TYPE, "").equals(m_filterFinance);

            boolean matchFavorite = m_filterFavorite.isEmpty();
            if (!m_filterFavorite.isEmpty()) {
                matchFavorite = favoriteSet.contains(school.get(SchoolInfo.NAME));
            }

            if (matchName && matchType && matchGender && matchFinance && matchFavorite) {
                SchoolInfo.schoolList.add(school);
            }
        }

        // ====================== 替换：ListView → RecyclerView ======================
        if (SchoolInfo.schoolList.isEmpty()) {
            mRecyclerView.setVisibility(View.GONE);
            tvEmpty.setVisibility(View.VISIBLE);
        } else {
            mRecyclerView.setVisibility(View.VISIBLE);
            tvEmpty.setVisibility(View.GONE);
        }

        currentPage = 1;
        totalPage = (SchoolInfo.schoolList.size() + PAGE_SIZE - 1) / PAGE_SIZE;
        refreshList();
        refreshPageButtons();
        refreshMapMarkers();
    }

    // 地图标记（不变）
    private void refreshMapMarkers() {
        if (mMap == null) return;
        mMap.clear();

        for (HashMap<String, String> school : SchoolInfo.schoolList) {
            try {
                String latStr = school.get(SchoolInfo.LATITUDE);
                String lngStr = school.get(SchoolInfo.LONGITUDE);
                if (latStr != null && lngStr != null) {
                    double lat = Double.parseDouble(latStr);
                    double lng = Double.parseDouble(lngStr);
                    LatLng location = new LatLng(lat, lng);
                    mMap.addMarker(new MarkerOptions()
                            .position(location)
                            .title(school.get(SchoolInfo.NAME))
                            .snippet("電話: " + school.get(SchoolInfo.PHONE)));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (!SchoolInfo.schoolList.isEmpty()) {
            try {
                double firstLat = Double.parseDouble(SchoolInfo.schoolList.get(0).get(SchoolInfo.LATITUDE));
                double firstLng = Double.parseDouble(SchoolInfo.schoolList.get(0).get(SchoolInfo.LONGITUDE));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(firstLat, firstLng), 12));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // 分页事件（不变）
    private void initPageEvent() {
        btnPrev.setOnClickListener(v -> {
            if (currentPage > 1) {
                currentPage--;
                refreshList();
                refreshPageButtons();
            }
        });

        btnNext.setOnClickListener(v -> {
            if (currentPage < totalPage) {
                currentPage++;
                refreshList();
                refreshPageButtons();
            }
        });

        for (Button btn : pageBtns) {
            btn.setOnClickListener(v -> {
                currentPage = Integer.parseInt(((Button) v).getText().toString());
                refreshList();
                refreshPageButtons();
            });
        }
    }

    // ====================== 替换：RecyclerView刷新列表 ======================
    private void refreshList() {
        Set<String> favSet = mSharedPref.getStringSet(KEY_FAVORITE_SCHOOLS, new HashSet<>());
        List<HashMap<String, String>> currentPageData = getCurrentPageData();

        if (mAdapter == null) {
            mAdapter = new SchoolAdapter(currentPageData, favSet);
            mRecyclerView.setAdapter(mAdapter);
        } else {
            // 先更新适配器内部引用的数据源，再通知刷新
            mAdapter.updateData(currentPageData, favSet);
            mAdapter.notifyDataSetChanged();
        }
    }

    // 分页数据（不变）
    private List<HashMap<String, String>> getCurrentPageData() {
        List<HashMap<String, String>> pageList = new ArrayList<>();
        int start = (currentPage - 1) * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, SchoolInfo.schoolList.size());
        for (int i = start; i < end; i++) {
            pageList.add(SchoolInfo.schoolList.get(i));
        }
        return pageList;
    }

    // 页码刷新（不变）
    private void refreshPageButtons() {
        int startPage = Math.max(1, currentPage - 2);
        for (int i = 0; i < 5; i++) {
            int pageNum = startPage + i;
            if (pageNum > totalPage) {
                pageBtns[i].setVisibility(View.GONE);
            } else {
                pageBtns[i].setVisibility(View.VISIBLE);
                pageBtns[i].setText(String.valueOf(pageNum));

                if (pageNum == currentPage) {
                    pageBtns[i].setBackgroundColor(getResources().getColor(android.R.color.holo_blue_light));
                    pageBtns[i].setTextColor(getResources().getColor(android.R.color.white));
                } else {
                    pageBtns[i].setBackgroundColor(getResources().getColor(android.R.color.white));
                    pageBtns[i].setTextColor(getResources().getColor(android.R.color.black));
                }
            }
        }
    }

    // ====================== 新增：自定义RecyclerView适配器 ======================
    private class SchoolAdapter extends RecyclerView.Adapter<SchoolAdapter.ViewHolder> {
        private List<HashMap<String, String>> mData;
        private Set<String> mFavoriteSet;

        public SchoolAdapter(List<HashMap<String, String>> data, Set<String> favoriteSet) {
            this.mData = data;
            this.mFavoriteSet = favoriteSet;
        }

        @androidx.annotation.NonNull
        @Override
        public ViewHolder onCreateViewHolder(@androidx.annotation.NonNull ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.list_item_school, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@androidx.annotation.NonNull ViewHolder holder, int position) {
            HashMap<String, String> school = mData.get(position);
            String schoolName = school.get(SchoolInfo.NAME);

            holder.tvName.setText(schoolName);
            holder.tvDistrict.setText(school.get(SchoolInfo.DISTRICT));
            holder.tvPhone.setText(school.get(SchoolInfo.PHONE));

            // 收藏爱心状态
            holder.tvFavoriteIcon.setText(mFavoriteSet.contains(schoolName) ? "❤️" : "🤍");

            // 奇偶行变色
            if (position % 2 == 0) {
                holder.cardLayout.setBackgroundResource(R.drawable.rounded_item_white);
            } else {
                holder.cardLayout.setBackgroundResource(R.drawable.rounded_item_gray);
            }
        }

        @Override
        public int getItemCount() {
            return mData == null ? 0 : mData.size();
        }

        public void updateData(List<HashMap<String, String>> newData, Set<String> newFavSet) {
            this.mData = newData;
            this.mFavoriteSet = newFavSet;
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvDistrict, tvPhone, tvFavoriteIcon;
            LinearLayout cardLayout;

            public ViewHolder(View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tv_name);
                tvDistrict = itemView.findViewById(R.id.tv_district);
                tvPhone = itemView.findViewById(R.id.tv_phone);
                tvFavoriteIcon = itemView.findViewById(R.id.tv_favorite_icon);
                cardLayout = itemView.findViewById(R.id.item_card_layout);
            }
        }
    }

    private void updateLocale(String langCode) {
        // 1. 同步写入，确保立即生效
        SharedPreferences.Editor editor = getSharedPreferences("Settings", MODE_PRIVATE).edit();
        editor.putString("My_Lang", langCode);
        editor.commit();

        // 2. 完全重启应用，避免任何 Activity 栈缓存
        Intent intent = getBaseContext().getPackageManager()
                .getLaunchIntentForPackage(getBaseContext().getPackageName());
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
        finish();
        Runtime.getRuntime().exit(0); // 杀掉进程，彻底清零（可选，但对语言切换最有效）
    }

    // 提取出一个设置语言的方法，方便在 onCreate 调用
    private void applyLocale(String langCode) {
        Locale locale = new Locale(langCode);
        Locale.setDefault(locale);
        Configuration config = getResources().getConfiguration();
        config.setLocale(locale);
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        // 读取保存的语言，没有就默认英文（或者你想要的默认语言）
        SharedPreferences prefs = newBase.getSharedPreferences("Settings", MODE_PRIVATE);
        String lang = prefs.getString("My_Lang", "en");

        // 创建目标 Locale
        Locale targetLocale = "zh".equals(lang) ? Locale.TRADITIONAL_CHINESE : Locale.ENGLISH;

        // 执行语言配置（标准模版代码）
        Locale.setDefault(targetLocale);
        Resources res = newBase.getResources();
        Configuration config = new Configuration(res.getConfiguration());

        // 兼容不同 API 版本
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocale(targetLocale);
            config.setLocales(new LocaleList(targetLocale));
        } else {
            config.locale = targetLocale;
        }

        // 生成新的 Context 并传递给父类
        Context context = newBase.createConfigurationContext(config);
        super.attachBaseContext(context);
    }
}