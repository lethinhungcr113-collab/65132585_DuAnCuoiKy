package ltn.daystory;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.PopupMenu;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private FirebaseFirestore duLieu;

    private NhatKyAdapter adapter;

    private List<NhatKy> danhSachNhatKy;

    private ListenerRegistration listenerNhatKy;
    private boolean sapXepMoiNhat = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        setContentView(R.layout.activity_main);

        // SYSTEM BAR

        if (findViewById(R.id.main) != null) {

            ViewCompat.setOnApplyWindowInsetsListener(

                    findViewById(R.id.main),

                    (v, insets) -> {

                        Insets systemBars =
                                insets.getInsets(
                                        WindowInsetsCompat.Type.systemBars()
                                );

                        v.setPadding(
                                systemBars.left,
                                0,
                                systemBars.right,
                                0
                        );

                        return insets;
                    }
            );
        }

        // ÁNH XẠ VIEW

        RecyclerView rv =
                findViewById(R.id.rvDiaries);

        View nutThem =
                findViewById(R.id.btnOpenAdd);

        MaterialCardView btnSort =
                findViewById(R.id.btnSort);

        // FIREBASE

        duLieu =
                FirebaseFirestore.getInstance();

        danhSachNhatKy =
                new ArrayList<>();

        adapter =
                new NhatKyAdapter(
                        this,
                        danhSachNhatKy
                );

        rv.setLayoutManager(
                new LinearLayoutManager(this)
        );

        rv.setAdapter(adapter);

        layDuLieuTuFirebase(
                Query.Direction.DESCENDING
        );

        // BUTTON SORT
        btnSort.setOnClickListener(v -> {
            showFilterMenu(v);
        });
        // MỞ ADD STORY

        nutThem.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            MainActivity.this,
                            AddStoryActivity.class
                    );

            startActivity(intent);
        });
    }

    // LẤY DỮ LIỆU FIREBASE

    private void layDuLieuTuFirebase(
            Query.Direction huongSapXep
    ) {

        // Hủy listener cũ

        if (listenerNhatKy != null) {

            listenerNhatKy.remove();
        }

        listenerNhatKy = duLieu

                .collection("DanhSachNhatKy")

                .orderBy(
                        "ngayThang",
                        huongSapXep
                )

                .addSnapshotListener((value, error) -> {

                    if (error != null) {

                        error.printStackTrace();

                        return;
                    }

                    if (value != null) {

                        try {

                            danhSachNhatKy.clear();

                            for (DocumentSnapshot doc :
                                    value.getDocuments()) {

                                NhatKy nhatKy =
                                        doc.toObject(
                                                NhatKy.class
                                        );

                                if (nhatKy != null) {

                                    nhatKy.setDocumentId(
                                            doc.getId()
                                    );

                                    danhSachNhatKy.add(
                                            nhatKy
                                    );
                                }
                            }

                            adapter.notifyDataSetChanged();

                        } catch (Exception e) {

                            e.printStackTrace();
                        }
                    }
                });
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();

        if (listenerNhatKy != null) {

            listenerNhatKy.remove();
        }
    }
    // HÀM HIỂN THỊ MENU LỌC
    private void showFilterMenu(View v) {
        View menuView = getLayoutInflater().inflate(R.layout.layout_filter_menu, null);
        android.widget.PopupWindow popupWindow = new android.widget.PopupWindow(menuView, 500,
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                true);

        popupWindow.setElevation(20);
        popupWindow.setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));

        // Xử lý click nút Mới nhất
        menuView.findViewById(R.id.btnSortNewest).setOnClickListener(view -> {
            sapXepMoiNhat = true;
            layDuLieuTuFirebase(Query.Direction.DESCENDING);
            popupWindow.dismiss();
        });

        // Xử lý click nút Cũ nhất
        menuView.findViewById(R.id.btnSortOldest).setOnClickListener(view -> {
            sapXepMoiNhat = false;
            layDuLieuTuFirebase(Query.Direction.ASCENDING);
            popupWindow.dismiss();
        });

        popupWindow.showAsDropDown(v, 0, 10);
    }
}