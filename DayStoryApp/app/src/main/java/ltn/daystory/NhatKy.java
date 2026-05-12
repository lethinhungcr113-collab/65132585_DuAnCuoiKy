package ltn.daystory;

public class NhatKy {
    private String noiDung;
    private String duongDanAnh;
    private String ngayThang;

    public NhatKy() {
    }


    public NhatKy(String noiDung, String duongDanAnh, String ngayThang) {
        this.noiDung = noiDung;
        this.duongDanAnh = duongDanAnh;
        this.ngayThang = ngayThang;
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

    public String getNgayThang() {
        return ngayThang;
    }

    public void setNgayThang(String ngayThang) {
        this.ngayThang = ngayThang;
    }
}