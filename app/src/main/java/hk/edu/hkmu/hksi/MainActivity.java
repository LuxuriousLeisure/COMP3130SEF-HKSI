package hk.edu.hkmu.hksi;

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private ListView listView;
    private EditText etSearch;
    private TextView tvEmpty;

    // ====================== 筛选变量（3个条件） ======================
    private String m_filterType = "";     // 學校類型：小學/中學
    private String m_filterGender = "";   // 學生性別：男女/男/女
    private String m_filterFinance = "";  // 資助種類：資助/官立/私立

    // 分页配置
    private static final int PAGE_SIZE = 20;
    private int currentPage = 1;
    private int totalPage = 1;

    // 分页按钮
    private Button btnPrev, btnNext;
    private Button[] pageBtns = new Button[5];

    // 搜索+筛选用：保存全部原始数据
    private List<HashMap<String, String>> allSchoolList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
//        btnFilterType.setOnClickListener(v -> showFilterDialog("學校類型",
//                new String[]{"全部", "小學", "中學"}, selectedText -> {
//                    m_filterType = selectedText.equals("全部") ? "" : selectedText;
//                    applySearchAndFilter();
//                }));
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
//        btnFilterGender.setOnClickListener(v -> showFilterDialog("學生性別",
//                new String[]{"全部", "男女", "男", "女"}, selectedText -> {
//                    m_filterGender = selectedText.equals("全部") ? "" : selectedText;
//                    applySearchAndFilter();
//                }));
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
//        btnFilterFinance.setOnClickListener(v -> showFilterDialog("資助種類",
//                new String[]{"全部", "資助", "官立", "私立"}, selectedText -> {
//                    m_filterFinance = selectedText.equals("全部") ? "" : selectedText;
//                    applySearchAndFilter();
//                }));
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
            String website = school.get(SchoolInfo.WEBSITE);

            new AlertDialog.Builder(MainActivity.this)
                    .setTitle(school.get(SchoolInfo.NAME))
                    .setMessage("电话：" + school.get(SchoolInfo.PHONE)
                            + "\n地址：" + school.get(SchoolInfo.ADDR)
                            + "\n官网：" + website)
                    .setPositiveButton("確定", null)
                    .setNegativeButton("打開官網", (dialog, which) -> {
                        android.content.Intent intent = new android.content.Intent(
                                MainActivity.this,
                                SchoolWebViewActivity.class
                        );
                        intent.putExtra(SchoolWebViewActivity.EXTRA_WEBSITE, website);
                        startActivity(intent);
                    })
                    .show();
        });
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

        for (HashMap<String, String> school : allSchoolList) {
            // 1. 搜索匹配
            boolean matchName = keyword.isEmpty()
                    || school.get(SchoolInfo.NAME).contains(keyword);

            // 2. 筛选匹配（使用重命名后的变量）
            boolean matchType = m_filterType.isEmpty()
                    || school.getOrDefault(SchoolInfo.SCHOOL_LEVEL, "").equals(m_filterType);
            boolean matchGender = m_filterGender.isEmpty()
                    || school.getOrDefault(SchoolInfo.STUDENTS_GENDER, "").equals(m_filterGender);
            boolean matchFinance = m_filterFinance.isEmpty()
                    || school.getOrDefault(SchoolInfo.FINANCE_TYPE, "").equals(m_filterFinance);

            // 全部满足才显示
            if (matchName && matchType && matchGender && matchFinance) {
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
    }

    // ====================== 筛选弹窗工具方法 ======================
    private void showFilterDialog(String title, String[] options, OnFilterSelectedListener listener) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setItems(options, (dialog, which) -> {
                    listener.onSelected(options[which]);
                    dialog.dismiss();
                })
                .show();
    }

    // 筛选回调接口
    interface OnFilterSelectedListener {
        void onSelected(String selected);
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