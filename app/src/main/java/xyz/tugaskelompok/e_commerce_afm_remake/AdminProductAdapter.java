package xyz.tugaskelompok.e_commerce_afm_remake;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AdminProductAdapter extends RecyclerView.Adapter<AdminProductAdapter.ProductViewHolder> {

    public interface ProductActionListener {
        void onEdit(Product product);
        void onDelete(Product product);
    }

    private List<Product> products;
    private final ProductActionListener listener;

    public AdminProductAdapter(List<Product> products, ProductActionListener listener) {
        this.products = products != null ? products : new ArrayList<>();
        this.listener = listener;
    }

    public void updateProducts(List<Product> newProducts) {
        this.products = newProducts != null ? newProducts : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        holder.bind(products.get(position));
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    class ProductViewHolder extends RecyclerView.ViewHolder {
        private final TextView textViewName;
        private final TextView textViewCategory;
        private final TextView textViewPrice;
        private final TextView textViewStock;
        private final Button buttonEdit;
        private final Button buttonDelete;

        ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewName = itemView.findViewById(R.id.textViewProductName);
            textViewCategory = itemView.findViewById(R.id.textViewProductCategory);
            textViewPrice = itemView.findViewById(R.id.textViewProductPrice);
            textViewStock = itemView.findViewById(R.id.textViewProductStock);
            buttonEdit = itemView.findViewById(R.id.buttonEdit);
            buttonDelete = itemView.findViewById(R.id.buttonDelete);
        }

        void bind(Product product) {
            textViewName.setText(product.getName());
            textViewCategory.setText(product.getCategory());

            NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
            textViewPrice.setText(format.format(product.getPrice()));
            textViewStock.setText("Stok: " + product.getStock());

            buttonEdit.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEdit(product);
                }
            });

            buttonDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDelete(product);
                }
            });
        }
    }
}


