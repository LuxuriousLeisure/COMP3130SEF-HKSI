package hk.edu.hkmu.hksi;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// 1. 实现 OnMapReadyCallback 接口
public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    private ListView listView;
    private EditText etSearch;
    private TextView tvEmpty;

    // 地图相关变量
    private GoogleMap mMap;

    // ====================== 筛选变量（3个条件） ======================
    private String m_filterType = "";     // 學校類型：小學/中學
    private String m_filterGender = "";   // 學生性別：男女/男/女
    private String m_filterFinance = "";  // 資助種類：資助/官立/私立
    private String m_filterFavorite = ""; // 收藏：全部/已收藏

    // 分页配置
    private static final int PAGE_SIZE = 20;
    private int currentPage = 1;
    private int totalPage = 1;

    // 分页按钮
    private Button btnPrev, btnNext;
    private Button[] pageBtns = new Button[5];

    // 搜索+筛选用：保存全部原始数据
    private List<HashMap<String, String>> allSchoolList = new ArrayList<>();

    private SharedPreferences mSharedPref;
    private static final String KEY_FAVORITE_SCHOOLS = "favorite_school_set";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSharedPref = getApplicationContext().getSharedPreferences(
                "SchoolFavoritePref",  // 配置文件名
                Context.MODE_PRIVATE   // 私有模式
        );

        // 绑定控件
        listView = findViewById(R.id.list_view);
        etSearch = findViewById(R.id.et_search);
        btnPrev = findViewById(R.id.btn_prev);
        btnNext = findViewById(R.id.btn_next);
        tvEmpty = findViewById(R.id.tv_empty);
        pageBtns[0] = findViewById(R.id.page_btn1);
        pageBtns[1] = findViewById(R.id.page_btn2);
        pageBtns[2] = findViewById(R.id.page_btn3);
        pageBtns[3] = findViewById(R.id.page_btn4);
        pageBtns[4] = findViewById(R.id.page_btn5);

        // ====================== 绑定筛选按钮 ======================
        Button btnFilterType = findViewById(R.id.filter_type);
        Button btnFilterGender = findViewById(R.id.filter_gender);
        Button btnFilterFinance = findViewById(R.id.filter_finance);

        // 1. 学校类型筛选
        btnFilterType.setText("學校類型 全部");
        btnFilterType.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(v.getContext(), btnFilterType);
            popupMenu.getMenuInflater().inflate(R.menu.menu_school_type, popupMenu.getMenu());

            popupMenu.setOnMenuItemClickListener(item -> {
                String selected = "";
                int id = item.getItemId();
                if (id == R.id.option_all) selected = "全部";
                else if (id == R.id.option_primary) selected = "小學";
                else if (id == R.id.option_middle) selected = "中學";

                m_filterType = selected.equals("全部") ? "" : selected;
                btnFilterType.setText("學校類型 " + selected);
                applySearchAndFilter();
                Toast.makeText(this, "选中：" + selected, Toast.LENGTH_SHORT).show();
                return true;
            });
            popupMenu.show();
        });

        // 2. 学生性别筛选
        btnFilterGender.setText("學生性別 全部");
        btnFilterGender.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(v.getContext(), btnFilterGender);
            popupMenu.getMenuInflater().inflate(R.menu.menu_gender, popupMenu.getMenu());

            popupMenu.setOnMenuItemClickListener(item -> {
                String selected = "";
                int id = item.getItemId();
                if (id == R.id.gender_all) selected = "全部";
                else if (id == R.id.gender_mix) selected = "男女";
                else if (id == R.id.gender_male) selected = "男";
                else if (id == R.id.gender_female) selected = "女";

                m_filterGender = selected.equals("全部") ? "" : selected;
                btnFilterGender.setText("學生性別 " + selected);
                applySearchAndFilter();
                return true;
            });
            popupMenu.show();
        });

        // 3. 资助种类筛选
        btnFilterFinance.setText("資助種類 全部");
        btnFilterFinance.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(v.getContext(), btnFilterFinance);
            popupMenu.getMenuInflater().inflate(R.menu.menu_finance, popupMenu.getMenu());

            popupMenu.setOnMenuItemClickListener(item -> {
                String selected = "";
                int id = item.getItemId();
                if (id == R.id.finance_all) selected = "全部";
                else if (id == R.id.finance_subsidy) selected = "資助";
                else if (id == R.id.finance_official) selected = "官立";
                else if (id == R.id.finance_private) selected = "私立";

                m_filterFinance = selected.equals("全部") ? "" : selected;
                btnFilterFinance.setText("資助種類 " + selected);
                applySearchAndFilter();
                return true;
            });
            popupMenu.show();
        });

        Button btnFilterFavorite = findViewById(R.id.filter_favorite);
        btnFilterFavorite.setText("收藏 全部");
        btnFilterFavorite.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(v.getContext(), btnFilterFavorite);
            // 加载刚才新建的菜单文件
            popupMenu.getMenuInflater().inflate(R.menu.menu_favorite, popupMenu.getMenu());

            popupMenu.setOnMenuItemClickListener(item -> {
                String selected = "";
                int id = item.getItemId();
                if (id == R.id.fav_all) selected = "全部";
                else if (id == R.id.fav_favorited) selected = "已收藏";

                m_filterFavorite = selected.equals("全部") ? "" : selected;
                btnFilterFavorite.setText("收藏 " + selected);
                applySearchAndFilter(); // 联动刷新
                return true;
            });
            popupMenu.show();
        });

        // 拉取JSON数据
        JsonHandlerThread thread = new JsonHandlerThread();
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // 保存全部原始数据
        allSchoolList.addAll(SchoolInfo.schoolList);

        // 计算总页数
        int totalSize = SchoolInfo.schoolList.size();
        totalPage = (totalSize + PAGE_SIZE - 1) / PAGE_SIZE;

        // 初始化事件
        initPageEvent();
        initSearchEvent();

        // 首次加载
        refreshList();
        refreshPageButtons();

        // 列表点击弹窗（原有功能完全保留）
        listView.setOnItemClickListener((parent, view, position, id) -> {
            HashMap<String,String> school = getCurrentPageData().get(position);
            String schoolName = school.get(SchoolInfo.NAME);
            String website = school.get(SchoolInfo.WEBSITE);

            // 【官方规范】读取收藏集合
            Set<String> originFavSet = mSharedPref.getStringSet(KEY_FAVORITE_SCHOOLS, new HashSet<>());
            // 必须复制新集合（谷歌强制要求：不可修改原生返回的Set）
            HashSet<String> editFavSet = new HashSet<>(originFavSet);
            boolean isFavorited = editFavSet.contains(schoolName);

            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this)
                    .setTitle(schoolName)
                    .setMessage("电话：" + school.get(SchoolInfo.PHONE)
                            + "\n地址：" + school.get(SchoolInfo.ADDR)
                            + "\n官网：" + website)
                    .setPositiveButton("確定", null)
                    .setNegativeButton("打開官網", (dialog, which) -> {
                        Intent intent = new Intent(MainActivity.this, SchoolWebViewActivity.class);
                        intent.putExtra(SchoolWebViewActivity.EXTRA_WEBSITE, website);
                        startActivity(intent);
                    })
                    // 收藏按钮文字自动适配状态
                    .setNeutralButton(isFavorited ? "取消收藏" : "加入收藏", (dialog, which) -> {
                        // 编辑集合
                        if (isFavorited) {
                            editFavSet.remove(schoolName);
                            Toast.makeText(MainActivity.this, "已取消收藏", Toast.LENGTH_SHORT).show();
                        } else {
                            editFavSet.add(schoolName);
                            Toast.makeText(MainActivity.this, "收藏成功", Toast.LENGTH_SHORT).show();
                        }

                        // ========== 官方标准写入提交（谷歌教程原版） ==========
                        SharedPreferences.Editor editor = mSharedPref.edit();
                        editor.putStringSet(KEY_FAVORITE_SCHOOLS, editFavSet);
                        editor.apply(); // 异步提交 官方推荐

                        // 刷新列表 → 爱心立即同步更新
                        applySearchAndFilter();
                    });

            builder.show();
        });

        // 2. 初始化地图
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    // 3. 实现地图回调方法
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        refreshMapMarkers();
    }

    // ====================== 搜索监听 ======================
    private void initSearchEvent() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                applySearchAndFilter(); // 搜索+筛选联动
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    // ====================== 核心：搜索 + 筛选 同时生效 ======================
    private void applySearchAndFilter() {
        String keyword = etSearch.getText().toString().trim();
        SchoolInfo.schoolList.clear();

        Set<String> favoriteSet = mSharedPref.getStringSet(KEY_FAVORITE_SCHOOLS, new HashSet<>());


        for (HashMap<String, String> school : allSchoolList) {
            // 1. 搜索匹配
//            boolean matchName = keyword.isEmpty()
//                    || school.get(SchoolInfo.NAME).contains(keyword);
            boolean matchName = keyword.isEmpty()
                    || (school.get(SchoolInfo.NAME) != null && school.get(SchoolInfo.NAME).contains(keyword))
                    || (school.get(SchoolInfo.NAME_EN) != null && school.get(SchoolInfo.NAME_EN).contains(keyword));

            // 2. 筛选匹配（使用重命名后的变量）
            boolean matchType = m_filterType.isEmpty()
                    || school.getOrDefault(SchoolInfo.SCHOOL_LEVEL, "").equals(m_filterType);
            boolean matchGender = m_filterGender.isEmpty()
                    || school.getOrDefault(SchoolInfo.STUDENTS_GENDER, "").equals(m_filterGender);
            boolean matchFinance = m_filterFinance.isEmpty()
                    || school.getOrDefault(SchoolInfo.FINANCE_TYPE, "").equals(m_filterFinance);

            boolean matchFavorite = m_filterFavorite.isEmpty(); // 默认“全部”直接通过
            if (!m_filterFavorite.isEmpty()) {
                // 如果是“已收藏”，检查学校名是否在收藏集合中
                String schoolName = school.get(SchoolInfo.NAME);
                matchFavorite = favoriteSet.contains(schoolName);
            }

            // 全部满足才显示
            if (matchName && matchType && matchGender && matchFinance && matchFavorite) {
                SchoolInfo.schoolList.add(school);
            }
        }

        // 空结果判断
        if (SchoolInfo.schoolList.isEmpty()) {
            listView.setVisibility(View.GONE);
            tvEmpty.setVisibility(View.VISIBLE);
        } else {
            listView.setVisibility(View.VISIBLE);
            tvEmpty.setVisibility(View.GONE);
        }

        // 重置分页
        currentPage = 1;
        totalPage = (SchoolInfo.schoolList.size() + PAGE_SIZE - 1) / PAGE_SIZE;
        refreshList();
        refreshPageButtons();
        refreshMapMarkers();
    }

    private void refreshMapMarkers() {
        // 如果地图还没准备好，就不执行
        if (mMap == null) return;

        // 1. 清空地图上旧的 Marker
        mMap.clear();

        // 2. 遍历当前符合条件的所有学校（注意：我们在地图上展示所有符合条件的学校，而不是只展示当前页的 20 个，这样体验更好）
        for (HashMap<String, String> school : SchoolInfo.schoolList) {
            try {
                String latStr = school.get(SchoolInfo.LATITUDE);
                String lngStr = school.get(SchoolInfo.LONGITUDE);

                if (latStr != null && lngStr != null && !latStr.isEmpty() && !lngStr.isEmpty()) {
                    double lat = Double.parseDouble(latStr);
                    double lng = Double.parseDouble(lngStr);
                    LatLng location = new LatLng(lat, lng);

                    // 添加标记
                    mMap.addMarker(new MarkerOptions()
                            .position(location)
                            .title(school.get(SchoolInfo.NAME))
                            .snippet("電話: " + school.get(SchoolInfo.PHONE)));
                }
            } catch (NumberFormatException e) {
                // 防止某些数据经纬度格式异常导致崩溃
                e.printStackTrace();
            }
        }

        // 3. (可选) 自动调整摄像头位置：如果列表有数据，将镜头移动到第一个学校的位置
        if (!SchoolInfo.schoolList.isEmpty()) {
            try {
                double firstLat = Double.parseDouble(SchoolInfo.schoolList.get(0).get(SchoolInfo.LATITUDE));
                double firstLng = Double.parseDouble(SchoolInfo.schoolList.get(0).get(SchoolInfo.LONGITUDE));
                // 移动镜头，Zoom 参数可根据需要调整 (如 11 适合看全港，14 适合看街区)
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(firstLat, firstLng), 12));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // ====================== 分页功能（完全不变） ======================
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
                String pageText = ((Button) v).getText().toString();
                currentPage = Integer.parseInt(pageText);
                refreshList();
                refreshPageButtons();
            });
        }
    }

    // ====================== 刷新列表（奇偶变色保留） ======================
    private void refreshList() {
        SimpleAdapter adapter = new SimpleAdapter(
                this,
                getCurrentPageData(),
                R.layout.list_item_school,
                new String[]{SchoolInfo.NAME, SchoolInfo.DISTRICT, SchoolInfo.PHONE},
                new int[]{R.id.tv_name, R.id.tv_district, R.id.tv_phone}
        ) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                LinearLayout cardLayout = view.findViewById(R.id.item_card_layout);

                // 1. 绑定爱心控件
                TextView tvFavoriteIcon = view.findViewById(R.id.tv_favorite_icon);
                // 2. 获取当前行学校数据
                HashMap<String, String> currentSchool = (HashMap<String, String>) getItem(position);
                String schoolName = currentSchool.get(SchoolInfo.NAME);

                // ========== 读取收藏集合 ==========
                Set<String> favoriteSet = mSharedPref.getStringSet(KEY_FAVORITE_SCHOOLS, new HashSet<>());

                // 3. 判断收藏状态，切换爱心（直观UI展示）
                if (favoriteSet.contains(schoolName)) {
                    tvFavoriteIcon.setText("❤️"); // 已收藏：实心红心
                } else {
                    tvFavoriteIcon.setText("🤍"); // 未收藏：空心爱心
                }

                // 奇偶行变色（你的原有效果）
                if (position % 2 == 0) {
                    cardLayout.setBackgroundResource(R.drawable.rounded_item_white);
                } else {
                    cardLayout.setBackgroundResource(R.drawable.rounded_item_gray);
                }
                return view;
            }
        };
        listView.setAdapter(adapter);
    }

    // ====================== 获取分页数据（不变） ======================
    private List<HashMap<String, String>> getCurrentPageData() {
        List<HashMap<String, String>> pageList = new ArrayList<>();
        int start = (currentPage - 1) * PAGE_SIZE;
        int end = start + PAGE_SIZE;

        if (end > SchoolInfo.schoolList.size()) {
            end = SchoolInfo.schoolList.size();
        }

        for (int i = start; i < end; i++) {
            pageList.add(SchoolInfo.schoolList.get(i));
        }
        return pageList;
    }

    // ====================== 页码按钮刷新（不变） ======================
    private void refreshPageButtons() {
        int startPage = currentPage - 2;
        if (startPage < 1) startPage = 1;

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
}