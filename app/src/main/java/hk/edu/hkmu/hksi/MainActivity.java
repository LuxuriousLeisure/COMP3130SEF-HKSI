package hk.edu.hkmu.hksi;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private ListView listView;
    private EditText etSearch; // 顶部搜索框（已加入）
    private TextView tvEmpty;
    // 分页配置
    private static final int PAGE_SIZE = 20;
    private int currentPage = 1;
    private int totalPage = 1;

    // 分页按钮
    private Button btnPrev, btnNext;
    private Button[] pageBtns = new Button[5];

    // 搜索用：保存全部学校数据
    private List<HashMap<String, String>> allSchoolList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 绑定控件
        listView = findViewById(R.id.list_view);
        etSearch = findViewById(R.id.et_search); // 绑定搜索框
        btnPrev = findViewById(R.id.btn_prev);
        btnNext = findViewById(R.id.btn_next);
        tvEmpty = findViewById(R.id.tv_empty);
        pageBtns[0] = findViewById(R.id.page_btn1);
        pageBtns[1] = findViewById(R.id.page_btn2);
        pageBtns[2] = findViewById(R.id.page_btn3);
        pageBtns[3] = findViewById(R.id.page_btn4);
        pageBtns[4] = findViewById(R.id.page_btn5);

        // 拉取JSON数据
        JsonHandlerThread thread = new JsonHandlerThread();
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // 保存全部学校（用于搜索）
        allSchoolList.addAll(SchoolInfo.schoolList);

        // 计算总页数
        int totalSize = SchoolInfo.schoolList.size();
        totalPage = (totalSize + PAGE_SIZE - 1) / PAGE_SIZE;

        // 初始化分页 + 搜索监听
        initPageEvent();
        initSearchEvent(); // 搜索初始化（新增）

        // 首次加载
        refreshList();
        refreshPageButtons();

        // 列表点击弹窗
        listView.setOnItemClickListener((parent, view, position, id) -> {
            HashMap<String,String> school = getCurrentPageData().get(position);
            // 拿到当前学校的官网地址
            String website = school.get(SchoolInfo.WEBSITE);

            new AlertDialog.Builder(MainActivity.this)
                    .setTitle(school.get(SchoolInfo.NAME))
                    .setMessage("电话：" + school.get(SchoolInfo.PHONE)
                            + "\n地址：" + school.get(SchoolInfo.ADDR)
                            + "\n官网：" + website)
                    // 左边按钮：确定（Lab02 风格）
                    .setPositiveButton("確定", null)
                    // 右边按钮：打开官网（核心！）
                    .setNegativeButton("打開官網", (dialog, which) -> {
                        // 👇 完全照搬 Lab03 Intent 传值跳转
                        android.content.Intent intent = new android.content.Intent(
                                MainActivity.this,
                                SchoolWebViewActivity.class
                        );
                        // 传官网地址给 WebView 页面
                        intent.putExtra(SchoolWebViewActivity.EXTRA_WEBSITE, website);
                        startActivity(intent);
                    })
                    .show();
        });
    }

    // ====================== 搜索功能（完全抄Lab02输入框） ======================
    private void initSearchEvent() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterSchool(s.toString()); // 实时筛选
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    // 搜索筛选学校
    private void filterSchool(String keyword) {
        SchoolInfo.schoolList.clear();

        if (keyword.isEmpty()) {
            // 空输入 → 恢复全部
            SchoolInfo.schoolList.addAll(allSchoolList);
        } else {
            // 匹配学校名称
            for (HashMap<String, String> school : allSchoolList) {
                String name = school.get(SchoolInfo.NAME);
                if (name.contains(keyword)) {
                    SchoolInfo.schoolList.add(school);
                }
            }
        }

        // ================== 空結果判斷 ==================
        if (SchoolInfo.schoolList.isEmpty()) {
            // 沒結果：顯示提示、隱藏列表
            listView.setVisibility(View.GONE);
            tvEmpty.setVisibility(View.VISIBLE);
        } else {
            // 有結果：顯示列表、隱藏提示
            listView.setVisibility(View.VISIBLE);
            tvEmpty.setVisibility(View.GONE);
        }

        // 搜索后回到第1页
        currentPage = 1;
        totalPage = (SchoolInfo.schoolList.size() + PAGE_SIZE - 1) / PAGE_SIZE;
        refreshList();
        refreshPageButtons();
    }

    // ====================== 分页功能 ======================
    private void initPageEvent() {
        // 上一页
        btnPrev.setOnClickListener(v -> {
            if (currentPage > 1) {
                currentPage--;
                refreshList();
                refreshPageButtons();
            }
        });

        // 下一页
        btnNext.setOnClickListener(v -> {
            if (currentPage < totalPage) {
                currentPage++;
                refreshList();
                refreshPageButtons();
            }
        });

        // 页码点击
        for (Button btn : pageBtns) {
            btn.setOnClickListener(v -> {
                String pageText = ((Button) v).getText().toString();
                currentPage = Integer.parseInt(pageText);
                refreshList();
                refreshPageButtons();
            });
        }
    }

    // 刷新列表
    private void refreshList() {
        SimpleAdapter adapter = new SimpleAdapter(
                this,
                getCurrentPageData(),
                R.layout.list_item_school,
                new String[]{SchoolInfo.NAME, SchoolInfo.DISTRICT, SchoolInfo.PHONE},
                new int[]{R.id.tv_name, R.id.tv_district, R.id.tv_phone}
        );
        listView.setAdapter(adapter);
    }

    // 获取当前页数据
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

    // 刷新页码按钮（已修复颜色方法）
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

                // 统一用 getResources().getColor() 兼容API29
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