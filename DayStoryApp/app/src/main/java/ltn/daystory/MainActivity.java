package ltn.daystory;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.TextView;

import com.google.android.material.card.MaterialCardView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private FirebaseFirestore duLieu;
    private NhatKyAdapter adapter;
    private List<NhatKy> danhSachNhatKy;

    // Nút sắp xếp
    private MaterialCardView btnNewest;
    private MaterialCardView btnOldest;
    private TextView txtNewest;
    private TextView txtOldest;

    // Trạng thái hiện tại
    private boolean sapXepMoiNhat = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        if (findViewById(R.id.main) != null) {
            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());

                v.setPadding(
                        systemBars.left,
                        0,
                        systemBars.right,
                        0
                );

                return insets;
            });
        }

        // RecyclerView
        RecyclerView rv = findViewById(R.id.rvDiaries);

        // Nút thêm
        View nutThem = findViewById(R.id.btnOpenAdd);

        // Nút lọc
        btnNewest = findViewById(R.id.btnNewest);
        btnOldest = findViewById(R.id.btnOldest);
        txtNewest = findViewById(R.id.txtNewest);
        txtOldest = findViewById(R.id.txtOldest);

        // Firebase
        duLieu = FirebaseFirestore.getInstance();

        // List
        danhSachNhatKy = new ArrayList<>();

        // Adapter
        adapter = new NhatKyAdapter(this, danhSachNhatKy);

        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        // Load mặc định: mới nhất
        layDuLieuTuFirebase(Query.Direction.DESCENDING);

        // =========================
        // NÚT MỚI NHẤT
        // =========================
        btnNewest.setOnClickListener(v -> {

            sapXepMoiNhat = true;

            capNhatMauNut();

            layDuLieuTuFirebase(Query.Direction.DESCENDING);
        });

        // =========================
        // NÚT CŨ NHẤT
        // =========================
        btnOldest.setOnClickListener(v -> {

            sapXepMoiNhat = false;

            capNhatMauNut();

            layDuLieuTuFirebase(Query.Direction.ASCENDING);
        });

        // =========================
        // NÚT THÊM NHẬT KÝ
        // =========================
        nutThem.setOnClickListener(v -> {

            Intent intent = new Intent(MainActivity.this, AddStoryActivity.class);

            startActivity(intent);
        });

        // Set màu mặc định
        capNhatMauNut();
    }

    // ===================================================
    // LOAD FIREBASE
    // ===================================================
    private void layDuLieuTuFirebase(Query.Direction huongSapXep) {

        duLieu.collection("DanhSachNhatKy")
                .orderBy("ngayThang", huongSapXep)

                .addSnapshotListener((value, error) -> {

                    if (error != null) {
                        error.printStackTrace();
                        return;
                    }

                    if (value != null) {

                        try {

                            danhSachNhatKy.clear();

                            danhSachNhatKy.addAll(
                                    value.toObjects(NhatKy.class)
                            );

                            adapter.notifyDataSetChanged();

                        } catch (Exception e) {

                            e.printStackTrace();
                        }
                    }
                });
    }

    // ===================================================
    // ĐỔI MÀU 2 NÚT FILTER
    // ===================================================
    private void capNhatMauNut() {

        if (sapXepMoiNhat) {

            // BACKGROUND
            btnNewest.setCardBackgroundColor(
                    android.graphics.Color.parseColor("#8A63D2")
            );

            btnOldest.setCardBackgroundColor(
                    android.graphics.Color.parseColor("#F6EFE8")
            );

            // TEXT
            txtNewest.setTextColor(
                    android.graphics.Color.parseColor("#FFFFFF")
            );

            txtOldest.setTextColor(
                    android.graphics.Color.parseColor("#8F7A66")
            );

        } else {

            // BACKGROUND
            btnOldest.setCardBackgroundColor(
                    android.graphics.Color.parseColor("#8A63D2")
            );

            btnNewest.setCardBackgroundColor(
                    android.graphics.Color.parseColor("#F6EFE8")
            );

            // TEXT
            txtOldest.setTextColor(
                    android.graphics.Color.parseColor("#FFFFFF")
            );

            txtNewest.setTextColor(
                    android.graphics.Color.parseColor("#8F7A66")
            );
        }
    }
}