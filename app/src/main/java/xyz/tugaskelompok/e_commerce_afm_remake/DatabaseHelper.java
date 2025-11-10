package xyz.tugaskelompok.e_commerce_afm_remake;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "ecommerce_afm.db";
    private static final int DATABASE_VERSION = 2;

    public static final String TABLE_USERS = "users";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_EMAIL = "email";
    public static final String COLUMN_PASSWORD = "password";
    public static final String COLUMN_ROLE = "role"; // "user" or "admin"

    public static final String TABLE_PRODUCTS = "products";
    public static final String COLUMN_PRODUCT_ID = "id";
    public static final String COLUMN_PRODUCT_NAME = "name";
    public static final String COLUMN_PRODUCT_CATEGORY = "category";
    public static final String COLUMN_PRODUCT_PRICE = "price";
    public static final String COLUMN_PRODUCT_IMAGE_URL = "image_url";
    public static final String COLUMN_PRODUCT_DESCRIPTION = "description";
    public static final String COLUMN_PRODUCT_STOCK = "stock";

    private static final String SQL_CREATE_USERS =
            "CREATE TABLE IF NOT EXISTS " + TABLE_USERS + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_NAME + " TEXT NOT NULL, " +
                    COLUMN_EMAIL + " TEXT NOT NULL UNIQUE, " +
                    COLUMN_PASSWORD + " TEXT NOT NULL, " +
                    COLUMN_ROLE + " TEXT NOT NULL CHECK(" + COLUMN_ROLE + " IN ('user','admin'))" +
            ")";

    private static final String SQL_CREATE_PRODUCTS =
            "CREATE TABLE IF NOT EXISTS " + TABLE_PRODUCTS + " (" +
                    COLUMN_PRODUCT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_PRODUCT_NAME + " TEXT NOT NULL, " +
                    COLUMN_PRODUCT_CATEGORY + " TEXT NOT NULL, " +
                    COLUMN_PRODUCT_PRICE + " REAL NOT NULL, " +
                    COLUMN_PRODUCT_IMAGE_URL + " TEXT, " +
                    COLUMN_PRODUCT_DESCRIPTION + " TEXT, " +
                    COLUMN_PRODUCT_STOCK + " INTEGER DEFAULT 0" +
            ")";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_USERS);
        db.execSQL(SQL_CREATE_PRODUCTS);
        seedDefaultAdmin(db);
        seedSampleProducts(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL(SQL_CREATE_PRODUCTS);
            seedSampleProducts(db);
        }
        // Future migrations can be added here
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        seedDefaultAdmin(db);
    }

    public boolean isEmailExists(String email) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_USERS,
                new String[]{COLUMN_ID},
                COLUMN_EMAIL + " = ?",
                new String[]{email},
                null,
                null,
                null
        );
        boolean exists = cursor != null && cursor.moveToFirst();
        if (cursor != null) cursor.close();
        return exists;
    }

    public boolean registerUser(String name, String email, String password, String role) {
        if (isEmailExists(email)) return false;
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, name);
        values.put(COLUMN_EMAIL, email);
        values.put(COLUMN_PASSWORD, password);
        values.put(COLUMN_ROLE, role);
        long id = db.insert(TABLE_USERS, null, values);
        return id != -1;
    }

    public String authenticateAndGetRole(String email, String password) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_USERS,
                new String[]{COLUMN_ROLE},
                COLUMN_EMAIL + " = ? AND " + COLUMN_PASSWORD + " = ?",
                new String[]{email, password},
                null,
                null,
                null
        );
        String role = null;
        if (cursor != null && cursor.moveToFirst()) {
            role = cursor.getString(0);
        }
        if (cursor != null) cursor.close();
        return role; // null if not found
    }

    private void seedDefaultAdmin(SQLiteDatabase db) {
        Cursor cursor = db.query(
                TABLE_USERS,
                new String[]{COLUMN_ID},
                COLUMN_ROLE + " = ?",
                new String[]{"admin"},
                null,
                null,
                null
        );
        boolean hasAdmin = cursor != null && cursor.moveToFirst();
        if (cursor != null) cursor.close();
        if (!hasAdmin) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_NAME, "Administrator");
            values.put(COLUMN_EMAIL, "admin@gmail.com");
            values.put(COLUMN_PASSWORD, "admin");
            values.put(COLUMN_ROLE, "admin");
            db.insert(TABLE_USERS, null, values);
        }
    }

    private void seedSampleProducts(SQLiteDatabase db) {
        // Check if products already exist
        Cursor cursor = db.query(
                TABLE_PRODUCTS,
                new String[]{COLUMN_PRODUCT_ID},
                null,
                null,
                null,
                null,
                null
        );
        boolean hasProducts = cursor != null && cursor.getCount() > 0;
        if (cursor != null) cursor.close();
        
        if (hasProducts) return; // Don't seed if products already exist

        // Sample CPU products
        insertProduct(db, "Intel Core i5-12400F", "CPU", 2500000, "", "Intel Core i5-12400F Processor", 10);
        insertProduct(db, "AMD Ryzen 5 5600X", "CPU", 3200000, "", "AMD Ryzen 5 5600X Processor", 8);
        insertProduct(db, "Intel Core i7-12700K", "CPU", 5500000, "", "Intel Core i7-12700K Processor", 5);
        
        // Sample GPU products
        insertProduct(db, "NVIDIA RTX 3060", "GPU", 6500000, "", "NVIDIA GeForce RTX 3060", 6);
        insertProduct(db, "AMD RX 6600 XT", "GPU", 5500000, "", "AMD Radeon RX 6600 XT", 7);
        insertProduct(db, "NVIDIA RTX 4070", "GPU", 12000000, "", "NVIDIA GeForce RTX 4070", 4);
        
        // Sample Case products
        insertProduct(db, "Corsair 4000D", "Case", 1500000, "", "Corsair 4000D Airflow Case", 12);
        insertProduct(db, "NZXT H510", "Case", 1200000, "", "NZXT H510 Mid Tower Case", 10);
        
        // Sample SSD products
        insertProduct(db, "Samsung 980 PRO 1TB", "SSD", 2500000, "", "Samsung 980 PRO 1TB NVMe SSD", 15);
        insertProduct(db, "WD Black SN850 1TB", "SSD", 2200000, "", "WD Black SN850 1TB NVMe SSD", 12);
        
        // Sample RAM products
        insertProduct(db, "Corsair Vengeance 16GB DDR4", "RAM", 1200000, "", "Corsair Vengeance LPX 16GB DDR4", 20);
        insertProduct(db, "G.Skill Trident Z 32GB DDR4", "RAM", 2500000, "", "G.Skill Trident Z 32GB DDR4", 8);
        
        // Sample PSU products
        insertProduct(db, "Corsair RM750x 750W", "PSU", 2000000, "", "Corsair RM750x 750W 80+ Gold", 10);
        insertProduct(db, "Seasonic Focus GX-650", "PSU", 1800000, "", "Seasonic Focus GX-650 80+ Gold", 9);
    }

    private void insertProduct(SQLiteDatabase db, String name, String category, double price, String imageUrl, String description, int stock) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_PRODUCT_NAME, name);
        values.put(COLUMN_PRODUCT_CATEGORY, category);
        values.put(COLUMN_PRODUCT_PRICE, price);
        values.put(COLUMN_PRODUCT_IMAGE_URL, imageUrl);
        values.put(COLUMN_PRODUCT_DESCRIPTION, description);
        values.put(COLUMN_PRODUCT_STOCK, stock);
        db.insert(TABLE_PRODUCTS, null, values);
    }

    // Product CRUD methods
    public List<Product> getAllProducts() {
        List<Product> products = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_PRODUCTS,
                null,
                null,
                null,
                null,
                null,
                COLUMN_PRODUCT_NAME + " ASC"
        );

        if (cursor != null && cursor.moveToFirst()) {
            do {
                Product product = cursorToProduct(cursor);
                products.add(product);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return products;
    }

    public List<Product> getProductsByCategory(String category) {
        List<Product> products = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_PRODUCTS,
                null,
                COLUMN_PRODUCT_CATEGORY + " = ?",
                new String[]{category},
                null,
                null,
                COLUMN_PRODUCT_NAME + " ASC"
        );

        if (cursor != null && cursor.moveToFirst()) {
            do {
                Product product = cursorToProduct(cursor);
                products.add(product);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return products;
    }

    public List<Product> searchProducts(String searchQuery) {
        List<Product> products = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        String query = "%" + searchQuery + "%";
        Cursor cursor = db.query(
                TABLE_PRODUCTS,
                null,
                COLUMN_PRODUCT_NAME + " LIKE ?",
                new String[]{query},
                null,
                null,
                COLUMN_PRODUCT_NAME + " ASC"
        );

        if (cursor != null && cursor.moveToFirst()) {
            do {
                Product product = cursorToProduct(cursor);
                products.add(product);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return products;
    }

    public List<Product> getProductsByCategoryAndSearch(String category, String searchQuery) {
        List<Product> products = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        String query = "%" + searchQuery + "%";
        String selection = COLUMN_PRODUCT_NAME + " LIKE ?";
        String[] selectionArgs = new String[]{query};

        if (category != null && !category.isEmpty()) {
            selection += " AND " + COLUMN_PRODUCT_CATEGORY + " = ?";
            selectionArgs = new String[]{query, category};
        }

        Cursor cursor = db.query(
                TABLE_PRODUCTS,
                null,
                selection,
                selectionArgs,
                null,
                null,
                COLUMN_PRODUCT_NAME + " ASC"
        );

        if (cursor != null && cursor.moveToFirst()) {
            do {
                Product product = cursorToProduct(cursor);
                products.add(product);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return products;
    }

    public Product getProductById(int id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_PRODUCTS,
                null,
                COLUMN_PRODUCT_ID + " = ?",
                new String[]{String.valueOf(id)},
                null,
                null,
                null
        );

        Product product = null;
        if (cursor != null && cursor.moveToFirst()) {
            product = cursorToProduct(cursor);
            cursor.close();
        }
        return product;
    }

    private Product cursorToProduct(Cursor cursor) {
        Product product = new Product();
        product.setId(String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_PRODUCT_ID))));
        product.setName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PRODUCT_NAME)));
        product.setCategory(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PRODUCT_CATEGORY)));
        product.setPrice(cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_PRODUCT_PRICE)));
        product.setImageUrl(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PRODUCT_IMAGE_URL)));
        return product;
    }

    public boolean addProduct(String name, String category, double price, String imageUrl, String description, int stock) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_PRODUCT_NAME, name);
        values.put(COLUMN_PRODUCT_CATEGORY, category);
        values.put(COLUMN_PRODUCT_PRICE, price);
        values.put(COLUMN_PRODUCT_IMAGE_URL, imageUrl);
        values.put(COLUMN_PRODUCT_DESCRIPTION, description);
        values.put(COLUMN_PRODUCT_STOCK, stock);
        long id = db.insert(TABLE_PRODUCTS, null, values);
        return id != -1;
    }

    public boolean updateProduct(int id, String name, String category, double price, String imageUrl, String description, int stock) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_PRODUCT_NAME, name);
        values.put(COLUMN_PRODUCT_CATEGORY, category);
        values.put(COLUMN_PRODUCT_PRICE, price);
        values.put(COLUMN_PRODUCT_IMAGE_URL, imageUrl);
        values.put(COLUMN_PRODUCT_DESCRIPTION, description);
        values.put(COLUMN_PRODUCT_STOCK, stock);
        int rowsAffected = db.update(TABLE_PRODUCTS, values, COLUMN_PRODUCT_ID + " = ?", new String[]{String.valueOf(id)});
        return rowsAffected > 0;
    }

    public boolean deleteProduct(int id) {
        SQLiteDatabase db = getWritableDatabase();
        int rowsAffected = db.delete(TABLE_PRODUCTS, COLUMN_PRODUCT_ID + " = ?", new String[]{String.valueOf(id)});
        return rowsAffected > 0;
    }
}


