package xyz.tugaskelompok.e_commerce_afm_remake;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {
    private List<Product> productList;
    private OnProductClickListener listener;

    public interface OnProductClickListener {
        void onProductClick(Product product);
    }

    public ProductAdapter(List<Product> productList) {
        this.productList = productList != null ? productList : new ArrayList<>();
    }

    public void setOnProductClickListener(OnProductClickListener listener) {
        this.listener = listener;
    }

    public void updateProducts(List<Product> newProducts) {
        this.productList = newProducts != null ? newProducts : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);
        holder.bind(product);
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    class ProductViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageViewProduct;
        private TextView textViewProductName;
        private TextView textViewProductPrice;
        private TextView textViewProductCategory;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewProduct = itemView.findViewById(R.id.imageViewProduct);
            textViewProductName = itemView.findViewById(R.id.textViewProductName);
            textViewProductPrice = itemView.findViewById(R.id.textViewProductPrice);
            textViewProductCategory = itemView.findViewById(R.id.textViewProductCategory);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onProductClick(productList.get(position));
                }
            });
        }

        public void bind(Product product) {
            textViewProductName.setText(product.getName());
            
            NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
            textViewProductPrice.setText(format.format(product.getPrice()));
            
            textViewProductCategory.setText(product.getCategory());

            // Load image if available
            String imageUrl = product.getImageUrl();
            if (imageUrl != null && !imageUrl.isEmpty()) {
                try {
                    if (imageUrl.startsWith("content://") || imageUrl.startsWith("file://")) {
                        imageViewProduct.setImageURI(Uri.parse(imageUrl));
                    } else if (imageUrl.startsWith("http://") || imageUrl.startsWith("https://")) {
                        // Load from network on background thread
                        imageViewProduct.setImageBitmap(null);
                        new Thread(() -> {
                            Bitmap bmp = downloadBitmap(imageUrl);
                            if (bmp != null) {
                                imageViewProduct.post(() -> imageViewProduct.setImageBitmap(bmp));
                            }
                        }).start();
                    }
                } catch (Exception ignored) {
                }
            }
        }

        private Bitmap downloadBitmap(String src) {
            HttpURLConnection connection = null;
            InputStream input = null;
            try {
                URL url = new URL(src);
                connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);
                connection.setInstanceFollowRedirects(true);
                connection.connect();
                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) return null;
                input = connection.getInputStream();
                return BitmapFactory.decodeStream(input);
            } catch (Exception e) {
                return null;
            } finally {
                try { if (input != null) input.close(); } catch (Exception ignored) {}
                if (connection != null) connection.disconnect();
            }
        }
    }
}

