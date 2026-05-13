package ltn.daystory;

import com.google.firebase.firestore.Exclude;

public class NhatKy {
    private String documentId;
    private String noiDung;
    private String duongDanAnh;

    // Sử dụng Object để có thể nhận cả Timestamp (từ Firebase) hoặc String (dữ liệu cũ)
    private Object ngayThang;

    // Constructor mặc định bắt buộc phải có để Firebase .toObject() hoạt động
    public NhatKy() {
    }

    public NhatKy(String noiDung, String duongDanAnh, Object ngayThang) {
        this.noiDung = noiDung;
        this.duongDanAnh = duongDanAnh;
        this.ngayThang = ngayThang;
    }

    // Exclude giúp Firebase không tạo thêm một trường "documentId" thừa thãi bên trong văn bản
    @Exclude
    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getNoiDung() {
        return noiDung;
    }

    public void setNoiDung(String noiDung) {
        this.noiDung = noiDung;
    }

    public String getDuongDanAnh() {
        return duongDanAnh;
    }

    public void setDuongDanAnh(String duongDanAnh) {
        this.duongDanAnh = duongDanAnh;
    }

    public Object getNgayThang() {
        return ngayThang;
    }

    public void setNgayThang(Object ngayThang) {
        this.ngayThang = ngayThang;
    }
}