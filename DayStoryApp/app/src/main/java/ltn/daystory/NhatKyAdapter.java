package ltn.daystory;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class NhatKyAdapter extends RecyclerView.Adapter<NhatKyAdapter.NhatKyViewHolder> {

    private Context context;
    private List<NhatKy> danhSachNhatKy;

    public NhatKyAdapter(Context context, List<NhatKy> danhSachNhatKy) {
        this.context = context;
        this.danhSachNhatKy = danhSachNhatKy;
    }

    @NonNull
    @Override
    public NhatKyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.item_nhat_ky, parent, false);
        return new NhatKyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NhatKyViewHolder holder, int position) {
        NhatKy nhatKy = danhSachNhatKy.get(position);

        holder.txtNoiDung.setText(nhatKy.getNoiDung());
        holder.txtNgay.setText(nhatKy.getNgayThang());

        String anhData = nhatKy.getDuongDanAnh();
        if (anhData != null && !anhData.isEmpty()) {
            try {
                byte[] decodedString = Base64.decode(anhData, Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                holder.imgNhatKy.setImageBitmap(decodedByte);
            } catch (Exception e) {
                holder.imgNhatKy.setImageResource(R.drawable.ic_launcher_background);
            }
        } else {
            holder.imgNhatKy.setImageResource(R.drawable.ic_launcher_background);
        }

        // Sự kiện khi bấm vào nút Sửa
        holder.btnEdit.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(context, AddStoryActivity.class);
            // Gửi dữ liệu cũ sang màn hình Add để hiện lên
            intent.putExtra("isEdit", true);
            intent.putExtra("oldContent", nhatKy.getNoiDung());
            intent.putExtra("oldImage", nhatKy.getDuongDanAnh());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return danhSachNhatKy.size();
    }

    public static class NhatKyViewHolder extends RecyclerView.ViewHolder {
        TextView txtNoiDung, txtNgay;
        ImageView imgNhatKy, btnEdit;

        public NhatKyViewHolder(@NonNull View itemView) {
            super(itemView);
            txtNoiDung = itemView.findViewById(R.id.txtNoiDung);
            txtNgay = itemView.findViewById(R.id.txtNgay);
            imgNhatKy = itemView.findViewById(R.id.imgNhatKy);
            btnEdit = itemView.findViewById(R.id.btnEdit);
        }
    }
}