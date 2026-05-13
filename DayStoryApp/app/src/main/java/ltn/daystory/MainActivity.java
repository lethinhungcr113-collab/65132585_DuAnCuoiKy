package ltn.daystory;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private FirebaseFirestore duLieu;
    private NhatKyAdapter adapter;
    private List<NhatKy> danhSachNhatKy;

    // Bộ quản lý lắng nghe dữ liệu từ Firebase
    private ListenerRegistration listenerNhatKy;

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

        // Thiết lập Padding cho hệ thống (Status bar, Navigation bar)
        if (findViewById(R.id.main) != null) {
            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, 0, systemBars.right, 0);
                return insets;
            });
        }

        // Ánh xạ View
        RecyclerView rv = findViewById(R.id.rvDiaries);
        View nutThem = findViewById(R.id.btnOpenAdd);
        btnNewest = findViewById(R.id.btnNewest);
        btnOldest = findViewById(R.id.btnOldest);
        txtNewest = findViewById(R.id.txtNewest);
        txtOldest = findViewById(R.id.txtOldest);

        // Khởi tạo Firebase và Danh sách
        duLieu = FirebaseFirestore.getInstance();
        danhSachNhatKy = new ArrayList<>();
        adapter = new NhatKyAdapter(this, danhSachNhatKy);

        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        // Load mặc định: Mới nhất lên đầu
        layDuLieuTuFirebase(Query.Direction.DESCENDING);

        // Sự kiện nút MỚI NHẤT
        btnNewest.setOnClickListener(v -> {
            if (!sapXepMoiNhat) {
                sapXepMoiNhat = true;
                capNhatMauNut();
                layDuLieuTuFirebase(Query.Direction.DESCENDING);
            }
        });

        // Sự kiện nút CŨ NHẤT
        btnOldest.setOnClickListener(v -> {
            if (sapXepMoiNhat) {
                sapXepMoiNhat = false;
                capNhatMauNut();
                layDuLieuTuFirebase(Query.Direction.ASCENDING);
            }
        });

        // Sự kiện Mở màn hình Thêm Nhật Ký
        nutThem.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddStoryActivity.class);
            startActivity(intent);
        });

        // Màu sắc nút mặc định
        capNhatMauNut();
    }

    // ===================================================
    // LẤY DỮ LIỆU TỪ FIREBASE (CÓ SẮP XẾP)
    // ===================================================
    private void layDuLieuTuFirebase(Query.Direction huongSapXep) {

        // Hủy bộ lắng nghe cũ nếu đang chạy để tránh tốn tài nguyên và loạn dữ liệu
        if (listenerNhatKy != null) {
            listenerNhatKy.remove();
        }

        // Tạo bộ lắng nghe mới
        listenerNhatKy = duLieu.collection("DanhSachNhatKy")
                .orderBy("ngayThang", huongSapXep)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        error.printStackTrace();
                        return;
                    }

                    if (value != null) {
                        try {
                            danhSachNhatKy.clear();
                            for (DocumentSnapshot doc : value.getDocuments()) {
                                NhatKy nhatKy = doc.toObject(NhatKy.class);
                                if (nhatKy != null) {
                                    // Quan trọng: Gán ID để có thể Sửa/Xóa
                                    nhatKy.setDocumentId(doc.getId());
                                    danhSachNhatKy.add(nhatKy);
                                }
                            }
                            adapter.notifyDataSetChanged();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    // ===================================================
    // CẬP NHẬT GIAO DIỆN NÚT LỌC
    // ===================================================
    private void capNhatMauNut() {
        if (sapXepMoiNhat) {
            btnNewest.setCardBackgroundColor(android.graphics.Color.parseColor("#8A63D2"));
            btnOldest.setCardBackgroundColor(android.graphics.Color.parseColor("#F6EFE8"));
            txtNewest.setTextColor(android.graphics.Color.parseColor("#FFFFFF"));
            txtOldest.setTextColor(android.graphics.Color.parseColor("#8F7A66"));
        } else {
            btnOldest.setCardBackgroundColor(android.graphics.Color.parseColor("#8A63D2"));
            btnNewest.setCardBackgroundColor(android.graphics.Color.parseColor("#F6EFE8"));
            txtOldest.setTextColor(android.graphics.Color.parseColor("#FFFFFF"));
            txtNewest.setTextColor(android.graphics.Color.parseColor("#8F7A66"));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Giải phóng bộ lắng nghe khi thoát App để tránh rò rỉ bộ nhớ
        if (listenerNhatKy != null) {
            listenerNhatKy.remove();
        }
    }
}