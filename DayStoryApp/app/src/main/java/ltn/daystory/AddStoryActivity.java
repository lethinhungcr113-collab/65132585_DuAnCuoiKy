package ltn.daystory;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class AddStoryActivity extends AppCompatActivity {

    // Bộ quản lý kết quả trả về từ Camera và Thư viện
    private ActivityResultLauncher<Intent> boChupAnh;
    private ActivityResultLauncher<Intent> boChonAnh;

    private ImageView imgSelected;
    private EditText edtContent;
    private MaterialButton btnSave;
    private View layoutUploadInfo;

    private Bitmap bitmapAnhChup = null;
    private boolean isEditMode = false;
    private String documentId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.addstory_activity);

        anhXaView();
        setupLaunchers(); // Thiết lập bộ xử lý ảnh
        setupClickEvents();
        kiemTraCheDoSua();
    }

    private void anhXaView() {
        imgSelected = findViewById(R.id.imgSelected);
        edtContent = findViewById(R.id.edtContent);
        btnSave = findViewById(R.id.btnSave);
        layoutUploadInfo = findViewById(R.id.layoutUploadInfo);
        ImageView btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());
    }

    private void setupLaunchers() {
        // 1. Xử lý sau khi CHỤP ẢNH xong
        boChupAnh = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                ketQua -> {
                    if (ketQua.getResultCode() == RESULT_OK && ketQua.getData() != null) {
                        Bundle extras = ketQua.getData().getExtras();
                        if (extras != null) {
                            bitmapAnhChup = (Bitmap) extras.get("data");
                            hienThiAnhDaChon();
                        }
                    }
                }
        );

        // 2. Xử lý CHỌN ẢNH TỪ THƯ VIỆN
        boChonAnh = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                ketQua -> {
                    if (ketQua.getResultCode() == RESULT_OK && ketQua.getData() != null) {
                        Uri imageUri = ketQua.getData().getData();
                        try {
                            InputStream inputStream = getContentResolver().openInputStream(imageUri);

                            BitmapFactory.Options options = new BitmapFactory.Options();
                            options.inSampleSize = 2; 

                            bitmapAnhChup = BitmapFactory.decodeStream(inputStream, null, options);

                            if (bitmapAnhChup != null) {
                                hienThiAnhDaChon();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(this, "Lỗi đọc ảnh!", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );
    }

    private void hienThiAnhDaChon() {
        if (bitmapAnhChup != null) {
            imgSelected.setVisibility(View.VISIBLE);
            imgSelected.setImageBitmap(bitmapAnhChup);
            if (layoutUploadInfo != null) {
                layoutUploadInfo.setVisibility(View.GONE);
            }
        }
    }

    private void setupClickEvents() {
        // Khi bấm vào khung ảnh -> Hiện Dialog lựa chọn
        View.OnClickListener selectImageListener = v -> showImageSelectionDialog();
        findViewById(R.id.cardImage).setOnClickListener(selectImageListener);
        imgSelected.setOnClickListener(selectImageListener);

        // Nút lưu dữ liệu
        btnSave.setOnClickListener(v -> {
            String noiDung = edtContent.getText().toString().trim();

            if (bitmapAnhChup == null) {
                Toast.makeText(this, "Vui lòng thêm ảnh trước!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (noiDung.isEmpty()) {
                Toast.makeText(this, "Hãy viết gì đó cho hôm nay nhé!", Toast.LENGTH_SHORT).show();
                return;
            }

            btnSave.setEnabled(false);
            btnSave.setText(isEditMode ? "Đang cập nhật..." : "Đang lưu...");
            luuDuLieuLenFirebase();
        });
    }

    private void showImageSelectionDialog() {
        String[] options = {"Chụp ảnh mới", "Chọn từ thư viện", "Hủy"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Thêm ảnh vào nhật ký");
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                // Chụp ảnh
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                boChupAnh.launch(intent);
            } else if (which == 1) {
                // Chọn từ thư viện
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                boChonAnh.launch(intent);
            } else {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    private void kiemTraCheDoSua() {
        Intent intent = getIntent();
        if (intent.hasExtra("isEdit")) {
            isEditMode = true;
            documentId = intent.getStringExtra("documentId");
            String oldContent = intent.getStringExtra("oldContent");
            String oldImage = intent.getStringExtra("oldImage");

            edtContent.setText(oldContent);

            if (oldImage != null && !oldImage.isEmpty()) {
                byte[] decodedString = Base64.decode(oldImage, Base64.DEFAULT);
                bitmapAnhChup = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                hienThiAnhDaChon();
            }
            btnSave.setText("CẬP NHẬT NHẬT KÝ");
        }
    }

    private void luuDuLieuLenFirebase() {
        String text = edtContent.getText().toString().trim();
        String anhMaHoa = "";

        if (bitmapAnhChup != null) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            // Nén ảnh xuống 60% để tiết kiệm dung lượng Firebase
            bitmapAnhChup.compress(Bitmap.CompressFormat.JPEG, 60, baos);
            byte[] b = baos.toByteArray();
            anhMaHoa = Base64.encodeToString(b, Base64.DEFAULT);
        }

        Map<String, Object> nhatKy = new HashMap<>();
        nhatKy.put("noiDung", text);
        nhatKy.put("duongDanAnh", anhMaHoa);
        nhatKy.put("ngayThang", new Date().toString());

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        if (isEditMode && !documentId.isEmpty()) {
            firestore.collection("DanhSachNhatKy").document(documentId)
                    .set(nhatKy)
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                        quayVeMain();
                    })
                    .addOnFailureListener(e -> resetSaveButton());
        } else {
            firestore.collection("DanhSachNhatKy").add(nhatKy)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(this, "Đã lưu nhật ký!", Toast.LENGTH_SHORT).show();
                        quayVeMain();
                    })
                    .addOnFailureListener(e -> resetSaveButton());
        }
    }

    private void resetSaveButton() {
        btnSave.setEnabled(true);
        btnSave.setText(isEditMode ? "CẬP NHẬT NHẬT KÝ" : "LƯU VÀO NHẬT KÝ");
        Toast.makeText(this, "Lỗi kết nối Firebase!", Toast.LENGTH_SHORT).show();
    }

    private void quayVeMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }
}