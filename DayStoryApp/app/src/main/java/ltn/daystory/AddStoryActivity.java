package ltn.daystory;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class AddStoryActivity extends AppCompatActivity {

    private ActivityResultLauncher<Intent> boChupAnh;

    private ImageView imgSelected;
    private EditText edtContent;
    private MaterialButton btnSave;

    private Bitmap bitmapAnhChup = null;

    private boolean isEditMode = false;

    // ID của document Firestore
    private String documentId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.addstory_activity);

        anhXaView();
        setupCameraLauncher();
        setupClickEvents();
        kiemTraCheDoSua();
    }

    private void anhXaView() {

        imgSelected = findViewById(R.id.imgSelected);
        edtContent = findViewById(R.id.edtContent);
        btnSave = findViewById(R.id.btnSave);

        ImageView btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());
    }

    private void setupCameraLauncher() {

        boChupAnh = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                ketQua -> {

                    if (ketQua.getResultCode() == RESULT_OK
                            && ketQua.getData() != null
                            && ketQua.getData().getExtras() != null) {

                        bitmapAnhChup =
                                (Bitmap) ketQua.getData()
                                        .getExtras()
                                        .get("data");

                        if (bitmapAnhChup != null) {
                            imgSelected.setVisibility(View.VISIBLE);
                            imgSelected.setImageBitmap(bitmapAnhChup);

                            View layoutUploadInfo = findViewById(R.id.layoutUploadInfo);
                            if (layoutUploadInfo != null) {
                                layoutUploadInfo.setVisibility(View.GONE);
                            }
                        }
                    }
                }
        );
    }


    private void setupClickEvents() {

        // Click card ảnh để mở camera
        findViewById(R.id.cardImage).setOnClickListener(v -> moCamera());

        imgSelected.setOnClickListener(v -> moCamera());

        // Nút lưu
        btnSave.setOnClickListener(v -> {

            String noiDung = edtContent.getText().toString().trim();

            // Kiểm tra ảnh
            if (bitmapAnhChup == null) {

                Toast.makeText(
                        this,
                        "Vui lòng thêm ảnh trước!",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            // Kiểm tra nội dung
            if (noiDung.isEmpty()) {

                Toast.makeText(
                        this,
                        "Hãy viết gì đó cho hôm nay nhé!",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            // Disable nút
            btnSave.setEnabled(false);

            if (isEditMode) {
                btnSave.setText("Đang cập nhật...");
            } else {
                btnSave.setText("Đang lưu...");
            }

            // Lưu dữ liệu
            luuDuLieuLenFirebase();
        });
    }


    private void moCamera() {

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        boChupAnh.launch(intent);
    }

    private void kiemTraCheDoSua() {

        Intent intent = getIntent();

        if (intent.hasExtra("isEdit")) {

            isEditMode = true;

            // Lấy documentId
            documentId = intent.getStringExtra("documentId");

            String oldContent = intent.getStringExtra("oldContent");
            String oldImage = intent.getStringExtra("oldImage");

            // Set nội dung cũ
            edtContent.setText(oldContent);

            // Set ảnh cũ
            if (oldImage != null && !oldImage.isEmpty()) {

                byte[] decodedString =
                        android.util.Base64.decode(
                                oldImage,
                                android.util.Base64.DEFAULT
                        );

                bitmapAnhChup = BitmapFactory.decodeByteArray(
                        decodedString,
                        0,
                        decodedString.length
                );

                if (bitmapAnhChup != null) {
                    imgSelected.setVisibility(View.VISIBLE);
                    imgSelected.setImageBitmap(bitmapAnhChup);

                    // Dán vào đây để ẩn chữ khi load bài cũ lên để sửa
                    View layoutUploadInfo = findViewById(R.id.layoutUploadInfo);
                    if (layoutUploadInfo != null) {
                        layoutUploadInfo.setVisibility(View.GONE);
                    }
                }
            }

            btnSave.setText("Cập nhật nhật ký");
        }
    }

    private void luuDuLieuLenFirebase() {

        String text = edtContent.getText().toString().trim();

        // Mã hóa ảnh
        String anhMaHoa = "";

        if (bitmapAnhChup != null) {

            ByteArrayOutputStream baos =
                    new ByteArrayOutputStream();

            bitmapAnhChup.compress(
                    Bitmap.CompressFormat.JPEG,
                    60,
                    baos
            );

            byte[] b = baos.toByteArray();

            anhMaHoa = android.util.Base64.encodeToString(
                    b,
                    android.util.Base64.DEFAULT
            );
        }

        // Object nhật ký
        Map<String, Object> nhatKy = new HashMap<>();

        nhatKy.put("noiDung", text);
        nhatKy.put("duongDanAnh", anhMaHoa);
        nhatKy.put("ngayThang", new Date().toString());

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        if (isEditMode && !documentId.isEmpty()) {

            firestore.collection("DanhSachNhatKy")
                    .document(documentId)
                    .set(nhatKy)

                    .addOnSuccessListener(unused -> {

                        Toast.makeText(
                                AddStoryActivity.this,
                                "Cập nhật thành công!",
                                Toast.LENGTH_SHORT
                        ).show();

                        quayVeMain();
                    })

                    .addOnFailureListener(e -> {

                        btnSave.setEnabled(true);
                        btnSave.setText("Cập nhật nhật ký");

                        Toast.makeText(
                                AddStoryActivity.this,
                                "Lỗi cập nhật dữ liệu!",
                                Toast.LENGTH_SHORT
                        ).show();
                    });
        }

        else {

            firestore.collection("DanhSachNhatKy")
                    .add(nhatKy)

                    .addOnSuccessListener(documentReference -> {

                        Toast.makeText(
                                AddStoryActivity.this,
                                "Đã lưu nhật ký!",
                                Toast.LENGTH_SHORT
                        ).show();

                        quayVeMain();
                    })

                    .addOnFailureListener(e -> {

                        btnSave.setEnabled(true);
                        btnSave.setText("LƯU VÀO NHẬT KÝ");

                        Toast.makeText(
                                AddStoryActivity.this,
                                "Lỗi lưu dữ liệu!",
                                Toast.LENGTH_SHORT
                        ).show();
                    });
        }
    }

    private void quayVeMain() {

        Intent intent = new Intent(
                AddStoryActivity.this,
                MainActivity.class
        );

        intent.addFlags(
                Intent.FLAG_ACTIVITY_CLEAR_TOP
                        | Intent.FLAG_ACTIVITY_SINGLE_TOP
        );

        startActivity(intent);
        finish();
    }
}