package ltn.daystory;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private FirebaseFirestore duLieu;
    private NhatKyAdapter adapter;
    private List<NhatKy> danhSachNhatKy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        if (findViewById(R.id.main) != null) {
            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        // 1. Kết nối giao diện
        RecyclerView rv = findViewById(R.id.rvDiaries);
        android.view.View nutThem = findViewById(R.id.btnOpenAdd);

        duLieu = FirebaseFirestore.getInstance();
        danhSachNhatKy = new ArrayList<>();
        adapter = new NhatKyAdapter(this, danhSachNhatKy);

        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        // 2. Lấy dữ liệu từ Firebase
        layDuLieuTuFirebase();

        // 3. Bấm nút để qua màn hình thêm mới
        nutThem.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddStoryActivity.class);
            startActivity(intent);
        });
    }

    private void layDuLieuTuFirebase() {
        duLieu.collection("DanhSachNhatKy")
                .orderBy("ngayThang", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        return;
                    }
                    if (value != null) {
                        try {
                            danhSachNhatKy.clear();
                            // Chuyển dữ liệu từ Firebase vào List
                            danhSachNhatKy.addAll(value.toObjects(NhatKy.class));
                            adapter.notifyDataSetChanged();
                        } catch (Exception e) {
                            // Nếu văng ở đây là do Class NhatKy chưa chuẩn
                            e.printStackTrace();
                        }
                    }
                });
    }
}